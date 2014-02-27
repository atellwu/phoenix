package com.dianping.phoenix.service.config;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.liger.Liger;
import com.dianping.liger.config.ConfigContext;
import com.dianping.liger.config.ConfigEnvironment;
import com.dianping.liger.config.model.entity.LigerModel;
import com.dianping.liger.config.model.transform.DefaultSaxParser;
import com.dianping.liger.repository.git.GitRepositoryBuilder;
import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceFactory;
import com.dianping.phoenix.config.ConfigServiceProvider;
import com.dianping.phoenix.context.ContextManager;
import com.dianping.phoenix.context.Environment;

public class LigerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		defineComponent(ConfigServiceProvider.class, LigerConfigServiceProvider.class);
		defineComponent(ConfigContext.class, LigerConfigContext.class);
		defineComponent(ConfigEnvironment.class, LigerConfigEnvironment.class) //
		      .req(Environment.class);

		GitRepositoryBuilder builder = lookup(GitRepositoryBuilder.class);
		LigerModel model = DefaultSaxParser.parse(getClass().getResourceAsStream("liger.xml"));
		String ligerHome = "target/liger";

		builder.buildClientRepository(ligerHome, model, "test resource");

		Liger.initialize(ligerHome, null);
		ContextManager.getEnvironment().setAttribute("lane", "membercard");

		// try with Liger
		ConfigService cs = ConfigServiceFactory.getConfig();

		Assert.assertEquals("true", cs.getString("pc[default].101", null));
		Assert.assertEquals("false", cs.getString("pc[default].104", null));
	}
}
