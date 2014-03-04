package com.dianping.phoenix.config;

import java.util.Date;

public interface ConfigService {
	public String getAppName();

	public String getDataBaseDir();

	public String getLogBaseDir();

	public boolean getBoolean(String key, boolean defaultValue);

	public Date getDate(String key, String format, Date defaultValue);

	public double getDouble(String key, double defaultValue);

	public float getFloat(String key, float defaultValue);

	public int getInt(String key, int defaultValue);

	public long getLong(String key, long defaultValue);

	public String getString(String key, String defaultValue);
}
