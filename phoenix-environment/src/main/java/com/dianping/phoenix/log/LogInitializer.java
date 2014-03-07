package com.dianping.phoenix.log;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.initialization.PhoenixInitializer;

public class LogInitializer implements PhoenixInitializer {
	@Inject
	private LoggerManager m_manager;

	public void bootstrap() {
		URL url = getClass().getResource("/META-INF/phoenix/log4j.xml");

		DOMConfigurator.configure(url);
	}

	@Override
	public void initialize() {
		m_manager.configure();
	}
}
