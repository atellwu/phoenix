package com.dianping.phoenix.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LoggerManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.log.Log4jLoggerManager;

public class PlexusConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(LoggerManager.class, Log4jLoggerManager.class));

		return all;
	}

	protected File getConfigurationFile() {
		return new File("src/main/resources/META-INF/plexus/plexus.xml");
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new PlexusConfigurator());
	}
}
