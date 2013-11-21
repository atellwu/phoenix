package com.dianping.phoenix.lb.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class PhoenixDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(defineJdbcDataSourceComponent("phoenix", "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.22.71/phoenix", "root", "root", "<![CDATA[useUnicode=true&characterEncoding=UTF-8&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "phoenix", com.dianping.phoenix.lb.dal.deploy._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.phoenix.lb.dal.deploy._INDEX.getDaoClasses());

      return all;
   }
}
