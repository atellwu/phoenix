package com.dianping.phoenix.config.liger;

import org.unidal.lookup.annotation.Inject;

import com.dianping.liger.config.ConfigEnvironment;
import com.dianping.phoenix.context.Environment;

public class LigerConfigEnvironment implements ConfigEnvironment {
	@Inject
	private Environment m_environment;

	@Override
	public String getVariant(String name) {
		return m_environment.getAttribute(name, null);
	}

	@Override
	public void setVariant(String name, String value) {
		m_environment.setAttribute(name, value);
	}
}