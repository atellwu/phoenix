package com.dianping.phoenix.session.server;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketClient;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;

public class DefaultSocketClientManager extends ContainerHolder implements SocketClientManager, LogEnabled,
      Initializable {

	private ConcurrentMap<InetSocketAddress, SocketClient> m_addrMap = new ConcurrentHashMap<InetSocketAddress, SocketClient>();

	private Logger m_logger;

	private String m_strMode;

	private Mode m_mode;

	private RequestEventDelegate m_singleDelegate;

	@Override
	public void openClients(List<InetSocketAddress> clientsToOpen) {
		for (InetSocketAddress addr : clientsToOpen) {
			m_logger.info("Open client to " + addr);
			SocketClient client = createClient(addr);
			m_addrMap.put(addr, client);
		}

	}

	@Override
	public void sendToClients(RequestEvent event) {
		if (m_mode == Mode.Broadcast) {
			for (Map.Entry<InetSocketAddress, SocketClient> entry : m_addrMap.entrySet()) {
				RequestEventDelegate delegate = (RequestEventDelegate) entry.getValue().getMessageDelegate();
				delegate.offer(event);
			}
		} else {
			m_singleDelegate.offer(event);
		}
	}

	@Override
	public void closeClients(List<InetSocketAddress> clientsToClose) {
		for (InetSocketAddress addr : clientsToClose) {
			m_logger.info("Close clients to " + addr);
			m_addrMap.get(addr).shutdown();
			m_addrMap.remove(addr);
		}

	}

	private SocketClient createClient(InetSocketAddress addr) {
		RequestEventDelegate delegate = null;
		if (m_mode == Mode.Broadcast) {
			delegate = lookup(RequestEventDelegate.class);
		} else {
			delegate = m_singleDelegate;
		}

		SocketClient client = Sockets.forClient() //
		      .threads(getClass().getSimpleName(), 0) //
		      .connectTo(addr.getPort(), addr.getAddress().getHostAddress()) //
		      .start(delegate);

		return client;
	}

	@Override
	public Set<InetSocketAddress> listClients() {
		return m_addrMap.keySet();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_mode = Mode.valueOf(m_strMode);

		if (m_mode == Mode.Single) {
			m_singleDelegate = lookup(RequestEventDelegate.class);
		}
	}

}
