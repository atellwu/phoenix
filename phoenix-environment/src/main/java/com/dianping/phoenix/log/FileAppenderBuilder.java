package com.dianping.phoenix.log;

import java.io.File;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;

import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceFactory;

public class FileAppenderBuilder implements AppenderBuilder {
	public static final String ID = "file";

	@Override
	public Appender build(Layout layout, String name, String... parameters) {
		ConfigService config = ConfigServiceFactory.getConfig();
		String logBaseDir = config.getLogBaseDir();
		String dateFormat = parameters.length > 0 ? parameters[0] : LogConstants.DEFAULT_VALUE_DATE_FORMAT;

		try {
			String filename = new File(logBaseDir, name).getCanonicalPath();

			return new DailyRollingFileAppender(layout, filename, dateFormat);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to create file appender(%s:%s) due to %s.", name, dateFormat,
			      e));
		}
	}
}
