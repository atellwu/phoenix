package com.dianping.phoenix.log;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;

public interface AppenderBuilder {
	public Appender build(Layout layout, String name, String... params);
}
