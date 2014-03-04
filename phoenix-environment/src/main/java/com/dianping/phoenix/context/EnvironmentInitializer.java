package com.dianping.phoenix.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.initialization.PhoenixInitializer;

public class EnvironmentInitializer implements PhoenixInitializer, LogEnabled {
	@Inject
	private Environment m_env;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws Exception {
		prepareBaseDirs();
		processAppProperties();
		processServerProperties();
	}

	/**
	 * Loads properties from specified input stream and close the stream. If this method is called multiple times, then some
	 * properties might be overridden by latter calls.
	 * 
	 * @param in
	 *           stream to load properties from
	 */
	private void loadFrom(InputStream in) throws IOException {
		try {
			Properties properties = new Properties();

			properties.load(in);

			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				m_env.setAttribute(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
			}
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore it
			}
		}
	}

	/**
	 * prepare data and log base directories.
	 */
	private void prepareBaseDirs() {
		String dataBaseDir = org.unidal.helper.Properties.forString() //
		      .fromSystem().fromEnv("DATA_BASE_DIR").getProperty("dataBaseDir", "/data/appdatas");
		String logBaseDir = org.unidal.helper.Properties.forString() //
		      .fromSystem().fromEnv("LOG_BASE_DIR").getProperty("logBaseDir", "/data/applogs");

		if (!new File(dataBaseDir).isDirectory()) {
			throw new RuntimeException(String.format("Directory(%s) does not exist!", dataBaseDir));
		}

		if (!new File(logBaseDir).isDirectory() && !new File(logBaseDir).mkdirs()) {
			throw new RuntimeException(String.format("Unable to create directory(%s)!", logBaseDir));
		}

		m_env.setAttribute(Environment.DATA_BASE_DIR, dataBaseDir);
		m_env.setAttribute(Environment.LOG_BASE_DIR, logBaseDir);
	}

	/**
	 * load properties from application, which is owned by developers
	 * 
	 * @throws IOException
	 */
	private void processAppProperties() throws IOException {
		URL appProperties = getClass().getResource("/META-INF/app.properties");

		if (appProperties == null) {
			throw new IllegalStateException("Resource(/META-INF/app.properties) not found!");
		}

		loadFrom(appProperties.openStream());
	}

	/**
	 * load properties from server, which is owned by operations
	 * 
	 * @throws IOException
	 */
	private void processServerProperties() throws IOException {
		String dataBaseDir = m_env.getAttribute(Environment.DATA_BASE_DIR, ".");
		File serverProperties = new File(dataBaseDir, "server.properties");

		if (serverProperties.canRead()) {
			loadFrom(new FileInputStream(serverProperties));
		} else {
			m_logger.warn(String.format("Properties(%s) does not exist!", serverProperties));
		}
	}
}