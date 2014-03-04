package com.dianping.phoenix.log;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class LogTest extends ComponentTestCase {
	@Test
	public void testBootstrap() {
		LogInitializer initializer = lookup(LogInitializer.class);

		initializer.bootstrap();

		Logger logger = Logger.getLogger(Object.class);

		logger.debug("debug");
		logger.info("information");
		logger.warn("warning");
		logger.error("error");
	}

	@Test
	public void testInitialize() {
		LogInitializer initializer = lookup(LogInitializer.class);

		initializer.bootstrap();
		initializer.initialize();

		Logger logger = Logger.getLogger(getClass());

		logger.info("haha");
	}
}
