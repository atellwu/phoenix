package com.dianping.phoenix.context;

import java.util.HashMap;
import java.util.Map;

import org.unidal.helper.Reflects;
import org.unidal.lookup.annotation.Inject;

public class DefaultThreadLifecycleRemedy implements ThreadLifecycleRemedy {
	@Inject
	private ThreadLocalRegistry m_registry;

	@Override
	public void afterThreadStarted(Map<ThreadLocal<?>, Object> inheritableValues) {
		for (ThreadLocal<?> tl : m_registry.getThreadLocals()) {
			Object value = inheritableValues.get(tl);

			remedy(tl, value);
		}
	}

	@Override
	public void beforeThreadStopped() {
		for (ThreadLocal<?> tl : m_registry.getThreadLocals()) {
			tl.remove();
		}
	}

	@Override
	public Map<ThreadLocal<?>, Object> getInheritableValues() {
		Map<ThreadLocal<?>, Object> map = new HashMap<ThreadLocal<?>, Object>();

		for (ThreadLocal<?> tl : m_registry.getThreadLocals()) {
			if (tl instanceof InheritableThreadLocal) {
				map.put(tl, tl.get());
			}
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private void remedy(ThreadLocal<?> tl, Object parentValue) {
		tl.remove();

		if (tl instanceof InheritableThreadLocal) {
			Object value = Reflects.forMethod().invokeDeclaredMethod(tl, "childValue", Object.class, parentValue);

			((ThreadLocal<Object>) tl).set(value);
		}
	}
}
