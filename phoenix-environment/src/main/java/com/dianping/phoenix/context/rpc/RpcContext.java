package com.dianping.phoenix.context.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dianping.phoenix.context.Context;

public class RpcContext implements Context {
	private static ThreadLocal<RpcContext> s_context = new ThreadLocal<RpcContext>() {
		@Override
		protected RpcContext initialValue() {
			return new RpcContext();
		}
	};

	private Map<String, String> m_attributes = new HashMap<String, String>();

	private RpcContext() {
	}

	public static RpcContext get() {
		return s_context.get();
	}

	public static void reset() {
		s_context.remove();
	}

	@Override
	public String getAttribute(String name) {
		return m_attributes.get(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return m_attributes.keySet();
	}

	@Override
	public void setAttribute(String name, String value) {
		m_attributes.put(name, value);
	}
}
