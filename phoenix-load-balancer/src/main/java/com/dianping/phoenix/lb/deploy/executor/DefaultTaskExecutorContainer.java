package com.dianping.phoenix.lb.deploy.executor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class DefaultTaskExecutorContainer implements TaskExecutorContainer {

    private Map<Long, TaskExecutor> container = new HashMap<Long, TaskExecutor>();

    @Autowired
    private DeployTaskService       deployTaskService;

    @Autowired
    private VirtualServerService    virtualServerService;

    @Autowired
    private StrategyService         strategyService;

    private ConfigManager           configManager;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    @Override
    public TaskExecutor getTaskExecutor(long taskId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskExecutor submitTaskExecutor(long taskId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskExecutor submitTaskExecutor(DeployTaskBo deployTaskBo) {
        // TODO Auto-generated method stub
        return null;
    }

}
