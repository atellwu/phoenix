package com.dianping.phoenix.config;

import org.unidal.lookup.ContainerLoader;

public abstract class ConfigServiceFactory {
	private static volatile ConfigService s_service;

	public static void destroy() {
		s_service = null;
	}

	public static ConfigService getConfig() {
		if (s_service == null) {
			synchronized (ConfigServiceFactory.class) {
				if (s_service == null) {
					try {
						ConfigServiceProvider provider = ContainerLoader.getDefaultContainer().lookup(
						      ConfigServiceProvider.class);

						s_service = provider.getConfigService();
					} catch (Throwable e) {
						throw new RuntimeException("Unable to get ConfigService due to " + e, e);
					}
				}
			}
		}

		return s_service;
	}
}
