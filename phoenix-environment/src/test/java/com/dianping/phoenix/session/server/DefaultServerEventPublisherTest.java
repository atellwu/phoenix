package com.dianping.phoenix.session.server;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.phoenix.session.RequestEvent;

public class DefaultServerEventPublisherTest extends ComponentTestCase {

	private DefaultEventPublisher publiser;

	@Before
	public void before() {
	}

	public static class FakeSocketClientManager implements SocketClientManager {

		private SocketClientManager target;

		public void openClients(List<InetSocketAddress> addrList) {
			target.openClients(addrList);
		}

		public void sendToClients(RequestEvent event) {
			target.sendToClients(event);
		}

		public void closeClients(List<InetSocketAddress> serverToClose) {
			target.closeClients(serverToClose);
		}

		public Set<InetSocketAddress> listClients() {
			return target.listClients();
		}

		public void setTarget(SocketClientManager target) {
			this.target = target;
		}

	}

	@Test
	public void shouldAddNewServer() throws Exception {

		super.defineComponent(SocketClientManager.class, FakeSocketClientManager.class);
		publiser = (DefaultEventPublisher) lookup(EventPublisher.class);
		FakeSocketClientManager clientMgr = (FakeSocketClientManager) lookup(SocketClientManager.class);

		final CountDownLatch latch = new CountDownLatch(1);

		clientMgr.setTarget(new SocketClientManager() {
			List<InetSocketAddress> clientList = new ArrayList<InetSocketAddress>();

			@Override
			public void sendToClients(RequestEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void openClients(List<InetSocketAddress> addrList) {
				clientList.addAll(addrList);
				latch.countDown();
			}

			@Override
			public Set<InetSocketAddress> listClients() {
				return new HashSet<InetSocketAddress>(clientList);
			}

			@Override
			public void closeClients(List<InetSocketAddress> serverToClose) {
				// TODO Auto-generated method stub

			}
		});

		InetSocketAddress[] addrs = new InetSocketAddress[2];
		for (int i = 0; i < addrs.length; i++) {
			addrs[i] = new InetSocketAddress("127.0.0.1", 9090 + i);
		}

		publiser.updateServerConnections(Arrays.asList(addrs));

		assertTrue(latch.await(1, TimeUnit.SECONDS));
		Set<InetSocketAddress> clients = clientMgr.listClients();
		assertEquals(2, clients.size());
		clients.removeAll(Arrays.asList(addrs));
		assertEquals(0, clients.size());

	}

	@Test
	public void shouldRemoveServer() throws Exception {

		super.defineComponent(SocketClientManager.class, FakeSocketClientManager.class);
		publiser = (DefaultEventPublisher) lookup(EventPublisher.class);
		FakeSocketClientManager clientMgr = (FakeSocketClientManager) lookup(SocketClientManager.class);

		final CountDownLatch latch = new CountDownLatch(2);

		clientMgr.setTarget(new SocketClientManager() {
			List<InetSocketAddress> clientList = new ArrayList<InetSocketAddress>();

			{
				clientList.add(new InetSocketAddress("127.0.0.1", 9090));
				clientList.add(new InetSocketAddress("127.0.0.2", 9090));
			}

			@Override
			public void sendToClients(RequestEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void openClients(List<InetSocketAddress> addrList) {
				clientList.addAll(addrList);
				latch.countDown();
			}

			@Override
			public Set<InetSocketAddress> listClients() {
				return new HashSet<InetSocketAddress>(clientList);
			}

			@Override
			public void closeClients(List<InetSocketAddress> serverToClose) {
				clientList.removeAll(serverToClose);
				latch.countDown();
			}
		});

		InetSocketAddress[] addrs = new InetSocketAddress[3];
		for (int i = 0; i < addrs.length; i++) {
			// 127.0.0.2/3/4
			addrs[i] = new InetSocketAddress("127.0.0." + (i + 2), 9090);
		}
		publiser.updateServerConnections(Arrays.asList(addrs));

		assertTrue(latch.await(1, TimeUnit.SECONDS));
		Set<InetSocketAddress> clients = clientMgr.listClients();
		// 127.0.0.2/3/4
		assertEquals(3, clients.size());
		clients.removeAll(Arrays.asList(addrs));
		assertEquals(0, clients.size());

	}

	@Test
	public void shouldPublishToAllServer() throws Exception {
		RequestEvent event = new RequestEvent();
		String phoenixId = UUID.randomUUID().toString();
		event.setPhoenixId(phoenixId);

		super.defineComponent(SocketClientManager.class, FakeSocketClientManager.class);
		publiser = (DefaultEventPublisher) lookup(EventPublisher.class);
		FakeSocketClientManager clientMgr = (FakeSocketClientManager) lookup(SocketClientManager.class);

		final CountDownLatch latch = new CountDownLatch(1);

		final AtomicReference<RequestEvent> sentEvent = new AtomicReference<RequestEvent>();

		clientMgr.setTarget(new SocketClientManager() {
			List<InetSocketAddress> clientList = new ArrayList<InetSocketAddress>();

			{
				clientList.add(new InetSocketAddress("127.0.0.1", 9090));
				clientList.add(new InetSocketAddress("127.0.0.2", 9090));
			}

			@Override
			public void sendToClients(RequestEvent event) {
				sentEvent.set(event);
				latch.countDown();
			}

			@Override
			public void openClients(List<InetSocketAddress> addrList) {
				clientList.addAll(addrList);
				latch.countDown();
			}

			@Override
			public Set<InetSocketAddress> listClients() {
				return new HashSet<InetSocketAddress>(clientList);
			}

			@Override
			public void closeClients(List<InetSocketAddress> serverToClose) {
				clientList.removeAll(serverToClose);
				latch.countDown();
			}
		});

		publiser.publish(event);

		assertTrue(latch.await(1, TimeUnit.SECONDS));
		assertEquals(event.getPhoenixId(), sentEvent.get().getPhoenixId());
	}

}
