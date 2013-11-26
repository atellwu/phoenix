/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
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
public class DeployServiceImpl implements DeployService {

    @Override
    public int deploy(String virtualServerName, List<String> hosts, DeployPlan plan, String processDisplayUrl) throws BizException {
        // TODO Auto-generated method stub
        return 0;
    }

}
