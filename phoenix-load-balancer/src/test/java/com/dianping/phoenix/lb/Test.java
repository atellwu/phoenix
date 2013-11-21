/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 20, 2013
 * 
 */
package com.dianping.phoenix.lb;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.phoenix.lb.dal.deploy.DeploymentDao;
import com.dianping.phoenix.lb.dal.deploy.DeploymentEntity;

/**
 * @author Leo Liang
 * 
 */
public class Test {
    public static void main(String[] args) throws Exception {
        PlexusContainer container = ContainerLoader.getDefaultContainer();
        DeploymentDao deploymentDao = container.lookup(DeploymentDao.class);
        System.out.println(deploymentDao.findByPK(1, DeploymentEntity.READSET_FULL));
    }
}
