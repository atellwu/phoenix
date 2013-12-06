package com.dianping.phoenix.lb.deploy.agent;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;

/**
 * agentClient內部需要使用service(比如访问tag，生成config)，使用ioc框架的get的方式去獲取（PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);）
 * service改爲使用plexus，action使用plexcus獲取service
 *
 * agenCleint里面不会使用线程。发布操作在execute方法里一次性执行。
 */
public interface AgentClient {
    /**
     * 执行发布运行
     */
    void execute();

    /**
     * 获取Agent的状态
     */
    DeployAgentStatus getAgentStatus();

    /**
     * 获取agent的执行日志
     */
    String getRawLog();
}
