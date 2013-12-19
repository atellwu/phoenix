package com.dianping.phoenix.session.requestid.serverevent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.util.NetworkUtil;

public class DefaultServerAddressManager implements ServerAddressManager, Initializable, LogEnabled {

	private String m_serverString;

	@Inject
	private ConfigManager m_config;

	private Logger m_logger;

	private List<InetSocketAddress> m_serverList = new ArrayList<InetSocketAddress>();

	private List<AddressChangeListener> m_listeners = Collections
	      .synchronizedList(new ArrayList<ServerAddressManager.AddressChangeListener>());

	private Set<InetAddress> localIps = new HashSet<InetAddress>();
	
	private DefaultHttpClient hc = new DefaultHttpClient();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	String fetchServerString(String reqUrl) {
		String serverString = null;
		try {
			if (reqUrl.startsWith("http://")) {
				serverString = fetchFromHttp(reqUrl);
			} else if (reqUrl.startsWith("file:/")) {
				serverString = fetchFromFile(reqUrl);
			} else {
				throw new RuntimeException(String.format("Unknown type of url %s", reqUrl));
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error fetch server list from %s", reqUrl), e);
		}
		return serverString;
	}

	private String fetchFromFile(String reqUrl) throws IOException {
		return IOUtils.toString(new InputStreamReader(new URL(reqUrl).openStream(), "utf-8"));
	}

	private String fetchFromHttp(String reqUrl) throws Exception {
		HttpGet req = new HttpGet(reqUrl);
		HttpResponse res = hc.execute(req);
		return IOUtils.toString(res.getEntity().getContent());
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
		localIps.addAll(getLocalIps());
		updateServerList(fetchServerString(m_config.getServerListUpdateUrl()));
	}

	private void notifyListeners() {
		for (AddressChangeListener listener : m_listeners) {
			listener.onAddressChange(m_serverList);
		}
	}

	private boolean parseServerString(String newServerString, List<InetSocketAddress> newServerList) {
		boolean success = true;

		String[] servers = newServerString.split(",");
		try {
			for (int i = 0; i < servers.length; i++) {
				String[] hostPort = servers[i].split(":");
				String host = hostPort[0].trim();
				if (!localIps.contains(InetAddress.getByName(host))) {
					newServerList.add(new InetSocketAddress(host, Integer.parseInt(hostPort[1].trim())));
				}
			}
		} catch (Exception e) {
			success = false;
			m_logger.error(String.format("Invalid server list %s", newServerList));
		}

		return success;
	}

	public void start() {
		Threads.forGroup("Phoenix").start(new UpdateTask());
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

	class UpdateTask implements Task {

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (true) {
				updateServerList(fetchServerString(m_config.getServerListUpdateUrl()));
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					break;
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}

}
