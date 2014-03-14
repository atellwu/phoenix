package com.dianping.phoenix.context;

import java.util.List;

public interface ThreadLocalRegistry {
	public List<ThreadLocal<?>> getThreadLocals();

	public void register(ThreadLocal<?> threadLocal);
}
