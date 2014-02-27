package com.dianping.phoenix.service.config;

import com.dianping.liger.Liger;
import com.dianping.phoenix.config.AbstractConfigService;
import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceProvider;

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