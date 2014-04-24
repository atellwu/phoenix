package com.dianping.phoenix.session.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.server.ServerAddressManager.AddressChangeListener;

public class DefaultEventPublisher implements Initializable, LogEnabled, EventPublisher {

	@Inject
	private ServerAddressManager m_addrMgr;

	@Inject
	private SocketClientManager m_clientMgr;

	private AddressChangeListener m_listener;

	private AtomicBoolean m_stop = new AtomicBoolean(false);

	private Logger m_logger;

	private AtomicBoolean m_serverListUpdated = new AtomicBoolean(false);

	private AtomicReference<List<InetSocketAddress>> m_newAddrList = new AtomicReference<List<InetSocketAddress>>();

	private RequestEventDelegate m_out;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {

		m_listener = new AddressChangeListener() {

			@Override
			public void onAddressChange(List<InetSocketAddress> newAddr) {
				m_newAddrList.set(newAddr);
				m_serverListUpdated.set(true);
			}
		};

	}

	@Override
	public void publish(RequestEvent event) {
		m_clientMgr.sendToClients(event);
	}

	public void setAddrMgr(ServerAddressManager addrMgr) {
		m_addrMgr = addrMgr;
	}

	public void start(RequestEventDelegate delegate) {

		m_out = delegate;

		List<InetSocketAddress> addrList = m_addrMgr.getServerList(m_listener);
		m_clientMgr.openClients(addrList);

		Threads.forGroup("Phoenix").start(new PublishTask());

		Threads.forGroup("Phoenix").start(new ServerListUpdateTask());
	}

	void updateServerConnections(List<InetSocketAddress> newAddrList) {

		Set<InetSocketAddress> currentClients = m_clientMgr.listClients();

		// open connection to newly added server
		List<InetSocketAddress> clientsToOpen = new ArrayList<InetSocketAddress>();
		for (InetSocketAddress newAddr : newAddrList) {
			if (!currentClients.contains(newAddr)) {
				clientsToOpen.add(newAddr);
			}
		}
		m_clientMgr.openClients(clientsToOpen);

		// close connection to removed server
		List<InetSocketAddress> clientsToClose = new ArrayList<InetSocketAddress>();
		for (InetSocketAddress currentClient : currentClients) {
			if (!newAddrList.contains(currentClient)) {
				clientsToClose.add(currentClient);
			}
		}
		m_clientMgr.closeClients(clientsToClose);
	}

	class PublishTask implements Task {

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			try {
				while (!m_stop.get()) {
					RequestEvent event = m_out.take();
					try {
						publish(event);
					} catch (RuntimeException e) {
						m_logger.error("Error publish event " + event.getRequestId(), e);
					}
				}
			} catch (InterruptedException e) {
				m_logger.info("Thread Interrupted, will exit");
			}
		}

		@Override
		public void shutdown() {
			m_stop.set(true);
		}

	}

	class ServerListUpdateTask implements Task {

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!m_stop.get()) {
				if (m_serverListUpdated.compareAndSet(true, false)) {
					try {
						updateServerConnections(m_newAddrList.get());
					} catch (Exception e) {
						m_logger.error("Error update server connections", e);
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						m_logger.info("Thread Interrupted, will exit");
						break;
					}
				}
			}
		}

		@Override
		public void shutdown() {
			m_stop.set(true);
		}

	}

}
