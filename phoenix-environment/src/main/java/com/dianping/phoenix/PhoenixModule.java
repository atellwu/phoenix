package com.dianping.phoenix;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.phoenix.config.ConfigServiceFactory;
import com.dianping.phoenix.context.EnvironmentManager;
import com.dianping.phoenix.log.LoggerManager;

public class PhoenixModule extends AbstractModule {
	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return null;
	}

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		// step 0: setup environment
		ctx.lookup(EnvironmentManager.class).configure();

		// step 1: warm up configuration service
		ConfigServiceFactory.getConfig();

		// step 2: configure log
		ctx.lookup(LoggerManager.class).initialize();
	}
}
