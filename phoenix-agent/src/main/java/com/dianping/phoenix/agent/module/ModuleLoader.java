package com.dianping.phoenix.agent.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ModuleLoader {

	private static Logger logger = Logger.getLogger(ModuleLoader.class);
	private static ModuleLoader ins = new ModuleLoader();

	public static ModuleLoader getInstance() {
		return ins;
	}

	private ModuleLoader() {

	}

	public void load() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/module.properties");

		if (in == null) {
			logger.warn("No module.properties found on classpath");
		} else {
			Properties props = new Properties();
			props.load(in);
			for (String className : props.stringPropertyNames()) {
				Class<?> clazz = null;
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.warn(String.format("Class %s not found, skip it", className));
					continue;
				}

				if (Runnable.class.isAssignableFrom(clazz)) {
					try {
						Runnable runnable = (Runnable) clazz.newInstance();
						Thread t = new Thread(runnable);
						t.setName("module-" + clazz.getSimpleName());
						logger.info(String.format("Start %s as module", className));
						t.start();
					} catch (Exception e) {
						logger.error(String.format("Error construct %s by default constructor, skip it", className), e);
						continue;
					}
				} else {
					logger.error(String.format("%s is not a Runnable, skip it", className));
				}
			}
		}

	}

	public static void main(String[] args) throws IOException {
		ModuleLoader.getInstance().load();
	}

}
