/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.deploy;

import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.deploy.DeployManager;
import com.dianping.phoenix.lb.deploy.DeployPlan;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
public class DeployServiceImpl implements DeployService {
    private final static String WAR_TYPE_TENGINE = "tengine";

    private DeployManager       deployManager;

    public void init() throws ComponentLookupException {
        deployManager = PlexusComponentContainer.INSTANCE.lookup(DeployManager.class);
    }

    @Override
    public int deploy(String virtualServerName, List<String> hosts, DeployPlan plan, String processDisplayUrl)
            throws BizException {
        plan.setWarType(WAR_TYPE_TENGINE);

        try {
            return deployManager.deploy(virtualServerName, hosts, plan, processDisplayUrl);
        } catch (Exception e) {
            ExceptionUtils.logAndRethrowBizException(e, MessageID.DEPLOY_EXCEPTION, virtualServerName);
        }

        return -1;

    }

}
