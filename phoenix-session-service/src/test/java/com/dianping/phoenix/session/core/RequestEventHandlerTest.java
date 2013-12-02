package com.dianping.phoenix.session.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;

public class RequestEventHandlerTest extends ComponentTestCase {

	private RequestEventHandler handler;
	private RequestEventDelegate rcvQ;

	String uid = "uid";
	String svrEventUrlDigest;
	String clientEventUrlDigest;

	RequestEvent svrEvent1;
	RequestEvent svrEvent2;
	RequestEvent clientEvent1;
	RequestEvent clientEventRTSvrEvent1;
	RequestEvent clientEventRTClientEvent1;
	RequestEvent clientEventRTNothing;

	@Before
	public void before() throws Exception {
		rcvQ = lookup(RequestEventDelegate.class);
		handler = lookup(RequestEventHandler.class);
		handler.setRecorder(new DummyRecorder());
		handler.setRcvQ(rcvQ);

		Random rnd = new Random(System.currentTimeMillis());

		uid = "uid";
		svrEventUrlDigest = "svrEventUrlDigest";
		clientEventUrlDigest = "clientEventUrlDigest";

		svrEvent1 = new RequestEvent();
		svrEvent1.setHop(RequestEventHandler.HOP_SERVER);
		svrEvent1.setRequestId("svrEvent1");
		svrEvent1.setTimestamp(System.currentTimeMillis());
		svrEvent1.setUrlDigest(svrEventUrlDigest);
		svrEvent1.setUserId(uid);

		svrEvent2 = new RequestEvent();
		svrEvent2.setHop(RequestEventHandler.HOP_SERVER);
		svrEvent2.setRequestId("svrEvent2");
		svrEvent2.setTimestamp(System.currentTimeMillis() + 1);
		svrEvent2.setUrlDigest(svrEventUrlDigest);
		svrEvent2.setUserId(uid);

		clientEvent1 = new RequestEvent();
		clientEvent1.setHop(RequestEventHandler.HOP_CLIENT);
		clientEvent1.setRequestId("clientEvent1");
		clientEvent1.setTimestamp(System.currentTimeMillis());
		clientEvent1.setUrlDigest(clientEventUrlDigest);
		clientEvent1.setUserId(uid);

		clientEventRTSvrEvent1 = new RequestEvent();
		clientEventRTSvrEvent1.setHop(RequestEventHandler.HOP_CLIENT);
		clientEventRTSvrEvent1.setRefererUrlDigest(svrEventUrlDigest);
		clientEventRTSvrEvent1.setRequestId("referToServerEvent1");
		clientEventRTSvrEvent1.setTimestamp(System.currentTimeMillis() + 2);
		clientEventRTSvrEvent1.setUrlDigest("any" + +rnd.nextLong());
		clientEventRTSvrEvent1.setUserId(uid);

		clientEventRTClientEvent1 = new RequestEvent();
		clientEventRTClientEvent1.setHop(RequestEventHandler.HOP_CLIENT);
		clientEventRTClientEvent1.setRefererUrlDigest(clientEventUrlDigest);
		clientEventRTClientEvent1.setRequestId("referToClientEvent1");
		clientEventRTClientEvent1.setTimestamp(System.currentTimeMillis() + 3);
		clientEventRTClientEvent1.setUrlDigest("any" + +rnd.nextLong());
		clientEventRTClientEvent1.setUserId(uid);

		clientEventRTNothing = new RequestEvent();
		clientEventRTNothing.setHop(RequestEventHandler.HOP_CLIENT);
		clientEventRTNothing.setRefererUrlDigest("not exists");
		clientEventRTNothing.setRequestId("clientEventRTNothing");
		clientEventRTNothing.setTimestamp(System.currentTimeMillis());
		clientEventRTNothing.setUrlDigest("any" + +rnd.nextLong());
		clientEventRTNothing.setUserId(uid);

	}

	@After
	public void after() {
		handler.stop();
	}

	@Test
	public void newServerEventShouldReplaceOldEvent() throws Exception {
		handler.start();

		rcvQ.offer(svrEvent1);
		rcvQ.offer(svrEvent2);

		long start = System.currentTimeMillis();
		while (rcvQ.size() > 0 && System.currentTimeMillis() - start < 1000) {
			Thread.sleep(1);
		}
		Thread.sleep(100);
		RequestEvent eventNow = handler.findEvent(uid, svrEventUrlDigest);

		assertEquals(svrEvent2, eventNow);
		assertEquals(1, handler.getL1Cache().get(uid).size());

	}

	@Test
	public void oldServerEventShouldNotReplaceNewEvent() throws Exception {
		handler.start();

		rcvQ.offer(svrEvent2);
		rcvQ.offer(svrEvent1);

		long start = System.currentTimeMillis();
		while (rcvQ.size() > 0 && System.currentTimeMillis() - start < 1000) {
			Thread.sleep(1);
		}
		Thread.sleep(100);
		RequestEvent eventNow = handler.findEvent(uid, svrEventUrlDigest);

		assertEquals(svrEvent2, eventNow);
		assertEquals(1, handler.getL1Cache().get(uid).size());

	}

	@Test
	public void shouldRecordClientEventReferToSvrEvent() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		handler.setRecorder(new RequestEventRecorder() {

			@Override
			public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
				assertEquals(clientEventRTSvrEvent1, curEvent);
				assertEquals(svrEvent2, referToEvent);
				latch.countDown();
			}
		});
		handler.start();

		rcvQ.offer(svrEvent1);
		rcvQ.offer(svrEvent2);
		rcvQ.offer(clientEventRTSvrEvent1);

		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void shouldRecordClientEventReferToClientEvent() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		handler.setRecorder(new RequestEventRecorder() {

			@Override
			public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
				assertEquals(clientEventRTClientEvent1, curEvent);
				assertEquals(clientEvent1, referToEvent);
				latch.countDown();
			}
		});
		handler.start();

		rcvQ.offer(clientEvent1);
		rcvQ.offer(clientEventRTClientEvent1);

		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void shouldNotRecordClientEventReferToNothing() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		handler.setRecorder(new RequestEventRecorder() {

			@Override
			public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
				assertEquals(clientEventRTNothing, curEvent);
				assertEquals(clientEvent1, referToEvent);
				latch.countDown();
			}
		});
		handler.start();

		rcvQ.offer(clientEvent1);
		rcvQ.offer(clientEventRTNothing);

		assertFalse(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void shouldRetryRecordClientEventReferToClientEvent() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		handler.setRecorder(new RequestEventRecorder() {

			@Override
			public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
				assertEquals(clientEventRTClientEvent1, curEvent);
				assertEquals(clientEvent1, referToEvent);
				latch.countDown();
			}
		});
		handler.start();

		rcvQ.offer(clientEventRTClientEvent1);
		assertFalse(latch.await(1, TimeUnit.SECONDS));

		rcvQ.offer(clientEvent1);
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void shouldExpireClientEvent() throws Exception {
		ConfigManager config = new ConfigManager() {
			
			{
				initialize();
			}
			
			@Override
			public int getRetryQueueSafeLength() {
				return 0;
			}

			@Override
			public int getEventExpireTime() {
				return 50;
			}

		};
		handler.setConfig(config);
		handler.start();

		assertEquals(0, handler.getRetryCache().size());
		Thread.sleep(config.getRetryQueueCleanInterval() / 8);
		clientEventRTClientEvent1.setTimestamp(System.currentTimeMillis());
		rcvQ.offer(clientEventRTClientEvent1);
		Thread.sleep(config.getRetryQueueCleanInterval() / 8);
		assertEquals(1, handler.getRetryCache().size());
		
		clientEventRTSvrEvent1.setTimestamp(System.currentTimeMillis());
		rcvQ.offer(clientEventRTSvrEvent1);
		
		Thread.sleep(config.getRetryQueueCleanInterval());
		assertEquals(0, handler.getRetryCache().size());
	}

}
