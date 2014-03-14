package com.dianping.phoenix.context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class ThreadLifecycleRemedyTest extends ComponentTestCase {
	private static int s_tl_index = 0;

	private static int s_itl_index = 0;

	private ThreadLocal<String> m_tl1 = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "mock-" + s_tl_index++;
		}
	};

	private InheritableThreadLocal<String> m_itl1 = new InheritableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "mock-" + s_itl_index++;
		}

		@Override
		protected String childValue(String parentValue) {
			return parentValue + "-" + s_itl_index++;
		}
	};

	@Test
	public void testRemedy() throws Exception {
		ThreadLocalRegistry registry = lookup(ThreadLocalRegistry.class);
		StringBuilder sb = new StringBuilder();
		ThreadPool pool = new ThreadPool(2);

		// thread local
		registry.register(m_tl1);

		check(sb, m_tl1, "mock-0");
		check(sb, m_tl1, "mock-1");

		check(sb, pool, m_tl1, "mock-2");
		check(sb, pool, m_tl1, "mock-3");
		check(sb, pool, m_tl1, "mock-4");

		// inheritable thread local
		registry.register(m_itl1);

		Assert.assertEquals("mock-0", m_itl1.get());
		check(sb, pool, m_itl1, "mock-0-2");
		check(sb, pool, m_itl1, "mock-0-4");
		check(sb, pool, m_itl1, "mock-0-6");

		// another one
		m_itl1.remove();
		Assert.assertEquals("mock-7", m_itl1.get());
		check(sb, pool, m_itl1, "mock-7-9");
		check(sb, pool, m_itl1, "mock-7-11");
		check(sb, pool, m_itl1, "mock-7-13");

		// native thread
		m_itl1.remove();
		check(sb, m_itl1, "mock-14");
		check(sb, m_itl1, "mock-15");
	}

	private void check(StringBuilder sb, ThreadLocal<String> tl, String expected) throws InterruptedException {
		Thread thread = new MockThread(sb, tl);

		thread.start();
		thread.join();

		Assert.assertEquals(expected, sb.toString());
		sb.setLength(0);
	}

	private void check(StringBuilder sb, ThreadPool pool, ThreadLocal<String> tl, String expected)
	      throws InterruptedException, ExecutionException {
		Thread thread = new MockThread(sb, tl);
		Future<?> future = pool.submit(thread);

		future.get();

		Assert.assertEquals(expected, sb.toString());
		sb.setLength(0);
	}

	class MockThread extends Thread {
		private StringBuilder m_sb;

		private ThreadLocal<String> m_tl;

		public MockThread(StringBuilder sb, ThreadLocal<String> tl) {
			m_sb = sb;
			m_tl = tl;
		}

		@Override
		public void run() {
			Object value = m_tl.get();

			if (m_sb.length() > 0) {
				m_sb.append('|');
			}

			m_sb.append(value);
			m_tl.remove();
		}
	}
}
