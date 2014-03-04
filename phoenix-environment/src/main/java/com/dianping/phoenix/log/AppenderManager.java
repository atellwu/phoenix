package com.dianping.phoenix.log;

import org.apache.log4j.Appender;

public interface AppenderManager {
	public Appender getAppender(String type, String name, String... parameters);
}
