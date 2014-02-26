package com.dianping.phoenix.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.context.DefaultEnvironment;
import com.dianping.phoenix.context.Environment;
import com.dianping.phoenix.log.LogInitializer;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.RequestIdHandler;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Environment.class, DefaultEnvironment.class));
		all.add(C(LogInitializer.class));

		all.add(C(PhoenixFilterHandler.class, RequestIdHandler.ID, RequestIdHandler.class));
		all.add(C(RequestEventDelegate.class).is(PER_LOOKUP));

		
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
