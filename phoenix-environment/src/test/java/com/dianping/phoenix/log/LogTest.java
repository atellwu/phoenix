package com.dianping.phoenix.log;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class LogTest extends ComponentTestCase {
	@Before
	public void before() {
		LogInitializer initializer = lookup(LogInitializer.class);

		initializer.bootstrap();
	}

	@Test
	public void testLog4j() {
		Logger logger = Logger.getLogger(getClass());

		logger.debug("debug");
		logger.info("information");
		logger.warn("warning");
		logger.error("error");
	}
}
