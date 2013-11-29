package com.dianping.phoenix.lb.deploy.service.impl;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.dao.DeploymentDetailMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentTaskMapper;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;

@Service
public class DeployTaskServiceImpl implements DeployTaskService {

    private ConfigManager          configManager;

    @Autowired
    private DeploymentMapper       deploymentMapper;

    @Autowired
    private DeploymentDetailMapper deploymentDetailMapper;

    @Autowired
    private DeploymentTaskMapper   deploymentTaskMapper;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    @Override
    public void list(int pageNum) {
        // TODO Auto-generated method stub
        //        deploymentTaskDao.
    }

    @Override
    public void getTask(int taskId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createTask() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTask() {
        // TODO Auto-generated method stub

    }

}
