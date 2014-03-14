package com.dianping.phoenix.context;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

public class ThreadPool extends ThreadPoolExecutor {
	public ThreadPool(int poolSize) {
		super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	private ThreadLifecycleRemedy getRemedy() {
		try {
			ThreadLifecycleRemedy remedy = ContainerLoader.getDefaultContainer().lookup(ThreadLifecycleRemedy.class);

			return remedy;
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to lookup ThreadLifecycleRemedy component!", e);
		}
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, T value) {
		final ThreadLifecycleRemedy remedy = getRemedy();
		final Map<ThreadLocal<?>, Object> values = remedy.getInheritableValues();

		return super.newTaskFor(new Runnable() {
			@Override
			public void run() {
				remedy.afterThreadStarted(values);

				try {
					runnable.run();
				} finally {
					remedy.beforeThreadStopped();
				}
			}
		}, value);
	}
}
