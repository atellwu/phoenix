package com.dianping.phoenix.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	public Set<String> getAttributeNames() {
		return m_attributes.keySet();
	}

	@Override
	public void setAttribute(String name, String value) {
		m_attributes.put(name, value);
	}
}
