/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 20, 2013
 * 
 */
package com.dianping.phoenix.lb.service;

import com.dianping.phoenix.lb.exception.BizException;

/**
 * @author Leo Liang
 * 
 */
public interface DeployService {
    String deploy(String virtualServerName, int version) throws BizException;

    String rollBackAndRedeploy(String virtualServerName, String currentTagId) throws BizException;
}
