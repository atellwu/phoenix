package com.dianping.phoenix.session.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum NetworkUtil {

	INSTANCE;

	public List<InetAddress> getAllIp() {
		List<InetAddress> addrList = new ArrayList<InetAddress>();
		ArrayList<NetworkInterface> nicList = null;
		try {
			nicList = Collections.list(NetworkInterface.getNetworkInterfaces());
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (nicList != null) {
			for (NetworkInterface ifc : nicList) {
				try {
					if (ifc.isUp()) {
						for (InetAddress addr : Collections.list(ifc.getInetAddresses())) {
							addrList.add(addr);
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
					continue;
				}
			}
		}

		return addrList;

	}

}
