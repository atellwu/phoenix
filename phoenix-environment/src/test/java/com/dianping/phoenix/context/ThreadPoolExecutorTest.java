package com.dianping.phoenix.context;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.phoenix.agent.AgentMain;
import com.dianping.phoenix.agent.ObjectHelper;

public class ThreadPoolExecutorTest extends ComponentTestCase {
	@Test
	public void testThreadPool() throws Exception {
		defineComponent(ThreadLifecycleRemedy.class, MockRemedy.class);

		AgentMain.attach();

		ObjectHelper.set("test", new StringBuilder());

		ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(new Runnable() {
			@Override
			public void run() {
				StringBuilder sb = ObjectHelper.get("test");

				sb.append("insideRunnable\n");
			}
		});

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);

		Assert.assertEquals("afterThreadStarted\ninsideRunnable\nbeforeThreadStopped\n", ObjectHelper.get("test")
		      .toString());
	}

	public static class MockRemedy implements ThreadLifecycleRemedy {
		@Override
		public Map<ThreadLocal<?>, Object> getInheritableValues() {
			return null;
		}

		@Override
		public void afterThreadStarted(Map<ThreadLocal<?>, Object> inheritableValues) {
			StringBuilder sb = ObjectHelper.get("test");

			sb.append("afterThreadStarted\n");
		}

		@Override
		public void beforeThreadStopped() {
			StringBuilder sb = ObjectHelper.get("test");

			sb.append("beforeThreadStopped\n");
		}
	}
}
