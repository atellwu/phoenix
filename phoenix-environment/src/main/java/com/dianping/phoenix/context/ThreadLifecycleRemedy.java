package com.dianping.phoenix.context;

import java.util.Map;

public interface ThreadLifecycleRemedy {
	public Map<ThreadLocal<?>, Object> getInheritableValues();

	public void afterThreadStarted(Map<ThreadLocal<?>, Object> inheritableValues);

	public void beforeThreadStopped();
}
