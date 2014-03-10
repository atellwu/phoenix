package com.dianping.phoenix;

import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.phoenix.config.ConfigServiceFactory;

public class PhoenixModule extends AbstractModule {
	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return null;
	}

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		// step 1: warm up configuration service
		ConfigServiceFactory.getConfig();
	}
}
