package com.dianping.phoenix.lb.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.shell.DefaultScriptExecutor;
import com.dianping.phoenix.lb.shell.ScriptExecutor;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();
      
      all.add(C(ConfigManager.class));
      all.add(C(ScriptExecutor.class, DefaultScriptExecutor.class).is(PER_LOOKUP));
      
      // move following line to top-level project if necessary
      all.add(C(JdbcDataSourceDescriptorManager.class) //
              .config(E("datasourceFile").value("/data/appdatas/phoenix/datasources.xml")));

      all.addAll(new PhoenixDatabaseConfigurator().defineComponents());

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
