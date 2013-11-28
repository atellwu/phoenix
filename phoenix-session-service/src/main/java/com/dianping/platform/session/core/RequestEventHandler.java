package com.dianping.platform.session.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.session.RequestEvent;

public class RequestEventHandler {

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

	private ConcurrentHashMap<String, RequestEvent> retryMap;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, RequestEvent>> l1Map;
	private AtomicBoolean stop = new AtomicBoolean();

	public RequestEventHandler() {
		l1Map = new ConcurrentHashMap<String, ConcurrentHashMap<String, RequestEvent>>();
		retryMap = new ConcurrentHashMap<String, RequestEvent>();
	}

	public RequestEventHandler(BlockingQueue<RequestEvent> rcvQ, BlockingQueue<RequestEvent> sendQ,
			RequestEventRecorder recorder) {
		this();
		this.rcvQ = rcvQ;
		this.sendQ = sendQ;
		this.rec = recorder;
		config = new ConfigManager();
	}

	public RequestEvent findEvent(String uid, String urlDigest) {
		ConcurrentHashMap<String, RequestEvent> l2Map = l1Map.get(uid);
		if (l2Map != null) {
			return l2Map.get(urlDigest);
		} else {
			return null;
		}
	}

	ConcurrentHashMap<String, ConcurrentHashMap<String, RequestEvent>> getL1Map() {
		return l1Map;
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

		ConcurrentHashMap<String, RequestEvent> l2Map = l1Map.get(userId);
		if (refererUrlDigest != null) {
			if (l2Map != null) {
				RequestEvent referEvent = l2Map.get(refererUrlDigest);
				if (referEvent != null) {
					referEventFound(curEvent, referEvent);
				} else {
					referEventNotFound(curEvent);
				}
			} else {
				referEventNotFound(curEvent);
			}
		}

		// client event should also be stored in map
		curEvent.setHop(HOP_SERVER);
		processServerEvent(curEvent);

		if (!sendQ.offer(curEvent)) {
			// TODO
			throw new RuntimeException("send q is full");
		}
	}

	private void processServerEvent(RequestEvent curEvent) {
		String userId = curEvent.getUserId();
		ConcurrentHashMap<String, RequestEvent> l2Map = l1Map.get(userId);
		String urlDigest = curEvent.getUrlDigest();

		RequestEvent retryingEvent = retryMap.remove(urlDigest);
		if (retryingEvent != null && retryingEvent.getTimestamp() > curEvent.getTimestamp()) {
			referEventFound(retryingEvent, curEvent);
		}

		if (l2Map == null) {
			l2Map = new ConcurrentHashMap<String, RequestEvent>();
			l2Map.put(urlDigest, curEvent);
			l1Map.put(userId, l2Map);
		} else {
			RequestEvent oldEvent = l2Map.get(urlDigest);
			if (oldEvent == null) {
				l2Map.put(urlDigest, curEvent);
			} else {
				if (curEvent.getTimestamp() > oldEvent.getTimestamp()) {
					overrideEvent(curEvent, oldEvent);
				} else {
					// TODO
				}
			}
		}

	}

	private void referEventFound(RequestEvent curEvent, RequestEvent referEvent) {
		rec.recordEvent(curEvent, referEvent);
	}

	private void referEventNotFound(RequestEvent curEvent) {
		retryMap.put(curEvent.getRefererUrlDigest(), curEvent);
	}

	public void start() {

		Threads.forGroup("Phoenix-RequestEventHandler").start(new Task() {

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void run() {
				while (!stop.get()) {
					RequestEvent event = null;
					try {
						event = rcvQ.take();
					} catch (InterruptedException e) {
						// TODO
						return;
					}
					switch (event.getHop()) {
					case HOP_CLIENT:
						processClientEvent(event);
						break;

					case HOP_SERVER:
						processServerEvent(event);
						break;

					default:
						// TODO
						break;
					}
				}
			}

			@Override
			public void shutdown() {
				// TODO Auto-generated method stub

			}
		});

		Threads.forGroup("Phoenix-RequestEventRetryQueueCleaner").start(new Task() {

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void run() {
				while (!stop.get()) {
					for (Map.Entry<String, RequestEvent> entry : retryMap.entrySet()) {
						RequestEvent event = entry.getValue();
						
						if (event.getTimestamp() + config.getEventExpireTime() > System.currentTimeMillis()) {
							// TODO log expire
							retryMap.remove(entry.getKey());
							System.out.println("expire " + event);
						}
					}
					try {
						Thread.sleep(config.getRetryQueueCleanInterval());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void shutdown() {
				// TODO Auto-generated method stub

			}

		});

	}

	public void stop() {
		stop.set(true);
	}

}
