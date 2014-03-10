package com.dianping.phoenix.log;

import static org.codehaus.plexus.logging.Logger.LEVEL_DEBUG;
import static org.codehaus.plexus.logging.Logger.LEVEL_ERROR;
import static org.codehaus.plexus.logging.Logger.LEVEL_FATAL;
import static org.codehaus.plexus.logging.Logger.LEVEL_INFO;
import static org.codehaus.plexus.logging.Logger.LEVEL_WARN;

import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

public class Log4jLoggerManager extends BaseLoggerManager implements Initializable {
	@Override
	protected org.codehaus.plexus.logging.Logger createLogger(String name) {
		Logger logger = LogManager.getLogger(name);
		LevelAdapter level = new LevelAdapter(logger.getLevel());

		return new LoggerAdapter(logger, level.getThreshold());
	}

	@Override
	public void initialize() {
		super.initialize();

		// basic logging, loaded from log4j.xml file
		URL url = getClass().getResource("/META-INF/phoenix/log4j.xml");

		DOMConfigurator.configure(url);
	}

	@Override
	protected String toMapKey(String role, String roleHint) {
		// we only care about class name
		return role;
	}

	static class LevelAdapter {
		private int m_threshold;

		public LevelAdapter(Level level) {
			m_threshold = LEVEL_DEBUG;

			if (level != null) {
				switch (level.toInt()) {
				case Priority.DEBUG_INT:
					m_threshold = LEVEL_DEBUG;
					break;
				case Priority.INFO_INT:
					m_threshold = LEVEL_INFO;
					break;
				case Priority.WARN_INT:
					m_threshold = LEVEL_WARN;
					break;
				case Priority.ERROR_INT:
					m_threshold = LEVEL_ERROR;
					break;
				case Priority.FATAL_INT:
					m_threshold = LEVEL_FATAL;
					break;
				}
			}
		}

		public int getThreshold() {
			return m_threshold;
		}
	}

	static class LoggerAdapter extends AbstractLogger {
		private Logger m_logger;

		public LoggerAdapter(Logger logger, int threshold) {
			super(threshold, logger.getName());

			m_logger = logger;
		}

		@Override
		public void debug(String message, Throwable t) {
			if (isDebugEnabled()) {
				m_logger.debug(message, t);
			}
		}

		@Override
		public void error(String message, Throwable t) {
			if (isErrorEnabled()) {
				m_logger.error(message, t);
			}
		}

		@Override
		public void fatalError(String message, Throwable t) {
			if (isFatalErrorEnabled()) {
				m_logger.fatal(message, t);
			}
		}

		@Override
		public org.codehaus.plexus.logging.Logger getChildLogger(String name) {
			return this;
		}

		@Override
		public void info(String message, Throwable t) {
			if (isInfoEnabled()) {
				m_logger.info(message, t);
			}
		}

		@Override
		public void warn(String message, Throwable t) {
			if (isWarnEnabled()) {
				m_logger.warn(message, t);
			}
		}
	}
}
