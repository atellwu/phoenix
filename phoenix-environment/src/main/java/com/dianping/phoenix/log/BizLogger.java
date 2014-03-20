package com.dianping.phoenix.log;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BizLogger {
	public BizLogger add(String key, String value);

	public BizLogger add(String key, int value);

	public BizLogger add(String key, long value);

	public BizLogger add(String key, Date value);

	public BizLogger add(String key, double value);

	public BizLogger add(String key, List<?> list);

	public BizLogger add(String key, Map<?, ?> map);

	public void initialize(String name);

	public void flush();
}
