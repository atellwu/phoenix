package com.dianping.phoenix.session.requestid.serverevent;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class DefaultServerAddressManagerTest extends ComponentTestCase {

	private DefaultServerAddressManager mgr;

	public static class FakeServerAddressManager extends DefaultServerAddressManager {
		@Override
		List<InetAddress> getLocalIps() {
			List<InetAddress> ips = new ArrayList<InetAddress>();
			try {
				ips.add(InetAddress.getByName("10.1.1.1"));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			return ips;
		}
	}

	@Test
	public void shouldIgnoreLocalIp() throws Exception {
		super.defineComponent(ServerAddressManager.class, null, FakeServerAddressManager.class);

		mgr = (DefaultServerAddressManager) lookup(ServerAddressManager.class);

		List<InetSocketAddress> svrList = mgr.getServerList(null);
		assertEquals(0, svrList.size());

		mgr.updateServerList("10.1.1.1:9999, 10.1.1.2:8888, 10.1.1.1:7777, 10.1.1.3:6666");
		svrList = mgr.getServerList(null);
		assertEquals(2, svrList.size());
		assertEquals(new InetSocketAddress("10.1.1.2", 8888), svrList.get(0));
		assertEquals(new InetSocketAddress("10.1.1.3", 6666), svrList.get(1));

	}

}
