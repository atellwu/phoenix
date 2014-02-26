package com.dianping.phoenix.log;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;

import com.dianping.phoenix.initialization.PhoenixInitializer;

public class LogInitializer implements PhoenixInitializer {
	public static final String ID = "log";

	public void bootstrap() {
		URL url = getClass().getResource("/META-INF/phoenix/log4j.xml");

		DOMConfigurator.configure(url);
	}

	@Override
	public void initialize() {
	}
}
