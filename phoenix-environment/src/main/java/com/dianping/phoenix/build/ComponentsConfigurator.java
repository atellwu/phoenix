package com.dianping.phoenix.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.liger.config.Config;
import com.dianping.liger.config.ConfigContext;
import com.dianping.liger.config.ConfigEnvironment;
import com.dianping.phoenix.config.ConfigServiceProvider;
import com.dianping.phoenix.config.LigerConfigContext;
import com.dianping.phoenix.config.LigerConfigEnvironment;
import com.dianping.phoenix.config.LigerConfigServiceProvider;
import com.dianping.phoenix.context.DefaultEnvironment;
import com.dianping.phoenix.context.Environment;
import com.dianping.phoenix.log.AppenderBuilder;
import com.dianping.phoenix.log.AppenderManager;
import com.dianping.phoenix.log.ConsoleAppenderBuilder;
import com.dianping.phoenix.log.DefaultAppenderManager;
import com.dianping.phoenix.log.DefaultLoggerManager;
import com.dianping.phoenix.log.FileAppenderBuilder;
import com.dianping.phoenix.log.LogInitializer;
import com.dianping.phoenix.log.LoggerManager;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.RequestIdHandler;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Environment.class, DefaultEnvironment.class));

		all.addAll(defineLogComponents());
		all.addAll(defineConfigComponents());

		all.add(C(PhoenixFilterHandler.class, RequestIdHandler.ID, RequestIdHandler.class));
		all.add(C(RequestEventDelegate.class).is(PER_LOOKUP));

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ConfigServiceProvider.class, LigerConfigServiceProvider.class));
		all.add(C(ConfigContext.class, LigerConfigContext.class));
		all.add(C(ConfigEnvironment.class, LigerConfigEnvironment.class) //
		      .req(Environment.class));

		return all;
	}

	private List<Component> defineLogComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(LogInitializer.class) //
		      .req(LoggerManager.class));

		all.add(C(LoggerManager.class, DefaultLoggerManager.class) //
		      .req(Config.class, AppenderManager.class));
		all.add(C(AppenderManager.class, DefaultAppenderManager.class));
		all.add(C(AppenderBuilder.class, ConsoleAppenderBuilder.ID, ConsoleAppenderBuilder.class));
		all.add(C(AppenderBuilder.class, FileAppenderBuilder.ID, FileAppenderBuilder.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
