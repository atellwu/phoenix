package com.dianping.phoenix.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.environment.PhoenixEnvironmentFilter;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;
import com.dianping.phoenix.session.RequestEventDelegate;
import com.dianping.phoenix.session.RequestIdHandler;
import com.dianping.phoenix.session.server.DefaultEventPublisher;
import com.dianping.phoenix.session.server.DefaultServerAddressManager;
import com.dianping.phoenix.session.server.DefaultSocketClientManager;
import com.dianping.phoenix.session.server.EventPublisher;
import com.dianping.phoenix.session.server.ServerAddressManager;
import com.dianping.phoenix.session.server.SocketClientManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(PhoenixFilterHandler.class, PhoenixEnvironmentFilter.ID, PhoenixEnvironmentFilter.class));
		all.add(C(PhoenixFilterHandler.class, RequestIdHandler.ID, RequestIdHandler.class));
		all.add(C(RequestEventDelegate.class).is(PER_LOOKUP));
		all.add(C(ServerAddressManager.class, DefaultServerAddressManager.class));
		all.add(C(SocketClientManager.class, DefaultSocketClientManager.class) //
		      .config(E("m_strMode").value(SocketClientManager.Mode.Single.toString())));
		all.add(C(EventPublisher.class, DefaultEventPublisher.class) //
		      .req(SocketClientManager.class) //
		      .req(ServerAddressManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
