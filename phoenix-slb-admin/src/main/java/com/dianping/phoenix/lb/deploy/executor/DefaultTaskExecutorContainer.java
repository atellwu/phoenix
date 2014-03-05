package com.dianping.phoenix.lb.deploy.executor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.model.StateAction;
import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class DefaultTaskExecutorContainer implements TaskExecutorContainer {

    private static final Logger                   LOG       = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);

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
        try {
            List<DeployTaskBo> list = deployTaskService.getTasksByStateAction(StateAction.START);
            if (list != null && list.size() > 0) {
                for (DeployTaskBo deployTaskBo : list) {
                    TaskExecutor taskExecutor = this.submitTaskExecutor(deployTaskBo);
                    taskExecutor.start();
                }
            }
            list = deployTaskService.getTasksByStateAction(StateAction.PAUSE);
            if (list != null && list.size() > 0) {
                for (DeployTaskBo deployTaskBo : list) {
                    this.submitTaskExecutor(deployTaskBo);
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("Error when init Task, just ignore it. Somebody should start it manually.", e);
        }
    }

    @Override
    public TaskExecutor getTaskExecutor(long taskId) {
        return container.get(taskId);
    }

    @Override
    public TaskExecutor submitTaskExecutor(long taskId) throws BizException {
        DeployTaskBo deployTaskBo = deployTaskService.getTask(taskId);
        if (deployTaskBo == null) {
            throw new IllegalArgumentException("Task is not exist associate with id(" + taskId + ")");
        }
        return this.submitTaskExecutor(deployTaskBo);
    }

    @Override
    public TaskExecutor submitTaskExecutor(DeployTaskBo deployTaskBo) {
        TaskExecutor taskExecutor = new DefaultTaskExecutor(deployTaskBo, deployTaskService, virtualServerService, strategyService, agentSequenceService, configManager);

        TaskExecutor taskExecutor0 = this.container.putIfAbsent(deployTaskBo.getTask().getId(), taskExecutor);
        if (taskExecutor0 != null) {
            return taskExecutor0;
        } else {
            return taskExecutor;
        }

    }

}
