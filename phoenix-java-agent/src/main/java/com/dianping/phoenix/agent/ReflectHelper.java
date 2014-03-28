package com.dianping.phoenix.agent;

import java.lang.reflect.Method;

public class ReflectHelper {
	@SuppressWarnings("unchecked")
	public static <T> T call(Object instance, String className, String methodName, Object... params) {
		if (className == null && instance == null) {
			throw new IllegalArgumentException("Parameter(instance) or parameter(className) is required!");
		} else if (methodName == null) {
			throw new IllegalArgumentException("Parameter(methodName) is required!");
		}

		try {
			Class<?> clazz;
			Method method = null;

			if (className == null) {
				clazz = instance.getClass();
			} else {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			}

			if (params.length == 0) {
				method = clazz.getMethod(methodName);
			} else {
				Method[] methods = clazz.getMethods();

				for (Method m : methods) {
					if (m.getParameterTypes().length == params.length && m.getName().equals(methodName)) {
						method = m;
						break;
					}
				}
			}

			if (method != null) {
				return (T) method.invoke(instance, params);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;
	}
}
