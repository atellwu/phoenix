package com.dianping.phoenix.context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class DefaultEnvironment implements Environment, Initializable {
	private Map<String, String> m_attributes = new HashMap<String, String>();

	private String m_dataBaseDir;

	private String m_logBaseDir;

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
	public String getDataBaseDir() {
		return m_dataBaseDir;
	}

	@Override
	public String getLogBaseDir() {
		return m_logBaseDir;
	}

	@Override
	public void initialize() throws InitializationException {
		m_dataBaseDir = org.unidal.helper.Properties.forString() //
		      .fromSystem().fromEnv("DATA_BASE_DIR").getProperty("dataBaseDir", "/data/appdatas");
		m_logBaseDir = org.unidal.helper.Properties.forString() //
		      .fromSystem().fromEnv("LOG_BASE_DIR").getProperty("logBaseDir", "/data/applogs");

		if (!new File(m_dataBaseDir).isDirectory()) {
			throw new RuntimeException(String.format("Directory(%s) does not exist!", m_dataBaseDir));
		}

		if (!new File(m_logBaseDir).isDirectory() && !new File(m_logBaseDir).mkdirs()) {
			throw new RuntimeException(String.format("Unable to create directory(%s)!", m_logBaseDir));
		}
	}

	@Override
	public void loadFrom(InputStream in) throws IOException {
		try {
			Properties properties = new Properties();

			properties.load(in);

			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				m_attributes.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
			}
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore it
			}
		}
	}

	@Override
	public void setAttribute(String name, String value) {
		m_attributes.put(name, value);
	}
}
