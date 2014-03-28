package com.dianping.phoenix.context;

import java.util.Map;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

/**
 * This helper class will be used by remedied ThreadPoolExecutor.
 * 
 */
public class ThreadPoolExecutorHelper {
	private static ThreadLifecycleRemedy getRemedy() {
		try {
			ThreadLifecycleRemedy remedy = ContainerLoader.getDefaultContainer().lookup(ThreadLifecycleRemedy.class);

			return remedy;
		} catch (ComponentLookupException e) {
			new RuntimeException("Unable to lookup ThreadLifecycleRemedy component!", e).printStackTrace();
			return null;
		}
	}

	public static Runnable wrap(final Runnable runnable) {
		final ThreadLifecycleRemedy remedy = getRemedy();

		if (remedy != null) {
			final Map<ThreadLocal<?>, Object> values = remedy.getInheritableValues();

			return new Runnable() {
				@Override
				public void run() {
					remedy.afterThreadStarted(values);

					try {
						runnable.run();
					} finally {
						remedy.beforeThreadStopped();
					}
				}
			};
		} else {
			return runnable;
		}
	}
}
