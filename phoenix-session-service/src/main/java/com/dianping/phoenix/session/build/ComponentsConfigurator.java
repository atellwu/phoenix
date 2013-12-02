package com.dianping.phoenix.session.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.core.FileRecorder;
import com.dianping.phoenix.session.core.RecordFileManager;
import com.dianping.phoenix.session.core.RequestEventHandler;
import com.dianping.phoenix.session.core.RequestEventRecorder;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ConfigManager.class));
		all.add(C(RecordFileManager.class) //
				.req(ConfigManager.class));
		all.add(C(RequestEventRecorder.class, FileRecorder.class) //
				.req(RecordFileManager.class));
		all.add(C(RequestEventHandler.class) //
				.req(ConfigManager.class) //
				.req(RequestEventRecorder.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
