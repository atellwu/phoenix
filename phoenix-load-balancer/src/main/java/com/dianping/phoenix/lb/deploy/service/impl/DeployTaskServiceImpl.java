package com.dianping.phoenix.lb.deploy.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.ibatis.session.RowBounds;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.dao.DeploymentDetailMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentTaskMapper;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;
import com.dianping.phoenix.lb.deploy.model.DeploymentTaskExample;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;

@Service
public class DeployTaskServiceImpl implements DeployTaskService {

    private static final int       PAGE_SIZE = 20;

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
    public List<DeploymentTask> list(int pageNum) {
        DeploymentTaskExample example = new DeploymentTaskExample();
        example.setOrderByClause("creation_date desc");
        RowBounds rowBounds = new RowBounds(PAGE_SIZE * (pageNum - 1), PAGE_SIZE);
        return deploymentTaskMapper.selectByExampleWithRowbounds(example, rowBounds);
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
