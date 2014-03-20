package com.dianping.phoenix.context;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.lookup.ContainerLoader;

public class ContextManager {
	private static volatile Environment s_environment;

	private static ThreadLocal<Context> s_context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new DefaultContext(getEnvironment());
		}
	};

	public static Context getContext() {
		return s_context.get();
	}

	public static Environment getEnvironment() {
		initialize();

		return s_environment;
	}

	public static void initialize() {
		if (s_environment == null) {
			synchronized (ContextManager.class) {
				if (s_environment == null) {
					PlexusContainer container = ContainerLoader.getDefaultContainer();

					try {
						s_environment = container.lookup(Environment.class);

					} catch (Exception e) {
						throw new RuntimeException("Error when looking up Environment!", e);
					}

					try {
						ThreadLocalRegistry registry = container.lookup(ThreadLocalRegistry.class);

						registry.register(s_context);
					} catch (Exception e) {
						throw new RuntimeException("Error when looking up Environment!", e);
					}
				}
			}
		}
	}

	public static void resetContext() {
		s_context.remove();
	}
}
