package com.dianping.phoenix.context;

import java.util.ArrayList;
import java.util.List;

public class DefaultThreadLocalRegistry implements ThreadLocalRegistry {
	private List<ThreadLocal<?>> m_threadLocals = new ArrayList<ThreadLocal<?>>();

	@Override
	public List<ThreadLocal<?>> getThreadLocals() {
		return m_threadLocals;
	}

	@Override
	public void register(ThreadLocal<?> threadLocal) {
		if (!m_threadLocals.contains(threadLocal)) {
			m_threadLocals.add(threadLocal);
		}
	}
}
