package com.dianping.phoenix.log;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;

public class ConsoleAppenderBuilder implements AppenderBuilder {
	public static final String ID = "console";

	@Override
	public Appender build(Layout layout, String name, String... params) {
		return new ConsoleAppender(layout);
	}
}
