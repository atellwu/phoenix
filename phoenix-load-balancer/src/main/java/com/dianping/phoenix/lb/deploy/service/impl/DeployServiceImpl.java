/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.deploy.service.impl;

import java.util.List;

import com.dianping.phoenix.lb.deploy.DeployPlan;
import com.dianping.phoenix.lb.deploy.service.DeployService;
import com.dianping.phoenix.lb.exception.BizException;

/**
 * @author Leo Liang
 * 
 */
public class DeployServiceImpl implements DeployService {
    
    @Override
    public int deploy(String virtualServerName, String tagId, List<String> hosts, DeployPlan plan,
            String processDisplayUrl) throws BizException {
        // TODO Auto-generated method stub
        return 0;
    }

}
