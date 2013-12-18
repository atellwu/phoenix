package com.dianping.phoenix.session.requestid.serverevent;

import java.net.InetSocketAddress;
import java.util.List;

public interface ServerAddressManager {

	public interface AddressChangeListener {
		public void onAddressChange(List<InetSocketAddress> newAddr);
	}

	public List<InetSocketAddress> getServerList(AddressChangeListener listener);

}
