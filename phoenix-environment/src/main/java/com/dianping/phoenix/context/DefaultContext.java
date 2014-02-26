package com.dianping.phoenix.context;

import java.util.HashMap;
import java.util.Map;

public class DefaultContext implements Context {
	private Environment m_env;

	private Map<String, String> m_attributes = new HashMap<String, String>();

	@Override
	public String getAttribute(String name) {
		String value = m_attributes.get(name);

		if (value == null) {
			value = m_env.getAttribute(name, null);
		}

		return value;
	}

	@Override
	public Map<String, String> getAttributes() {
		return m_attributes;
	}

	@Override
	public Environment getEnvironment() {
		return m_env;
	}

	@Override
	public void setAttribute(String name, String value) {
		if (value == null) {
			m_attributes.remove(name);
		} else {
			m_attributes.put(name, value);
		}
	}

	public DefaultContext setEnvironment(Environment env) {
		m_env = env;
		return this;
	}

	@Override
	public String toString() {
		return String.format("%s[attributes=%s, env=%s]", getClass().getSimpleName(), m_attributes, m_env);
	}
}