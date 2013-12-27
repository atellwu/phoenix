package com.dianping.phoenix.session.requestid.serverevent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.server.ServerAddressManager;
import com.dianping.phoenix.session.util.NetworkUtil;

public class DefaultServerAddressManager implements ServerAddressManager, Initializable, LogEnabled {
	
	public final static String LION_KEY_SERVER_ADDRESS = "session-service.server.address";

	private String m_serverString;

	@Inject
	private ConfigManager m_config;

	private Logger m_logger;

	private List<InetSocketAddress> m_serverList = new ArrayList<InetSocketAddress>();

	private List<AddressChangeListener> m_listeners = Collections
	      .synchronizedList(new ArrayList<ServerAddressManager.AddressChangeListener>());

	private Set<InetAddress> m_localIps = new HashSet<InetAddress>();
	
	private ConfigCache lion;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	List<InetAddress> getLocalIps() {
		return NetworkUtil.INSTANCE.getAllIp();
	}

	@Override
	public List<InetSocketAddress> getServerList(AddressChangeListener listener) {
		if (listener != null && !m_listeners.contains(listener)) {
			m_listeners.add(listener);
		}
		return m_serverList;
	}

	@Override
	public void initialize() throws InitializationException {
		m_localIps.addAll(getLocalIps());
		m_logger.info("Local ips " + m_localIps);
		
		String serverAddr = null;
		try {
			serverAddr = getConfigFromLion();
		} catch (LionException e) {
			m_logger.error("Error get config from lion", e);
			throw new RuntimeException(e);
		}

		updateServerList(serverAddr);		
		
	}
	
	private String getConfigFromLion() throws LionException {
		lion = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());

		String serverAddr = null;
		serverAddr = lion.getProperty(LION_KEY_SERVER_ADDRESS);
		if (serverAddr == null) {
			m_logger.error("Error get server address from lion");
			throw new RuntimeException("Can not get server address from lion");
		}
		lion.addChange(new ConfigChange() {

			@Override
			public void onChange(String key, String value) {
				if (LION_KEY_SERVER_ADDRESS.equals(key)) {
					updateServerList(value);
				}
			}
		});
		return serverAddr;
	}

	private void notifyListeners() {
		for (AddressChangeListener listener : m_listeners) {
			listener.onAddressChange(m_serverList);
		}
	}

	private boolean parseServerString(String newServerString, List<InetSocketAddress> newServerList) {
		boolean success = true;

		if (!StringUtils.isEmpty(newServerString)) {

			String[] servers = newServerString.split(",");
			try {
				for (int i = 0; i < servers.length; i++) {
					String[] hostPort = servers[i].split(":");
					String host = hostPort[0].trim();
					int port = Integer.parseInt(hostPort[1].trim());
					if (!isThisServer(host, port)) {
						newServerList.add(new InetSocketAddress(host, port));
					}
				}
			} catch (Exception e) {
				success = false;
				m_logger.error(String.format("Invalid server list %s", newServerList), e);
			}
		} else {
			success = false;
		}

		return success;
	}

	private boolean isThisServer(String host, int port) throws UnknownHostException {
		return m_localIps.contains(InetAddress.getByName(host)) && port == m_config.getPort();
	}

	void updateServerList(String newServerString) {
		List<InetSocketAddress> newServerList = new ArrayList<InetSocketAddress>();
		if (newServerString != null && !newServerString.equals(m_serverString)) {
			if (parseServerString(newServerString, newServerList)) {
				m_logger
				      .info(String.format("Server list updated to %s and effectively %s", newServerString, newServerList));
				m_serverString = newServerString;
				m_serverList = newServerList;
				notifyListeners();
			}
		}
	}

}
