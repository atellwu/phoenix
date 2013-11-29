package com.dianping.platform.session.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.session.RequestEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class RequestEventHandler {
	
	private static Logger logger = Logger.getLogger(RequestEventHandler.class);

	private class HandlerTask implements Task {
		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!stop.get()) {
				RequestEvent event = null;
				try {
					event = rcvQ.take();
				} catch (InterruptedException e) {
					logger.info("Thread Interrupted, will exit");
					return;
				}
				
				switch (event.getHop()) {
				case HOP_CLIENT:
					if(isEventExpired(event)) {
						logger.info(String.format("Receive expired RequestEvent %s from client, will ignore", event));
					} else {
						processClientEvent(event);
					}
					break;

				case HOP_SERVER:
					processServerEvent(event);
					break;

				default:
					logger.error(String.format("Unknown hop %d received, will ignore", event.getHop()));
					break;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
	private class RetryQueueCleanTask implements Task {
		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!stop.get()) {
				if (retryCache.size() > config.getRetryQueueSafeLength()) {
					for (Map.Entry<String, RequestEvent> entry : retryCache.entrySet()) {
						RequestEvent event = entry.getValue();

						if (System.currentTimeMillis() > event.getTimestamp() + config.getEventExpireTime()) {
							logger.warn(String.format("RequestEvent %s is expired", event));
							retryCache.remove(entry.getKey());
						}
					}
				}
				try {
					Thread.sleep(config.getRetryQueueCleanInterval());
				} catch (InterruptedException e) {
					logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	public final static int HOP_CLIENT = 0;

	public final static int HOP_SERVER = 1;

	@Inject
	private BlockingQueue<RequestEvent> rcvQ;

	@Inject
	private BlockingQueue<RequestEvent> sendQ;

	@Inject
	private RequestEventRecorder rec;
	@Inject
	private ConfigManager config;
	
	private ConcurrentMap<String, RequestEvent> retryCache;

	private ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> l1Cache;

	private AtomicBoolean stop = new AtomicBoolean();

	public RequestEventHandler() {
	}

	public RequestEventHandler(BlockingQueue<RequestEvent> rcvQ, BlockingQueue<RequestEvent> sendQ,
			RequestEventRecorder recorder) {
		this(rcvQ, sendQ, recorder, new ConfigManager());
	}

	public RequestEventHandler(BlockingQueue<RequestEvent> rcvQ, BlockingQueue<RequestEvent> sendQ,
			RequestEventRecorder recorder, ConfigManager config) {
		this.rcvQ = rcvQ;
		this.sendQ = sendQ;
		this.rec = recorder;
		this.config = config;

		retryCache = buildRetryCache();
		l1Cache = buildL1Cache();
	}

	private ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> buildL1Cache() {
		Cache<String, ConcurrentMap<String, RequestEvent>> l1Cache = CacheBuilder.newBuilder() //
				.maximumSize(config.getMaxL1CacheSize()) //
				.build();
		return l1Cache.asMap();
	}

	private ConcurrentMap<String, RequestEvent> buildL2Cache() {
		Cache<String, RequestEvent> l2Cache = CacheBuilder.newBuilder() //
				.maximumSize(config.getMaxL2CacheSize())// 
				.build();
		return l2Cache.asMap();
	}
	
	private ConcurrentMap<String, RequestEvent> buildRetryCache() {
		Cache<String, RequestEvent> retryCache = CacheBuilder.newBuilder() //
				.maximumSize(config.getMaxRetryCacheSize())// 
				.build();
		return retryCache.asMap();
	}

	public RequestEvent findEvent(String uid, String urlDigest) {
		ConcurrentMap<String, RequestEvent> l2Cache = l1Cache.get(uid);
		if (l2Cache != null) {
			return l2Cache.get(urlDigest);
		} else {
			return null;
		}
	}

	ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> getL1Cache() {
		return l1Cache;
	}

	ConcurrentMap<String, RequestEvent> getRetryCache() {
		return retryCache;
	}

	private boolean isEventExpired(RequestEvent event) {
		return System.currentTimeMillis() > event.getTimestamp() + config.getEventExpireTime();
	}

	private void overrideEvent(RequestEvent curEvent, RequestEvent oldEvent) {
		oldEvent.setHop(curEvent.getHop());
		oldEvent.setRefererUrlDigest(curEvent.getRefererUrlDigest());
		oldEvent.setRequestId(curEvent.getRequestId());
		oldEvent.setTimestamp(curEvent.getTimestamp());
		oldEvent.setUrlDigest(curEvent.getUrlDigest());
	}

	private void processClientEvent(RequestEvent curEvent) {
		String userId = curEvent.getUserId();
		String refererUrlDigest = curEvent.getRefererUrlDigest();

		ConcurrentMap<String, RequestEvent> l2Cache = l1Cache.get(userId);
		if (refererUrlDigest != null) {
			if (l2Cache != null) {
				RequestEvent referEvent = l2Cache.get(refererUrlDigest);
				if (referEvent != null) {
					referEventFound(curEvent, referEvent);
				} else {
					referEventNotFound(curEvent);
				}
			} else {
				referEventNotFound(curEvent);
			}
		}

		// client event is also a server event
		curEvent.setHop(HOP_SERVER);
		processServerEvent(curEvent);

		if (!sendQ.offer(curEvent)) {
			logger.error(String.format("Send queue is full, can not send RequestEvent %s to other server", curEvent));
		}
	}

	private void processServerEvent(RequestEvent curEvent) {
		String userId = curEvent.getUserId();
		ConcurrentMap<String, RequestEvent> l2Cache = l1Cache.get(userId);
		String urlDigest = curEvent.getUrlDigest();

		RequestEvent retryingEvent = retryCache.remove(urlDigest);
		if (retryingEvent != null && !isEventExpired(retryingEvent)
				&& retryingEvent.getTimestamp() > curEvent.getTimestamp()) {
			referEventFound(retryingEvent, curEvent);
		}

		if (l2Cache == null) {
			l2Cache = buildL2Cache();
			l2Cache.put(urlDigest, curEvent);
			l1Cache.put(userId, l2Cache);
		} else {
			RequestEvent oldEvent = l2Cache.get(urlDigest);
			if (oldEvent == null) {
				l2Cache.put(urlDigest, curEvent);
			} else {
				if (curEvent.getTimestamp() > oldEvent.getTimestamp()) {
					overrideEvent(curEvent, oldEvent);
				} else {
					logger.info(String.format("RequestEvent %s received after %s", curEvent, oldEvent));
				}
			}
		}

	}

	private void referEventFound(RequestEvent curEvent, RequestEvent referEvent) {
		try {
			rec.recordEvent(curEvent, referEvent);
		} catch (Exception e) {
			logger.error(String.format("Can not record event %s refer to %s", curEvent, referEvent), e);
		}
	}

	private void referEventNotFound(RequestEvent curEvent) {
		retryCache.put(curEvent.getRefererUrlDigest(), curEvent);
	}

	public void start() {
		Threads.forGroup("Phoenix").start(new HandlerTask());
		Threads.forGroup("Phoenix").start(new RetryQueueCleanTask());
	}

	public void stop() {
		stop.set(true);
		Threads.forGroup("Phoenix").shutdown();
	}

}
