package com.dianping.phoenix.service.resource;

import java.util.ArrayList;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.phoenix.agent.resource.entity.Container;
import com.dianping.phoenix.agent.resource.entity.Domain;
import com.dianping.phoenix.agent.resource.entity.Host;
import com.dianping.phoenix.agent.resource.entity.PhoenixAgent;
import com.dianping.phoenix.agent.resource.entity.Product;
import com.dianping.phoenix.agent.resource.entity.Resource;

public class MockResourceManager extends DefaultResourceManager {
	private Resource m_resource = new Resource();

	@Override
	public void initialize() throws InitializationException {
		Product product = new Product();
		product.setName("PhoenixTest");

		Domain domain = new Domain();
		domain.setName("user-web");

		Host host = new Host();
		host.setIp("192.168.22.114");// Don't use 127.0.0.1 or Localhost
		host.setEnv("dev");
		host.setOwner("tong.song");
		host.setStatus("up");

		Host host2 = new Host();
		host2.setIp("127.0.0.1");// Don't use 127.0.0.1 or Localhost
		host2.setEnv("dev");
		host2.setOwner("tong.song");
		host2.setStatus("up");

		PhoenixAgent agent = new PhoenixAgent();
		agent.setVersion("0.0.1");
		agent.setStatus("ok");

		host.setPhoenixAgent(agent);
		host2.setPhoenixAgent(agent);

		Container container = new Container();
		container.setInstallPath("/usr/local/tomcat");
		container.setStatus("up");
		container.setType("tomcat");
		container.setVersion("1.6.2");

		host.setContainer(container);
		host2.setContainer(container);

		Domain domain2 = new Domain("tuangou-web");
		domain2.addHost(host);
		domain2.addHost(host2);

		domain.addHost(host);
		domain.addHost(host2);
		product.addDomain(domain);
		product.addDomain(domain2);
		m_resource.addProduct(product);
		m_agentStatusFetcher.fetchPhoenixAgentStatus(new ArrayList<Host>(domain.getHosts().values()));

		super.generateMetaInformation(m_resource);
	}

	@Override
	public Resource getResource() {
		return m_resource;
	}
}
