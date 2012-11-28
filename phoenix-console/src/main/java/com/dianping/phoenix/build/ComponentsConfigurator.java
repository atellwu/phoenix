package com.dianping.phoenix.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.unidal.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.configure.ConfigManager;
import com.dianping.phoenix.console.dal.deploy.DeploymentDao;
import com.dianping.phoenix.console.dal.deploy.DeploymentDetailsDao;
import com.dianping.phoenix.console.dal.deploy.VersionDao;
import com.dianping.phoenix.console.page.deploy.JspViewer;
import com.dianping.phoenix.console.page.deploy.KeepAliveViewer;
import com.dianping.phoenix.deploy.DefaultDeployExecutor;
import com.dianping.phoenix.deploy.DefaultDeployManager;
import com.dianping.phoenix.deploy.DeployExecutor;
import com.dianping.phoenix.deploy.DeployManager;
import com.dianping.phoenix.deploy.DeployPolicy;
import com.dianping.phoenix.service.DefaultGitService;
import com.dianping.phoenix.service.DefaultProjectManager;
import com.dianping.phoenix.service.DefaultStatusReporter;
import com.dianping.phoenix.service.DefaultVersionManager;
import com.dianping.phoenix.service.DefaultWarService;
import com.dianping.phoenix.service.GitService;
import com.dianping.phoenix.service.GitProgressMonitor;
import com.dianping.phoenix.service.ProjectManager;
import com.dianping.phoenix.service.StatusReporter;
import com.dianping.phoenix.service.VersionManager;
import com.dianping.phoenix.service.WarService;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ConfigManager.class));

		defineServiceComponents(all);
		defineDatabaseComponents(all);
		defineWebComponents(all);

		return all;
	}

	private void defineDatabaseComponents(List<Component> all) {
		// setup datasource configuration manager
		all.add(C(JdbcDataSourceConfigurationManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/phoenix/datasources.xml")));

		// Phoenix database
		all.addAll(new PhoenixDatabaseConfigurator().defineComponents());
	}

	private void defineServiceComponents(List<Component> all) {
		all.add(C(StatusReporter.class, DefaultStatusReporter.class));

		all.add(C(WarService.class, DefaultWarService.class) //
		      .req(ConfigManager.class, StatusReporter.class));
		all.add(C(GitService.class, DefaultGitService.class) //
		      .req(ConfigManager.class, StatusReporter.class, ProgressMonitor.class));
		all.add(C(ProgressMonitor.class, GitProgressMonitor.class));

		all.add(C(VersionManager.class, DefaultVersionManager.class) //
		      .req(WarService.class, GitService.class, VersionDao.class));
		all.add(C(ProjectManager.class, DefaultProjectManager.class) //
		      .req(DeployManager.class));

		for (DeployPolicy policy : DeployPolicy.values()) {
			all.add(C(DeployExecutor.class, policy.getId(), DefaultDeployExecutor.class) //
			      .req(ConfigManager.class) //
			      .config(E("policy").value(policy.name())));
		}

		all.add(C(DeployManager.class, DefaultDeployManager.class) //
		      .req(DeploymentDao.class, DeploymentDetailsDao.class));
	}

	private void defineWebComponents(List<Component> all) {
		all.add(C(KeepAliveViewer.class).is(PER_LOOKUP) //
		      .req(JspViewer.class, DeployManager.class));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());
	}
}
