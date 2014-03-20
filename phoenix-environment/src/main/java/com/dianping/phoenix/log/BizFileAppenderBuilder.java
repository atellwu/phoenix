package com.dianping.phoenix.log;

import java.io.File;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceFactory;

public class BizFileAppenderBuilder implements AppenderBuilder, Initializable {
	public static final String ID = "biz";

	private Layout m_layout;

	@Override
	public Appender build(Layout layout, String name, String... parameters) {
		ConfigService config = ConfigServiceFactory.getConfig();
		String appName = config.getAppName();
		String logBaseDir = config.getLogBaseDir();
		String dateFormat = parameters.length > 0 ? parameters[0] : LogConstants.DEFAULT_VALUE_DATE_FORMAT;

		try {
			String filename = new File(logBaseDir, appName + "/biz/" + name + ".log").getCanonicalPath();

			return new DailyRollingFileAppender(m_layout, filename, dateFormat);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to create file appender(%s:%s) due to %s.", name, dateFormat,
			      e));
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_layout = new PatternLayout(LogConstants.DEFAULT_VALUE_BIZ_CONVERSION_PATTERN);
	}
}
