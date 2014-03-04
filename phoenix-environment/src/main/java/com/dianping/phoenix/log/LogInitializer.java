package com.dianping.phoenix.log;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.liger.config.Config;
import com.dianping.phoenix.config.ConfigServiceFactory;
import com.dianping.phoenix.initialization.PhoenixInitializer;

public class LogInitializer implements PhoenixInitializer, LogEnabled {
	@Inject
	private Config m_config;

	@Inject
	private AppenderManager m_appenderManager;

	private Logger m_logger;

	public void bootstrap() {
		URL url = getClass().getResource("/META-INF/phoenix/log4j.xml");

		DOMConfigurator.configure(url);
	}

	@Override
	public void initialize() {
		LoggerRepository repository = LogManager.getLoggerRepository();
		String appName = ConfigServiceFactory.getConfig().getAppName();
		Map<String, String> properties = m_config.getInstanceProperties("log", appName);
		String[] emptyArray = new String[0];

		for (Map.Entry<String, String> e : properties.entrySet()) {
			String category = e.getKey();
			String value = e.getValue();

			List<String> parameters = Splitters.by(':').trim().split(value);
			String level = parameters.size() > 0 ? parameters.remove(0) : null;
			String type = parameters.size() > 0 ? parameters.remove(0) : null;
			String name = parameters.size() > 0 ? parameters.remove(0) : null;

			if (type == null) {
				m_logger.warn(String.format("Invalid log property(%s=%s) configured!", category, value));
				continue;
			}

			Appender appender = m_appenderManager.getAppender(type, name, parameters.toArray(emptyArray));
			org.apache.log4j.Logger logger = repository.getLogger(category);
			boolean additivity = level.startsWith("@");

			logger.addAppender(appender);
			logger.setLevel(toLevel(additivity ? level.substring(1) : level));
			logger.setAdditivity(additivity);
		}
	}

	private Level toLevel(String level) {
		if ("ERROR".equalsIgnoreCase(level)) {
			return Level.ERROR;
		} else if ("WARN".equalsIgnoreCase(level) || "WARNING".equalsIgnoreCase(level)) {
			return Level.WARN;
		} else if ("INFO".equalsIgnoreCase(level)) {
			return Level.INFO;
		} else if ("DEBUG".equalsIgnoreCase(level)) {
			return Level.DEBUG;
		} else if ("FATAL".equalsIgnoreCase(level)) {
			return Level.FATAL;
		} else {
			return Level.OFF;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
