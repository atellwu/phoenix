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

	private Layout m_layout;

	@Override
	public Appender getAppender(String type, String name, String... parameters) {
		Pair<String, String> key = new Pair<String, String>(type, name);
		Pair<String[], Appender> value = m_map.get(key);

		if (value == null) {
			value = new Pair<String[], Appender>();
			m_map.put(key, value);
		}

		String[] oldParameters = value.getKey();

		if (oldParameters != null && !oldParameters.equals(parameters)) {
			throw new RuntimeException(String.format("Conflicting appenders(%s:%s:%s and %s:%s:%s) configured!", type,
			      name, Arrays.asList(oldParameters), type, name, Arrays.asList(parameters)));
		}

		Appender appender = makeAppender(type, name, parameters);

		value.setKey(parameters);
		value.setValue(appender);
		return appender;
	}

	private Appender makeAppender(String type, String name, String[] parameters) {
		AppenderBuilder builder = lookup(AppenderBuilder.class, type);
		Appender appender = builder.build(m_layout, name, parameters);

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
		String pattern = config.getString(LogConstants.KEY_CONVERSION_PATTERN,
		      LogConstants.DEFAULT_VALUE_CONVERSION_PATTERN);

		m_layout = new PatternLayout(pattern);
	}
}
