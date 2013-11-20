/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.impl;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.service.DeployService;

/**
 * @author Leo Liang
 * 
 */
public class DeployServiceImpl implements DeployService {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.DeployService#deploy(java.lang.String,
     * int)
     */
    @Override
    public String deploy(String virtualServerName, int version) throws BizException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.DeployService#rollBackAndRedeploy(java
     * .lang.String, java.lang.String)
     */
    @Override
    public String rollBackAndRedeploy(String virtualServerName, String currentTagId) throws BizException {
        // TODO Auto-generated method stub
        return null;
    }

}
