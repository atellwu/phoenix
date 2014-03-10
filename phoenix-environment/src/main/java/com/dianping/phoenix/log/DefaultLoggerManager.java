package com.dianping.phoenix.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.liger.config.Config;
import com.dianping.liger.config.event.ConfigEvent;
import com.dianping.liger.config.event.InstancePropertiesListener;
import com.dianping.phoenix.config.ConfigServiceFactory;

public class DefaultLoggerManager implements LoggerManager, LogEnabled {
	@Inject
	private Config m_config;

	@Inject
	private AppenderManager m_appenderManager;

	private Parser m_parser = new Parser();

	private Logger m_logger;

	@Override
	public void configure() {
		String appName = ConfigServiceFactory.getConfig().getAppName();
		Map<String, String> properties = m_config.getInstanceProperties("log", appName);

		for (Map.Entry<String, String> e : properties.entrySet()) {
			doAdd(e.getKey(), e.getValue());
		}

		m_config.addListener(new InstancePropertiesListener("log", appName) {
			@Override                                                                 
			protected void handleAdd(ConfigEvent event, String key, String newValue) {
				doAdd(key, newValue);
			}

			@Override
			protected void handleDelete(ConfigEvent event, String key, String oldValue) {
				doRemove(key, oldValue);
			}

			@Override
			protected void handleUpdate(ConfigEvent event, String key, String newValue, String oldValue) {
				doRemove(key, oldValue);
				doAdd(key, newValue);
			}
		});
	}

	@Override
	public void destroy() {
		LogManager.getLoggerRepository().shutdown();
	}

	private void doAdd(String category, String value) {
		Entry entry = m_parser.parse(category, value);
		LoggerRepository repository = LogManager.getLoggerRepository();
		org.apache.log4j.Logger logger = repository.getLogger(category);
		int len = entry.size();

		for (int i = 0; i < len; i++) {
			String type = entry.getTypes().get(i);
			String name = entry.getNames().get(i);
			List<String> parameters = entry.getParametersList().get(i);
			Appender appender = m_appenderManager.getAppender(type, name, parameters.toArray(new String[0]));

			logger.addAppender(appender);
		}

		logger.setAdditivity(entry.isAdditivity());
		logger.setLevel(toLevel(entry.getLevel()));
	}

	private void doRemove(String category, String value) {
		Entry entry = m_parser.parse(category, value);
		LoggerRepository repository = LogManager.getLoggerRepository();
		org.apache.log4j.Logger logger = repository.getLogger(category);
		int len = entry.size();

		for (int i = 0; i < len; i++) {
			String type = entry.getTypes().get(i);
			String name = entry.getNames().get(i);
			List<String> parameters = entry.getParametersList().get(i);
			Appender appender = m_appenderManager.getAppender(type, name, parameters.toArray(new String[0]));

			logger.removeAppender(appender);
		}

		if (entry.isAdditivity()) {
			logger.setAdditivity(false);
		}

		logger.setLevel(Level.OFF);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

	static class Entry {
		private boolean m_additivity;

		private String m_level;

		private List<String> m_types = new ArrayList<String>();

		private List<String> m_names = new ArrayList<String>();

		private List<List<String>> m_parametersList = new ArrayList<List<String>>();

		public String getLevel() {
			return m_level;
		}

		public List<String> getNames() {
			return m_names;
		}

		public List<List<String>> getParametersList() {
			return m_parametersList;
		}

		public List<String> getTypes() {
			return m_types;
		}

		public boolean isAdditivity() {
			return m_additivity;
		}

		public void setAdditivity(boolean addivity) {
			m_additivity = addivity;
		}

		public void setLevel(String level) {
			m_level = level;
		}

		public int size() {
			return m_types.size();
		}

		@Override
		public String toString() {
			return String.format("%s[addivity=%ss, types=%s, names=%s, parametersList=%s]", getClass().getSimpleName(),
			      m_additivity, m_types, m_names, m_parametersList);
		}
	}

	class Parser {
		public Entry parse(String category, String value) {
			Entry entry = new Entry();

			if (value.startsWith("+")) {
				entry.setAdditivity(true);
				value = value.substring(1);
			}

			List<String> parts = Splitters.by(',').noEmptyItem().trim().split(value);
			String level = parts.size() > 0 ? parts.remove(0) : null;

			entry.setLevel(level);

			for (String part : parts) {
				List<String> parameters = Splitters.by(':').trim().split(part);
				String type = parameters.size() > 0 ? parameters.remove(0) : null;
				String name = parameters.size() > 0 ? parameters.remove(0) : null;

				if (type == null) {
					m_logger.warn(String.format("Invalid log property(%s=%s) configured!", category, value));
					continue;
				}

				entry.getTypes().add(type);
				entry.getNames().add(name);
				entry.getParametersList().add(parameters);
			}

			return entry;
		}
	}
}
