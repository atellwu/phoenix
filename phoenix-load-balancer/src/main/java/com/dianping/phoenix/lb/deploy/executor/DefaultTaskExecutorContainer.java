package com.dianping.phoenix.lb.deploy.executor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class DefaultTaskExecutorContainer implements TaskExecutorContainer {

    private ConcurrentHashMap<Long, TaskExecutor> container = new ConcurrentHashMap<Long, TaskExecutor>();

    @Autowired
    private DeployTaskService                     deployTaskService;

    @Autowired
    private VirtualServerService                  virtualServerService;

    @Autowired
    private StrategyService                       strategyService;

    @Autowired
    private AgentSequenceService                  agentSequenceService;

    private ConfigManager                         configManager;

    @PostConstruct
    public void init() throws ComponentLookupException, BizException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
        //获取数据库中，状态为READY的task，将其启动起来
        List<DeployTaskBo> list = deployTaskService.getTasks(DeployTaskStatus.READY);
        if (list != null && list.size() > 0) {
            for (DeployTaskBo deployTaskBo : list) {
                this.submitTaskExecutor(deployTaskBo);
            }
        }
    }

    @Override
    public TaskExecutor getTaskExecutor(long taskId) {
        return container.get(taskId);
    }

    @Override
    public TaskExecutor submitTaskExecutor(long taskId) throws BizException {
        DeployTaskBo deployTaskBo = deployTaskService.getTask(taskId);
        return this.submitTaskExecutor(deployTaskBo);
    }

    @Override
    public TaskExecutor submitTaskExecutor(DeployTaskBo deployTaskBo) {
        TaskExecutor taskExecutor = new DefaultTaskExecutor(deployTaskBo, deployTaskService, virtualServerService, strategyService, agentSequenceService, configManager);

        return this.container.putIfAbsent(deployTaskBo.getTask().getId(), taskExecutor);

    }

}
