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
import com.dianping.phoenix.session.requestid.FileUploader;
import com.dianping.phoenix.session.requestid.RecordFileManager;
import com.dianping.phoenix.session.requestid.serverevent.DefaultServerAddressManager;
import com.dianping.phoenix.session.server.DefaultEventPublisher;
import com.dianping.phoenix.session.server.DefaultSocketClientManager;
import com.dianping.phoenix.session.server.EventPublisher;
import com.dianping.phoenix.session.server.ServerAddressManager;
import com.dianping.phoenix.session.server.SocketClientManager;

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
		all.add(C(Bootstrap.class));
		all.add(C(ServerAddressManager.class, DefaultServerAddressManager.class) //
		      .req(ConfigManager.class));
		all.add(C(SocketClientManager.class, DefaultSocketClientManager.class) //
		      .config(E("m_strMode").value(SocketClientManager.Mode.Broadcast.toString())));
		all.add(C(EventPublisher.class, DefaultEventPublisher.class) //
		      .req(SocketClientManager.class) //
		      .req(ServerAddressManager.class));

		all.add(C(FileSystemManager.class) //
		      .req(ConfigManager.class));
		all.add(C(FileUploader.class) //
		      .req(ConfigManager.class, FileSystemManager.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}
}
