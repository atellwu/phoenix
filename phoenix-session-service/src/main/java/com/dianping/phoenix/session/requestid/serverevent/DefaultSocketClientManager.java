package com.dianping.phoenix.session.requestid.serverevent;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;
import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketClient;
import org.unidal.tuple.Pair;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;

public class DefaultSocketClientManager extends ContainerHolder implements SocketClientManager, LogEnabled {

	private ConcurrentMap<InetSocketAddress, Pair<RequestEventDelegate, SocketClient>> m_addrMap = new ConcurrentHashMap<InetSocketAddress, Pair<RequestEventDelegate, SocketClient>>();

	private Logger m_logger;

	@Override
	public void openClients(List<InetSocketAddress> clientsToOpen) {
		m_logger.info("Open clients to " + clientsToOpen);
		for (InetSocketAddress addr : clientsToOpen) {
			Pair<RequestEventDelegate, SocketClient> pair = createClient(addr);
			m_addrMap.put(addr, pair);
		}
	}

	@Override
	public void sendToClients(RequestEvent event) {
		m_logger.info(String.format("Sent %s clients to clients", event.getRequestId()));
		for (Pair<RequestEventDelegate, SocketClient> pair : m_addrMap.values()) {
			pair.getKey().offer(event);
		}
	}

	@Override
	public void closeClients(List<InetSocketAddress> clientsToClose) {
		m_logger.info("Close clients to " + clientsToClose);
		for (InetSocketAddress addr : clientsToClose) {
			m_addrMap.get(addr).getValue().shutdown();
			m_addrMap.remove(addr);
		}
	}

	private Pair<RequestEventDelegate, SocketClient> createClient(InetSocketAddress addr) {
		RequestEventDelegate delegate = lookup(RequestEventDelegate.class);

		SocketClient client = Sockets.forClient() //
		      .threads(getClass().getSimpleName(), 0) //
		      .connectTo(addr.getPort(), addr.getAddress().getHostAddress()) //
		      .start(delegate);

		Pair<RequestEventDelegate, SocketClient> pair = new Pair<RequestEventDelegate, Sockets.SocketClient>(delegate,
		      client);
		return pair;
	}

	@Override
	public Set<InetSocketAddress> listClients() {
		return m_addrMap.keySet();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
