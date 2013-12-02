package com.dianping.phoenix.lb.deploy.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.ibatis.session.RowBounds;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.action.Paginator;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.dao.DeploymentDetailMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentTaskMapper;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;
import com.dianping.phoenix.lb.deploy.model.DeploymentTaskExample;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;

@Service
public class DeployTaskServiceImpl implements DeployTaskService {

    private static final int       PAGE_SIZE    = 15;

    private ConfigManager          configManager;

    @Autowired
    private DeploymentMapper       deploymentMapper;

    @Autowired
    private DeploymentDetailMapper deploymentDetailMapper;

    @Autowired
    private DeploymentTaskMapper   deploymentTaskMapper;

    private int                    MAX_PAGE_NUM = 50;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    @Override
    public List<DeploymentTask> list(Paginator paginator, int pageNum) {
        if (pageNum > MAX_PAGE_NUM || pageNum <= 0) {
            pageNum = 1;
        }

        DeploymentTaskExample example = new DeploymentTaskExample();
        example.setOrderByClause("creation_date DESC");

        int count = deploymentTaskMapper.countByExample(example);

        paginator.setItemsPerPage(PAGE_SIZE);
        paginator.setItems(count);
        paginator.setPage(pageNum);

        if (pageNum > paginator.getLastPage()) {
            pageNum = paginator.getLastPage();
        }
        int offset = PAGE_SIZE * (pageNum - 1);
        int limit = PAGE_SIZE;
        RowBounds rowBounds = new RowBounds(offset, limit);

        return deploymentTaskMapper.selectByExampleWithRowbounds(example, rowBounds);
    }

    @Override
    public DeploymentTask getTask(int taskId) {
        return deploymentTaskMapper.selectByPrimaryKey(taskId);
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
