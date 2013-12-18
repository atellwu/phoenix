package com.dianping.phoenix.session.requestid.serverevent;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import com.dianping.phoenix.session.RequestEvent;

public interface SocketClientManager {

	void openClients(List<InetSocketAddress> addrList);

	void sendToClients(RequestEvent event);

	void closeClients(List<InetSocketAddress> serverToClose);
	
	Set<InetSocketAddress> listClients();

}
