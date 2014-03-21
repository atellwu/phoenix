package com.dianping.phoenix.log;

import junit.framework.Assert;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.liger.Liger;
import com.dianping.liger.config.event.ConfigEventDispatcher;
import com.dianping.liger.repository.EphemeralRepository;
import com.dianping.liger.repository.Repository;
import com.dianping.phoenix.config.ConfigServiceFactory;
import com.dianping.phoenix.context.Environment;

public class LogTest extends ComponentTestCase {
	private static StringBuilder s_result = new StringBuilder();

	@Before
	public void before() {
		Liger.reset();
		ConfigServiceFactory.destroy();
	}

	private void checkResult(String expected) {
		String actual = s_result.toString();

		s_result.setLength(0);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testBasicMode() {
		Logger logger = Logger.getLogger(Object.class);

		logger.debug("debug");
		logger.info("information");
		logger.warn("warning");
		logger.error("error");

		Liger.reset();
	}

	@Test
	public void testStandardMode() throws Exception {
		defineComponent(Repository.class, EphemeralRepository.ID, EphemeralRepository.class) //
		      .req(ConfigEventDispatcher.class);
		defineComponent(AppenderBuilder.class, MockAppenderBuilder.ID, MockAppenderBuilder.class);

		EphemeralRepository repository = (EphemeralRepository) lookup(Repository.class, EphemeralRepository.ID);
		Environment env = lookup(Environment.class);

		// simulate environment & configuration in Liger
		env.setAttribute(Environment.APP_NAME, "Test");
		repository.setProperty("log", "Test", "a.b.c", "+warn,mock:c");
		repository.setProperty("log", "Test", "a.b.d", "debug,mock:d,console");

		lookup(LoggerManager.class).initialize();

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

		repository.reset();
	}

	@Test
	public void testBizLog() throws Exception {
		defineComponent(Repository.class, EphemeralRepository.ID, EphemeralRepository.class) //
		      .req(ConfigEventDispatcher.class);
		defineComponent(AppenderBuilder.class, MockAppenderBuilder.ID, MockAppenderBuilder.class);
		defineComponent(BizLogger.class, MockBizLogger.class).is("per-lookup");

		EphemeralRepository repository = (EphemeralRepository) lookup(Repository.class, EphemeralRepository.ID);
		Environment env = lookup(Environment.class);

		// simulate environment & configuration in Liger
		env.setAttribute(Environment.APP_NAME, "Test");
		repository.setProperty("log", "Test", "tuangou", "info,mock:tuangou");
		repository.setProperty("log", "Test", "booking", "info,mock:booking");

		lookup(LoggerManager.class).initialize();

		// developer code starts below
		BizLogger c = BizLoggerManager.getLogger("tuangou");

		c.add("k1", "v1");
		c.add("k2", "v2");
		c.flush();

		checkResult("k1v1k2v2:");

		BizLogger d = BizLoggerManager.getLogger("booking");

		d.add("k1", "v1");
		d.add("k2", "v2");
		d.add("k3", "v3");
		d.flush();

		checkResult("k1v1k2v2k3v3:");

		// how about if a configuration item is updated
		repository.setProperty("log", "Test", "booking", "info,mock:booking2");
		repository.refresh();

		d.add("k1", "v1");
		d.add("k2", "v2");
		d.add("k3", "v3");
		d.flush();

		checkResult("k1v1k2v2k3v3:");

		repository.reset();
	}

	@Test
	public void testPlexusLog() throws Exception {
		defineComponent(AppenderBuilder.class, MockAppenderBuilder.ID, MockAppenderBuilder.class);

		// simulate environment & configuration in Liger
		Environment env = lookup(Environment.class);

		env.setAttribute(Environment.APP_NAME, "Test");
		Liger.getConfig().setThreadLocalProperty("log[Test].com.dianping.phoenix.log", "warn,mock:e");

		lookup(LoggerManager.class).initialize();

		org.codehaus.plexus.logging.Logger d = getContainer().getLoggerManager().getLoggerForComponent(
		      getClass().getName());

		d.debug("debug");
		d.info("information");
		d.warn("warning");
		d.error("error");

		checkResult("warning:error:");
	}

	public static class MockAppenderBuilder implements AppenderBuilder {
		public static final String ID = "mock";

		@Override
		public Appender build(Layout layout, String name, String... params) {
			return new AppenderSkeleton() {
				@Override
				protected void append(LoggingEvent event) {
					s_result.append(event.getMessage()).append(':');
				}

				@Override
				public void close() {
				}

				@Override
				public boolean requiresLayout() {
					return false;
				}
			};
		}
	}

	public static class MockBizLogger extends DefaultBizLogger {
		@Override
		protected void addSystemFields() {
			// do nothing
		}
	}
}
