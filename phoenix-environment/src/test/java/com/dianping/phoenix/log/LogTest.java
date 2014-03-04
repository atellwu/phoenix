package com.dianping.phoenix.log;

import junit.framework.Assert;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.liger.config.Config;
import com.dianping.phoenix.context.ContextManager;
import com.dianping.phoenix.context.Environment;

public class LogTest extends ComponentTestCase {
	private static StringBuilder s_result = new StringBuilder();

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
	public void testInitialize() throws Exception {
		defineComponent(AppenderBuilder.class, MockAppenderBuilder.ID, MockAppenderBuilder.class);

		LogInitializer initializer = lookup(LogInitializer.class);

		initializer.bootstrap();

		Config config = lookup(Config.class);
		ContextManager.getEnvironment().setAttribute(Environment.APP_NAME, "Test");

		config.setThreadLocalProperty("log[Test].a.b.c", "@warn:mock:c");
		config.setThreadLocalProperty("log[Test].a.b.d", "debug:mock:d");
		initializer.initialize();

		Logger c = Logger.getLogger("a.b.c");

		c.debug("debug");
		c.info("information");
		c.warn("warning");
		c.error("error");

		Logger d = Logger.getLogger("a.b.d.e");

		d.debug("debug");
		d.info("information");
		d.warn("warning");
		d.error("error");

		Assert.assertEquals("warning:error:debug:information:warning:error:", s_result.toString());
	}

	public static class MockAppenderBuilder implements AppenderBuilder {
		public static final String ID = "mock";

		@Override
		public Appender build(Layout layout, String name, String... params) {
			return new AppenderSkeleton() {
				@Override
				public void close() {
				}

				@Override
				public boolean requiresLayout() {
					return false;
				}

				@Override
				protected void append(LoggingEvent event) {
					s_result.append(event.getMessage()).append(':');
				}
			};
		}
	}
}
