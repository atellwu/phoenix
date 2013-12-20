package com.dianping.phoenix.session.requestid;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerLoader;
import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketServer;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.requestid.serverevent.DefaultServerAddressManager;
import com.dianping.phoenix.session.server.DefaultEventPublisher;
import com.dianping.phoenix.session.server.ServerAddressManager;
import com.dianping.phoenix.session.server.EventPublisher;

public class Bootstrap implements ServletContextListener {

	private final static int DEFAULT_PORT = 7377;

	private SocketServer m_server;

	private EventProcessor m_processor;

	private FileUploader m_uploader;

	private DefaultServerAddressManager m_serverAddrMgr;

	private DefaultEventPublisher m_serverEventPublisher;

	private ConfigManager m_config;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			PlexusContainer container = ContainerLoader.getDefaultContainer();

			String configFileInConfig = sce.getServletContext().getInitParameter("configFile");
			if (configFileInConfig != null) {
				ConfigManager.setConfigFile(configFileInConfig);
			}
			
			m_config = container.lookup(ConfigManager.class);
			
			int port = DEFAULT_PORT;
			String portInConfig = sce.getServletContext().getInitParameter("port");
			if (portInConfig != null) {
				try {
					port = Integer.parseInt(portInConfig);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			m_config.setPort(port);
			
			RecordFileManager recMgr = container.lookup(RecordFileManager.class);
			recMgr.start();

			EventDelegateManager manager = container.lookup(EventDelegateManager.class);

			m_processor = container.lookup(EventProcessor.class);
			m_processor.start();


			m_server = Sockets.forServer().listenOn(port).threads("RequestID", 0).start(manager.getIn());

			m_serverAddrMgr = (DefaultServerAddressManager) container.lookup(ServerAddressManager.class);
			Threads.forGroup("Phoenix").start(m_serverAddrMgr);

			m_serverEventPublisher = (DefaultEventPublisher) container.lookup(EventPublisher.class);
			RequestEventDelegate eventSource = container.lookup(EventDelegateManager.class).getOut();
			m_serverEventPublisher.start(eventSource);

			m_uploader = container.lookup(FileUploader.class);
			Threads.forGroup("Phoenix").start(m_uploader);
		} catch (Exception e) {
			throw new RuntimeException("Error when starting up session service!", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (m_server != null) {
			m_server.shutdown();
		}

		if (m_processor != null) {
			m_processor.stop();
		}

		if (m_uploader != null) {
			m_uploader.shutdown();
		}

		if (m_serverAddrMgr != null) {
			m_serverAddrMgr.shutdown();
		}
	}
}
