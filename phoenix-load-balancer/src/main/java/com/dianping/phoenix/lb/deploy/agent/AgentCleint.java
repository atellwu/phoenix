package com.dianping.phoenix.lb.deploy.agent;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.model.AgentStatus;

/**
 * agentClient和executor內部需要使用service，使用ioc框架的get的方式去獲取（PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);）
 * service改爲使用plexus，action使用plexcus獲取service
 *
 */
public interface AgentCleint {
    /**
     * 执行发布运行
     */
    void execute();

    /**
     * 获取Agent的状态
     */
    AgentStatus getAgentStatus();

    /**
     * 获取agent的执行日志
     */
    String getRawLog();
}
