package com.dianping.phoenix.agent;

import java.util.HashMap;
import java.util.Map;

public class ObjectHelper {
	private static Map<String, Object> s_map = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public static <T> T get(String name) {
		return (T) s_map.get(name);
	}

	public static void remove(String name) {
		s_map.remove(name);
	}

	public static void set(String name, Object obj) {
		s_map.put(name, obj);
	}
}
