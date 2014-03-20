package com.dianping.phoenix.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.phoenix.context.ContextManager;
import com.dianping.phoenix.context.Environment;

public abstract class AbstractConfigService implements ConfigService {
	@Override
	public String getAppName() {
		return getEnv(Environment.APP_NAME, "NONAME");
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public String getDataBaseDir() {
		return getEnv(Environment.DATA_BASE_DIR, "./target");
	}

	@Override
	public Date getDate(String key, String format, Date defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return new SimpleDateFormat(format).parse(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	private String getEnv(String name, String defaultValue) {
		return ContextManager.getEnvironment().getAttribute(name, defaultValue);
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public int getInt(String key, int defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public String getLogBaseDir() {
		return getEnv(Environment.LOG_BASE_DIR, "./target");
	}

	@Override
	public long getLong(String key, long defaultValue) {
		String value = getString(key, null);

		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}
}
