package com.dianping.phoenix.session.requestid;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerLoader;
import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketServer;

public class Bootstrap implements ServletContextListener {
	private SocketServer m_server;

	private EventProcessor m_processor;

	private FileUploader m_uploader;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			PlexusContainer container = ContainerLoader.getDefaultContainer();
			EventDelegateManager manager = container.lookup(EventDelegateManager.class);

			m_processor = container.lookup(EventProcessor.class);
			m_processor.start();

			m_server = Sockets.forServer().listenOn(7377).threads("RequestID", 0).start(manager.getIn());

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
	}
}
