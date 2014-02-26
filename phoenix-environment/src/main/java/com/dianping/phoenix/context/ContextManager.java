package com.dianping.phoenix.context;

import org.unidal.lookup.ContainerLoader;

public class ContextManager {
	private static volatile Environment s_environment;

	private static ThreadLocal<Context> s_contexts = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new DefaultContext().setEnvironment(getEnvironment());
		}
	};

	public static Context getContext() {
		return s_contexts.get();
	}

	public static Environment getEnvironment() {
		initialize();

		return s_environment;
	}

	public static void initialize() {
		if (s_environment == null) {
			synchronized (ContextManager.class) {
				if (s_environment == null) {
					try {
						s_environment = ContainerLoader.getDefaultContainer().lookup(Environment.class);
					} catch (Exception e) {
						throw new IllegalStateException("Unable to load Environment due to " + e, e);
					}
				}
			}
		}
	}

	public static void resetContext() {
		s_contexts.remove();
	}
}
