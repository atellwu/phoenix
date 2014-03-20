package com.dianping.phoenix.log;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.tuple.Pair;

import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceFactory;

public class DefaultAppenderManager extends ContainerHolder implements AppenderManager, Initializable {
	private Map<Pair<String, String>, Pair<String[], Appender>> m_map = new LinkedHashMap<Pair<String, String>, Pair<String[], Appender>>();

	private Layout m_appLayout;

	private Layout m_bizLayout;

	@Override
	public Appender getAppender(String type, String name, String... parameters) {
		Pair<String, String> key = new Pair<String, String>(type, name);
		Pair<String[], Appender> value = m_map.get(key);

		if (value == null) {
			value = new Pair<String[], Appender>();
			m_map.put(key, value);
		}

		String[] oldParameters = value.getKey();

		if (oldParameters != null && !Arrays.equals(oldParameters, parameters)) {
			throw new RuntimeException(String.format("Conflicting appenders(%s:%s:%s and %s:%s:%s) configured!", type,
			      name, Arrays.asList(oldParameters), type, name, Arrays.asList(parameters)));
		}

		if (value.getValue() == null) {
			Appender appender = makeAppender(type, name, parameters);

			value.setKey(parameters);
			value.setValue(appender);
		}

		return value.getValue();
	}

	private Appender makeAppender(String type, String name, String[] parameters) {
		AppenderBuilder builder = lookup(AppenderBuilder.class, type);
		Appender appender;

		if (type.equals("biz")) {
			appender = builder.build(m_bizLayout, name, parameters);
		} else {
			appender = builder.build(m_appLayout, name, parameters);
		}

		if (name == null) {
			appender.setName(type);
		} else {
			appender.setName(type + "-" + name);
		}

		return appender;
	}

	@Override
	public void initialize() throws InitializationException {
		ConfigService config = ConfigServiceFactory.getConfig();
		String appPattern = config.getString(LogConstants.KEY_APP_CONVERSION_PATTERN,
		      LogConstants.DEFAULT_VALUE_APP_CONVERSION_PATTERN);
		String bizPattern = config.getString(LogConstants.KEY_BIZ_CONVERSION_PATTERN,
		      LogConstants.DEFAULT_VALUE_BIZ_CONVERSION_PATTERN);

		m_appLayout = new PatternLayout(appPattern);
		m_bizLayout = new PatternLayout(bizPattern);
	}
}
