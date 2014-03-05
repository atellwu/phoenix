package com.dianping.phoenix.session.util;

import java.net.InetAddress;
import java.util.List;

import org.junit.Test;

public class NetworkUtilTest {

	@Test
	public void test() {
		List<InetAddress> ips = NetworkUtil.INSTANCE.getAllIp();
		for (InetAddress addr : ips) {
			System.out.println(addr.getHostName());
		}
	}

}
