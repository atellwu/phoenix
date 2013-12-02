/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 20, 2013
 * 
 */
package com.dianping.phoenix.lb.deploy.task;

import java.util.List;

import com.dianping.phoenix.lb.deploy.DeploySetting;
import com.dianping.phoenix.lb.exception.BizException;

/**
 * @author Leo Liang
 * 
 */
public interface DeployExecutor {
    int deploy(String virtualServerName, List<String> hosts, DeploySetting plan, String processDisplayUrl)
            throws BizException;
}
