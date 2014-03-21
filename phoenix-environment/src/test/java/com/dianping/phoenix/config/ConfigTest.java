package com.dianping.phoenix.config;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.liger.Liger;
import com.dianping.liger.config.model.entity.LigerModel;
import com.dianping.liger.config.model.transform.DefaultSaxParser;
import com.dianping.liger.repository.git.GitRepositoryBuilder;
import com.dianping.phoenix.context.Environment;

public class ConfigTest extends ComponentTestCase {
	@After
	public void after() {
		ConfigServiceFactory.destroy();
	}

	@Test
	public void testMock() throws Exception {
		defineComponent(ConfigServiceProvider.class, MockMemoryProvider.class);

		ConfigService config = ConfigServiceFactory.getConfig();

		Assert.assertEquals("hello string", config.getString("string", null));
		Assert.assertEquals(true, config.getBoolean("boolean", false));
		Assert.assertEquals(1, config.getInt("int", 0));
		Assert.assertEquals(1L, config.getLong("long", 0));
		Assert.assertEquals(1.0f, config.getFloat("float", 0));
		Assert.assertEquals(1.0d, config.getDouble("double", 0));
		Assert.assertEquals(1392817680000L, config.getDate("date", "yyyy-MM-dd HH:mm:ss", null).getTime());
	}

	@Test
	public void testLiger() throws Exception {
		GitRepositoryBuilder builder = lookup(GitRepositoryBuilder.class);
		Environment env = lookup(Environment.class);
		LigerModel model = DefaultSaxParser.parse(getClass().getResourceAsStream("liger.xml"));
		String ligerHome = "target/liger";

		builder.buildClientRepository(ligerHome, model, "test resource");

		Liger.initialize(ligerHome, null);
		env.setAttribute("lane", "membercard");

		// try with Liger
		ConfigService cs = ConfigServiceFactory.getConfig();

		Assert.assertEquals("true", cs.getString("pc[default].101", null));
		Assert.assertEquals("false", cs.getString("pc[default].104", null));
	}

	public static class MockMemoryProvider implements ConfigServiceProvider {
		@Override
		public ConfigService getConfigService() {
			return new AbstractConfigService() {
				@Override
				public String getString(String key, String defaultValue) {
					if ("string".equals(key)) {
						return "hello string";
					} else if ("boolean".equals(key)) {
						return "true";
					} else if ("int".equals(key) || "long".equals(key) || "float".equals(key) || "double".equals(key)) {
						return "1";
					} else if ("date".equals(key)) {
						return "2014-02-19 21:48:00";
					} else {
						return "unknown key: " + key;
					}
				}

				@Override
				protected String getEnv(String name, String defaultValue) {
					return null;
				}
			};
		}
	}
}
