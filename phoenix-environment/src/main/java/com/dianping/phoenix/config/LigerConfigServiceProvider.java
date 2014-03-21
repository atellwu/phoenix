package com.dianping.phoenix.config;

import org.unidal.lookup.ContainerHolder;

import com.dianping.liger.Liger;
import com.dianping.phoenix.context.Environment;

public class LigerConfigServiceProvider extends ContainerHolder implements ConfigServiceProvider {
	@Override
	public ConfigService getConfigService() {
		return new AbstractConfigService() {
			@Override
			public String getString(String key, String defaultValue) {
				return Liger.getConfig().getProperty(key, defaultValue);
			}

			@Override
			protected String getEnv(String name, String defaultValue) {
				Environment env = lookup(Environment.class);

				return env.getAttribute(name, defaultValue);
			}
		};
	}
}