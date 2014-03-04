package com.dianping.phoenix.context;

import java.util.HashMap;
import java.util.Map;

public class DefaultEnvironment implements Environment {
	private Map<String, String> m_attributes = new HashMap<String, String>();

	@Override
	public String getAttribute(String name, String defaultValue) {
		String value = m_attributes.get(name);

		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> getAttributes() {
		return (Map<String, String>) (Map) m_attributes;
	}

	@Override
	public void setAttribute(String name, String value) {
		m_attributes.put(name, value);
	}
}
