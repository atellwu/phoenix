package com.dianping.phoenix.config;

import com.dianping.liger.Liger;

public class LigerConfigServiceProvider implements ConfigServiceProvider {
	@Override
	public ConfigService getConfigService() {
		return new AbstractConfigService() {
			@Override
			public String getString(String key, String defaultValue) {
				return Liger.getConfig().getProperty(key, defaultValue);
			}
		};
	}
}