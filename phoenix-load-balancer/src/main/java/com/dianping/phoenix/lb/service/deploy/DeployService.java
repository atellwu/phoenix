/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 20, 2013
 * 
 */
package com.dianping.phoenix.lb.service.deploy;

import java.util.List;

import com.dianping.phoenix.lb.deploy.DeployPlan;
import com.dianping.phoenix.lb.exception.BizException;

/**
 * @author Leo Liang
 * 
 */
public interface DeployService {
    int deploy(String virtualServerName, List<String> hosts, DeployPlan plan, String processDisplayUrl)
            throws BizException;
}
