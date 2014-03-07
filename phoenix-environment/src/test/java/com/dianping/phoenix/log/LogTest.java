package com.dianping.phoenix.log;

import junit.framework.Assert;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.liger.config.event.ConfigEventDispatcher;
import com.dianping.liger.repository.EphemeralRepository;
import com.dianping.liger.repository.Repository;
import com.dianping.phoenix.context.ContextManager;
import com.dianping.phoenix.context.Environment;

public class LogTest extends ComponentTestCase {
	private static StringBuilder s_result = new StringBuilder();

	@Test
	public void testBasicMode() {
		LogInitializer initializer = lookup(LogInitializer.class);

		initializer.bootstrap();

		Logger logger = Logger.getLogger(Object.class);

		logger.debug("debug");
		logger.info("information");
		logger.warn("warning");
		logger.error("error");
	}

	@Test
	public void testStandardMode() throws Exception {
		defineComponent(Repository.class, EphemeralRepository.ID, EphemeralRepository.class) //
		      .req(ConfigEventDispatcher.class);
		defineComponent(AppenderBuilder.class, MockAppenderBuilder.ID, MockAppenderBuilder.class);

		LogInitializer initializer = lookup(LogInitializer.class);
		EphemeralRepository repository = (EphemeralRepository) lookup(Repository.class, EphemeralRepository.ID);

		initializer.bootstrap();

		// simulate environment & configuration in Liger
		ContextManager.getEnvironment().setAttribute(Environment.APP_NAME, "Test");
		repository.setProperty("log", "Test", "a.b.c", "+warn,mock:c");
		repository.setProperty("log", "Test", "a.b.d", "debug,mock:d,console");
		initializer.initialize();

		// developer code starts below
		Logger c = Logger.getLogger("a.b.c");

		c.debug("debug");
		c.info("information");
		c.warn("warning");
		c.error("error");

		checkResult("warning:error:");

		Logger d = Logger.getLogger("a.b.d.e");

		d.debug("debug");
		d.info("information");
		d.warn("warning");
		d.error("error");

		checkResult("debug:information:warning:error:");

		// how about if a configuration item is updated
		repository.setProperty("log", "Test", "a.b.d", "warn,mock:d");
		repository.refresh();

		d.debug("debug");
		d.info("information");
		d.warn("warning");
		d.error("error");

		checkResult("warning:error:");
	}

	private void checkResult(String expected) {
		String actual = s_result.toString();

		s_result.setLength(0);
		Assert.assertEquals(expected, actual);
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
