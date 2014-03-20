package com.dianping.phoenix.log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.Cat;

public class BizLoggerManager {
	private static volatile Map<String, BizLogger> s_loggers = new HashMap<String, BizLogger>();

	public static BizLogger getLogger(String name) {
		BizLogger logger = s_loggers.get(name);

		if (logger == null) {
			synchronized (s_loggers) {
				logger = s_loggers.get(name);

				if (logger == null) {
					logger = makeLogger(name);

					s_loggers.put(name, logger);
				}
			}
		}

		return logger;
	}

	private static BizLogger makeLogger(String name) {
		try {
			BizLogger logger = ContainerLoader.getDefaultContainer().lookup(BizLogger.class);

			logger.initialize(name);
			return logger;
		} catch (Throwable e) {
			Cat.logError(e);
		}

		return NullBizLogger.INSTANCE;
	}

	static enum NullBizLogger implements BizLogger {
		INSTANCE;

		@Override
      public BizLogger add(String key, Date value) {
	      return this;
      }

		@Override
      public BizLogger add(String key, double value) {
	      return this;
      }

		@Override
      public BizLogger add(String key, int value) {
	      return this;
      }

		@Override
      public BizLogger add(String key, List<?> list) {
	      return this;
      }

		@Override
      public BizLogger add(String key, long value) {
	      return this;
      }

		@Override
      public BizLogger add(String key, Map<?, ?> map) {
	      return this;
      }

		@Override
		public BizLogger add(String key, String value) {
			return this;
		}

		@Override
		public void flush() {
		}

		@Override
		public void initialize(String name) {
		}
	}
}
