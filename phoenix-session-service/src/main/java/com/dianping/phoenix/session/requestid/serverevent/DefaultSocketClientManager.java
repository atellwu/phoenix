package com.dianping.phoenix.session.requestid.serverevent;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketClient;
import org.unidal.tuple.Pair;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.RequestEventDelegate;

public class DefaultSocketClientManager implements SocketClientManager {

	private ConcurrentMap<InetSocketAddress, Pair<RequestEventDelegate, SocketClient>> m_addrMap = new ConcurrentHashMap<InetSocketAddress, Pair<RequestEventDelegate, SocketClient>>();

	@Override
	public void openClients(List<InetSocketAddress> clientsToOpen) {
		for (InetSocketAddress addr : clientsToOpen) {
			Pair<RequestEventDelegate, SocketClient> pair = createClient(addr);
			m_addrMap.put(addr, pair);
		}
	}

	@Override
	public void sendToClients(RequestEvent event) {
		for (Pair<RequestEventDelegate, SocketClient> pair : m_addrMap.values()) {
			pair.getKey().offer(event);
		}
	}

	@Override
	public void closeClients(List<InetSocketAddress> clientsToClose) {
		for (InetSocketAddress addr : clientsToClose) {
			m_addrMap.get(addr).getValue().shutdown();
			m_addrMap.remove(addr);
		}
	}

	private Pair<RequestEventDelegate, SocketClient> createClient(InetSocketAddress addr) {
		RequestEventDelegate delegate = new RequestEventDelegate();

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

}
