package com.dianping.phoenix.lb.deploy.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.Validate;
import org.apache.ibatis.session.RowBounds;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.action.Paginator;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo.VsAndTag;
import com.dianping.phoenix.lb.deploy.dao.DeployAgentMapper;
import com.dianping.phoenix.lb.deploy.dao.DeployTaskMapper;
import com.dianping.phoenix.lb.deploy.dao.DeployVsMapper;
import com.dianping.phoenix.lb.deploy.model.DeployAgent;
import com.dianping.phoenix.lb.deploy.model.DeployAgentExample;
import com.dianping.phoenix.lb.deploy.model.DeployTask;
import com.dianping.phoenix.lb.deploy.model.DeployTaskExample;
import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
import com.dianping.phoenix.lb.deploy.model.DeployVs;
import com.dianping.phoenix.lb.deploy.model.DeployVsExample;
import com.dianping.phoenix.lb.deploy.model.DeployVsStatus;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class DeployTaskServiceImpl implements DeployTaskService {

    private static final int     PAGE_SIZE    = 15;

    @SuppressWarnings("unused")
    private ConfigManager        configManager;

    @Autowired
    private DeployVsMapper       deployVsMapper;

    @Autowired
    private DeployAgentMapper    deployAgentMapper;

    @Autowired
    private DeployTaskMapper     deployTaskMapper;

    @Autowired
    private VirtualServerService virtualServerService;

    private int                  MAX_PAGE_NUM = 50;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    @Override
    public List<DeployTask> list(Paginator paginator, int pageNum) {
        if (pageNum > MAX_PAGE_NUM || pageNum <= 0) {
            pageNum = 1;
        }

        DeployTaskExample example = new DeployTaskExample();
        example.setOrderByClause("creation_date DESC");

        int count = deployTaskMapper.countByExample(example);

        paginator.setItemsPerPage(PAGE_SIZE);
        paginator.setItems(count);
        paginator.setPage(pageNum);

        if (pageNum > paginator.getLastPage()) {
            pageNum = paginator.getLastPage();
        }
        int offset = PAGE_SIZE * (pageNum - 1);
        int limit = PAGE_SIZE;
        RowBounds rowBounds = new RowBounds(offset, limit);

        return deployTaskMapper.selectByExampleWithRowbounds(example, rowBounds);
    }

    @Override
    public DeployTaskBo getTask(long taskId) throws BizException {
        DeployTaskBo task = new DeployTaskBo();

        DeployTask deployVsTask = deployTaskMapper.selectByPrimaryKey(taskId);

        DeployVsExample example = new DeployVsExample();
        example.createCriteria().andDeployTaskIdEqualTo(taskId);
        List<DeployVs> deployVsList = deployVsMapper.selectByExample(example);

        List<DeployVsBo> deployVsBos = new ArrayList<DeployVsBo>();
        for (DeployVs deployVs : deployVsList) {
            DeployVsBo deployVsBo = new DeployVsBo();
            DeployAgentExample example2 = new DeployAgentExample();
            example2.createCriteria().andDeployVsIdEqualTo(deployVs.getId());
            List<DeployAgent> deployAgents = deployAgentMapper.selectByExample(example2);
            VirtualServer vs = virtualServerService.findVirtualServer(deployVs.getVsName());

            deployVsBo.setDeployVs(deployVs);
            deployVsBo.setVs(vs);
            deployVsBo.setDeployAgents(convertDetailsToMap(deployAgents));

            deployVsBos.add(deployVsBo);
        }

        task.setTask(deployVsTask);
        task.setDeployVsBos(convertDeployVssToMap(deployVsBos));

        return task;
    }

    private Map<String, DeployVsBo> convertDeployVssToMap(List<DeployVsBo> deployVsBos) {
        Map<String, DeployVsBo> map = new HashMap<String, DeployVsBo>();
        for (DeployVsBo deployVsBo : deployVsBos) {
            String vsName = deployVsBo.getVs().getName();
            map.put(vsName, deployVsBo);
        }
        return map;
    }

    private Map<String, DeployAgent> convertDetailsToMap(List<DeployAgent> deployAgents) {
        Map<String, DeployAgent> map = new HashMap<String, DeployAgent>();
        for (DeployAgent deployAgent : deployAgents) {
            String ip = deployAgent.getIpAddress();
            map.put(ip, deployAgent);
        }
        return map;
    }

    @Override
    public void createTask() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTask() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addTask(NewTaskInfo newTaskInfo) {
        validate(newTaskInfo);
        DeployTask task = new DeployTask();
        task.setName(newTaskInfo.getTaskName());
        task.setLastModifiedDate(new Date());
        task.setStatus(DeployTaskStatus.CREATED);
        deployTaskMapper.insert(task);

        for (VsAndTag vsAndTag : newTaskInfo.getSelectedVsAndTag()) {
            DeployVs deployVs = new DeployVs();
            deployVs.setVsName(vsAndTag.getVsName());
            deployVs.setVsTag(vsAndTag.getTag());
            deployVs.setDeployTaskId(task.getId());
            deployVs.setStatus(DeployVsStatus.CREATED);
            deployVs.setLastModifiedDate(new Date());
            deployVsMapper.insertSelective(deployVs);
        }
    }

    private void validate(NewTaskInfo newTaskInfo) {
        Validate.notEmpty(newTaskInfo.getTaskName(), "Task's name can not be empty!");
        Validate.notEmpty(newTaskInfo.getSelectedVsAndTag(), "Must add one vs and tag at least !");
        Set<String> set = new HashSet<String>();
        for (VsAndTag vsAndTag : newTaskInfo.getSelectedVsAndTag()) {
            Validate.notEmpty(vsAndTag.getVsName(), "Vs's name can not be empty!");
            Validate.notEmpty(vsAndTag.getTag(), "Vs's tag can not be empty!");
            Validate.isTrue(set.add(vsAndTag.getVsName()), "Vs's name can not be duplicate！");
        }
    }

}
