package com.dianping.phoenix.session.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.requestid.Bootstrap;
import com.dianping.phoenix.session.requestid.DefaultEventRecorder;
import com.dianping.phoenix.session.requestid.EventDelegateManager;
import com.dianping.phoenix.session.requestid.EventProcessor;
import com.dianping.phoenix.session.requestid.EventRecorder;
import com.dianping.phoenix.session.requestid.FileSystemManager;
import com.dianping.phoenix.session.requestid.RecordFileManager;
import com.dianping.phoenix.session.requestid.FileUploader;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ConfigManager.class));
		all.add(C(RecordFileManager.class) //
		      .req(ConfigManager.class));
		all.add(C(EventRecorder.class, DefaultEventRecorder.class) //
		      .req(RecordFileManager.class));
		all.add(C(RequestEventDelegate.class).is(PER_LOOKUP));
		all.add(C(EventDelegateManager.class));
		all.add(C(EventProcessor.class) //
		      .req(ConfigManager.class) //
		      .req(EventRecorder.class) //
		      .req(EventDelegateManager.class));
		all.add(C(Bootstrap.class) //
		      .req(EventDelegateManager.class));

		all.add(C(FileSystemManager.class) //
		      .req(ConfigManager.class));
		all.add(C(FileUploader.class) //
		      .req(ConfigManager.class, FileSystemManager.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}
}
