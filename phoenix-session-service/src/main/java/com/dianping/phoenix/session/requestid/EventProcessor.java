package com.dianping.phoenix.session.requestid;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class EventProcessor extends ContainerHolder implements Initializable, LogEnabled {
	public final static int HOP_CLIENT = 0;

	public final static int HOP_SERVER = 1;

	@Inject
	EventDelegateManager eventDelegateMgr;

	@Inject
	private EventRecorder m_recorder;

	@Inject
	private ConfigManager m_config;

	private RequestEventDelegate m_rcvQ;

	private RequestEventDelegate m_sendQ;

	private ConcurrentMap<String, RequestEvent> m_retryCache;

	private ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> m_l1Cache;

	private AtomicBoolean m_stop = new AtomicBoolean();

	private Logger m_logger;

	private BlockingQueue<RequestEvent>[] m_handlerTaskQueues;

	private ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> buildL1Cache() {
		Cache<String, ConcurrentMap<String, RequestEvent>> l1Cache = CacheBuilder.newBuilder() //
		      .maximumSize(m_config.getMaxL1CacheSize()) //
		      .build();
		return l1Cache.asMap();
	}

	private ConcurrentMap<String, RequestEvent> buildL2Cache() {
		Cache<String, RequestEvent> l2Cache = CacheBuilder.newBuilder() //
		      .maximumSize(m_config.getMaxL2CacheSize())//
		      .build();
		return l2Cache.asMap();
	}

	private ConcurrentMap<String, RequestEvent> buildRetryCache() {
		Cache<String, RequestEvent> retryCache = CacheBuilder.newBuilder() //
		      .maximumSize(m_config.getMaxRetryCacheSize())//
		      .build();
		return retryCache.asMap();
	}

	private RequestEvent cloneToServerEvent(RequestEvent event) {
		RequestEvent svrEvent = null;
		try {
			svrEvent = event.clone();
		} catch (CloneNotSupportedException e) {
			// won't happen
		}
		svrEvent.setHop(HOP_SERVER);
		return svrEvent;
	}

	private void dispatchEvent(RequestEvent event) {
		switch (event.getHop()) {
		case HOP_CLIENT:
			if (isEventExpired(event)) {
				m_logger.info(String.format(
				      "Receive expired RequestEvent %s from client, will not calculate refer request id", event));
			} else {
				if (event.getRefererUrlDigest() != null) {
					offerToHandlerTaskQueue(event.getRefererUrlDigest(), event);
				}
			}

			RequestEvent svrEvent = cloneToServerEvent(event);

			if (!m_sendQ.offer(svrEvent)) {
				m_logger.error(String.format("Send queue is full, can not send RequestEvent %s to other server", svrEvent));
			}

			offerToHandlerTaskQueue(event.getUrlDigest(), svrEvent);
			break;

		case HOP_SERVER:
			offerToHandlerTaskQueue(event.getUrlDigest(), event);
			break;

		default:
			m_logger.error(String.format("Unknown hop %d received, will ignore", event.getHop()));
			break;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public RequestEvent findEvent(String uid, String urlDigest) {
		ConcurrentMap<String, RequestEvent> l2Cache = m_l1Cache.get(uid);
		if (l2Cache != null) {
			return l2Cache.get(urlDigest);
		} else {
			return null;
		}
	}

	ConcurrentMap<String, ConcurrentMap<String, RequestEvent>> getL1Cache() {
		return m_l1Cache;
	}

	ConcurrentMap<String, RequestEvent> getRetryCache() {
		return m_retryCache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() throws InitializationException {
		m_rcvQ = eventDelegateMgr.getIn();
		m_sendQ = eventDelegateMgr.getOut();
		m_retryCache = buildRetryCache();
		m_l1Cache = buildL1Cache();

		m_handlerTaskQueues = new BlockingQueue[m_config.getHandlerTasksThreads()];
		for (int i = 0; i < m_handlerTaskQueues.length; i++) {
			m_handlerTaskQueues[i] = new LinkedBlockingQueue<RequestEvent>(m_config.getHandlerTaskQueueCapacity());
		}

	}

	private boolean isEventExpired(RequestEvent event) {
		return System.currentTimeMillis() > event.getTimestamp() + m_config.getEventExpireTime();
	}

	private void offerToHandlerTaskQueue(String digest, RequestEvent event) {
		if (!m_handlerTaskQueues[slotForUrl(digest)].offer(event)) {
			m_logger.warn(String.format("Handler task queue is full, will discad %s", event));
		}
	}

	private void overrideEvent(RequestEvent curEvent, RequestEvent oldEvent) {
		oldEvent.setHop(curEvent.getHop());
		oldEvent.setRefererUrlDigest(curEvent.getRefererUrlDigest());
		oldEvent.setRequestId(curEvent.getRequestId());
		oldEvent.setTimestamp(curEvent.getTimestamp());
		oldEvent.setUrlDigest(curEvent.getUrlDigest());
	}

	private void processClientEvent(RequestEvent curEvent) {
		String userId = curEvent.getPhoenixId();
		String refererUrlDigest = curEvent.getRefererUrlDigest();

		ConcurrentMap<String, RequestEvent> l2Cache = m_l1Cache.get(userId);
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
	}

	private void processEvent(RequestEvent event) {
		switch (event.getHop()) {
		case HOP_CLIENT:
			processClientEvent(event);
			break;

		case HOP_SERVER:
			processServerEvent(event);
			break;

		default:
			m_logger.error(String.format("Unknown hop %d received, will ignore", event.getHop()));
			break;
		}
	}

	private void processServerEvent(RequestEvent curEvent) {
		String userId = curEvent.getPhoenixId();
		ConcurrentMap<String, RequestEvent> l2Cache = m_l1Cache.get(userId);
		String urlDigest = curEvent.getUrlDigest();

		RequestEvent retryingEvent = m_retryCache.remove(urlDigest);
		if (retryingEvent != null && !isEventExpired(retryingEvent)
		      && retryingEvent.getTimestamp() > curEvent.getTimestamp()) {
			referEventFound(retryingEvent, curEvent);
		}

		if (l2Cache == null) {
			l2Cache = buildL2Cache();
			l2Cache.put(urlDigest, curEvent);
			m_l1Cache.put(userId, l2Cache);
		} else {
			RequestEvent oldEvent = l2Cache.get(urlDigest);
			if (oldEvent == null) {
				l2Cache.put(urlDigest, curEvent);
			} else {
				if (curEvent.getTimestamp() > oldEvent.getTimestamp()) {
					overrideEvent(curEvent, oldEvent);
				} else {
					m_logger.info(String.format("RequestEvent %s received after %s", curEvent, oldEvent));
				}
			}
		}

	}

	private void referEventFound(RequestEvent curEvent, RequestEvent referEvent) {
		try {
			m_recorder.recordEvent(curEvent, referEvent);
		} catch (Exception e) {
			m_logger.error(String.format("Can not record event %s refer to %s", curEvent, referEvent), e);
		}
	}

	private void referEventNotFound(RequestEvent curEvent) {
		m_retryCache.put(curEvent.getRefererUrlDigest(), curEvent);
	}

	// for unit test only
	public void setConfig(ConfigManager config) {
		m_config = config;
	}

	// for unit test only
	public void setRcvQ(RequestEventDelegate rcvQ) {
		m_rcvQ = rcvQ;
	}

	// for unit test only
	public void setRecorder(EventRecorder recorder) {
		m_recorder = recorder;
	}

	private int slotForUrl(String urlDigest) {
		if (urlDigest != null) {
			return urlDigest.charAt(urlDigest.length() - 1) % m_handlerTaskQueues.length;
		} else {
			return 0;
		}
	}

	public void start() {
		String group = "Phoenix";
		for (int i = 0; i < m_handlerTaskQueues.length; i++) {
			Threads.forGroup(group).start(new HandlerTask(m_handlerTaskQueues[i]));
		}
		Threads.forGroup(group).start(new RetryQueueCleanTask());
		Threads.forGroup(group).start(new DispatchTask(), false);
	}

	public void stop() {
		m_stop.set(true);
		Threads.forGroup("Phoenix").shutdown();
	}

	private class DispatchTask implements Task {

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		private boolean isValidEvent(RequestEvent event) {
			return event.getRequestId() != null //
			      && event.getUrlDigest() != null //
			      && event.getPhoenixId() != null;
		}

		@Override
		public void run() {
			while (!m_stop.get()) {
				RequestEvent event = null;
				try {
					event = m_rcvQ.take();
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					return;
				}

				m_logger.info("Receiving " + event);

				if (!isValidEvent(event)) {
					m_logger.warn(String.format("Invalid RequetEvent %s received, will ignore", event));
					continue;
				}

				dispatchEvent(event);
			}
		}

		@Override
		public void shutdown() {
		}

	}

	private class HandlerTask implements Task {

		private BlockingQueue<RequestEvent> eventQ;

		public HandlerTask(BlockingQueue<RequestEvent> eventQ) {
			this.eventQ = eventQ;
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!m_stop.get()) {
				RequestEvent event = null;
				try {
					event = eventQ.take();
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					return;
				}

				m_logger.info("Processing " + event.getRequestId());

				processEvent(event);
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
			while (!m_stop.get()) {
				if (m_retryCache.size() > m_config.getRetryQueueSafeLength()) {
					for (Map.Entry<String, RequestEvent> entry : m_retryCache.entrySet()) {
						RequestEvent event = entry.getValue();

						if (System.currentTimeMillis() > event.getTimestamp() + m_config.getEventExpireTime()) {
							m_logger.warn(String.format("RequestEvent %s is expired", event));
							m_retryCache.remove(entry.getKey());
						}
					}
				}
				try {
					Thread.sleep(m_config.getRetryQueueCleanInterval());
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
