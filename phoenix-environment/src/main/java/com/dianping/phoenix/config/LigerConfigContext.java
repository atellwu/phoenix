package com.dianping.phoenix.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.liger.config.ConfigContext;
import com.dianping.phoenix.context.Context;
import com.dianping.phoenix.context.DefaultContext;
import com.dianping.phoenix.context.Environment;
import com.dianping.phoenix.context.ThreadLocalRegistry;

public class LigerConfigContext implements ConfigContext, Initializable {
	@Inject
	private Environment m_env;

	@Inject
	private ThreadLocalRegistry m_registry;

	private ThreadLocal<Context> m_context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new DefaultContext();
		}
	};

	private long m_creationTime;

	public LigerConfigContext() {
		m_creationTime = System.currentTimeMillis();
	}

	@Override
	public long getCreationTime() {
		return m_creationTime;
	}

	@Override
	public String getVariant(String name) {
		String value = m_context.get().getAttribute(name, null);

		if (value == null) {
			value = m_env.getAttribute(name, null);
		}

		return value;
	}

	@Override
	public void removeVariant(String name) {
		m_context.get().setAttribute(name, null);
	}

	@Override
	public void resetCreationTime() {
		m_creationTime = System.currentTimeMillis();
	}

	@Override
	public void setVariant(String name, String value) {
		m_context.get().setAttribute(name, value);
	}

	@Override
	public void initialize() throws InitializationException {
		m_registry.register(m_context);
	}
}