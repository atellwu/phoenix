package com.dianping.phoenix.config.liger;

import com.dianping.liger.config.ConfigContext;
import com.dianping.phoenix.context.Context;
import com.dianping.phoenix.context.ContextManager;

public class LigerConfigContext implements ConfigContext {
	private Context m_context;

	private long m_creationTime;

	public LigerConfigContext() {
		m_context = ContextManager.getContext();
		m_creationTime = System.currentTimeMillis();
	}

	@Override
	public long getCreationTime() {
		return m_creationTime;
	}

	@Override
	public String getVariant(String name) {
		return m_context.getAttribute(name);
	}

	@Override
	public void removeVariant(String name) {
		m_context.setAttribute(name, null);
	}

	@Override
	public void resetCreationTime() {
		m_creationTime = System.currentTimeMillis();
	}

	@Override
	public void setVariant(String name, String value) {
		m_context.setAttribute(name, value);
	}
}