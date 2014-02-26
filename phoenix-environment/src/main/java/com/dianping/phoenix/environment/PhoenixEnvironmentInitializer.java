package com.dianping.phoenix.environment;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.context.Environment;
import com.dianping.phoenix.initialization.PhoenixInitializer;

public class PhoenixEnvironmentInitializer implements PhoenixInitializer, LogEnabled {
	public static final String ID = "env";

	@Inject
	private Environment m_env;

	private Logger m_logger;

	@Override
	public void initialize() throws Exception {
		// load properties from application, which is owned by developers
		URL appProperties = getClass().getResource("/META-INF/app.properties");

		if (appProperties == null) {
			throw new IllegalStateException("Resource(/META-INF/app.properties) not found!");
		}

		m_env.loadFrom(appProperties.openStream());

		// load properties from server, which is owned by operations
		File serverProperties = new File(m_env.getDataBaseDir(), "server.properties");

		if (serverProperties.canRead()) {
			m_env.loadFrom(new FileInputStream(serverProperties));
		} else {
			m_logger.warn(String.format("Properties(%s) does not exist!", serverProperties));
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}