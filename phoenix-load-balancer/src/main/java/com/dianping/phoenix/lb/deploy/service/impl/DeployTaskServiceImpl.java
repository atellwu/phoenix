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
import com.dianping.phoenix.lb.deploy.bo.DeploymentBo;
import com.dianping.phoenix.lb.deploy.bo.DeploymentTaskBo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo.VsAndTag;
import com.dianping.phoenix.lb.deploy.dao.DeploymentDetailMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentMapper;
import com.dianping.phoenix.lb.deploy.dao.DeploymentTaskMapper;
import com.dianping.phoenix.lb.deploy.model.DeployStatus;
import com.dianping.phoenix.lb.deploy.model.Deployment;
import com.dianping.phoenix.lb.deploy.model.DeploymentDetail;
import com.dianping.phoenix.lb.deploy.model.DeploymentDetailExample;
import com.dianping.phoenix.lb.deploy.model.DeploymentExample;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;
import com.dianping.phoenix.lb.deploy.model.DeploymentTaskExample;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class DeployTaskServiceImpl implements DeployTaskService {

    private static final int       PAGE_SIZE    = 15;

    @SuppressWarnings("unused")
    private ConfigManager          configManager;

    @Autowired
    private DeploymentMapper       deploymentMapper;

    @Autowired
    private DeploymentDetailMapper deploymentDetailMapper;

    @Autowired
    private DeploymentTaskMapper   deploymentTaskMapper;

    @Autowired
    private VirtualServerService   virtualServerService;

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
    public DeploymentTaskBo getTask(int taskId) throws BizException {
        DeploymentTaskBo task = new DeploymentTaskBo();

        DeploymentTask deploymentTask = deploymentTaskMapper.selectByPrimaryKey(taskId);

        DeploymentExample example = new DeploymentExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        List<Deployment> deployments = deploymentMapper.selectByExample(example);

        List<DeploymentBo> deploymentBos = new ArrayList<DeploymentBo>();
        for (Deployment deployment : deployments) {
            DeploymentBo deploymentBo = new DeploymentBo();
            DeploymentDetailExample example2 = new DeploymentDetailExample();
            example2.createCriteria().andDeployIdEqualTo(deployment.getId());
            List<DeploymentDetail> deploymentDetails = deploymentDetailMapper.selectByExample(example2);
            VirtualServer vs = virtualServerService.findVirtualServer(deployment.getVs());

            deploymentBo.setDeployment(deployment);
            deploymentBo.setVs(vs);
            deploymentBo.setDeploymentDetails(convertDetailsToMap(deploymentDetails));

            deploymentBos.add(deploymentBo);
        }

        task.setTask(deploymentTask);
        task.setDeploymentBos(convertDeploymentsToMap(deploymentBos));

        return task;
    }

    private Map<String, DeploymentBo> convertDeploymentsToMap(List<DeploymentBo> deploymentBos) {
        Map<String, DeploymentBo> map = new HashMap<String, DeploymentBo>();
        for (DeploymentBo deploymentBo : deploymentBos) {
            String vsName = deploymentBo.getVs().getName();
            map.put(vsName, deploymentBo);
        }
        return map;
    }

    private Map<String, DeploymentDetail> convertDetailsToMap(List<DeploymentDetail> deploymentDetails) {
        Map<String, DeploymentDetail> map = new HashMap<String, DeploymentDetail>();
        for (DeploymentDetail deploymentDetail : deploymentDetails) {
            String ip = deploymentDetail.getIpAddress();
            map.put(ip, deploymentDetail);
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
        DeploymentTask task = new DeploymentTask();
        task.setName(newTaskInfo.getTaskName());
        task.setLastModifiedDate(new Date());
        task.setStatus(DeployStatus.CREATED);
        deploymentTaskMapper.insert(task);

        for (VsAndTag vsAndTag : newTaskInfo.getSelectedVsAndTag()) {
            Deployment deployment = new Deployment();
            deployment.setVs(vsAndTag.getVsName());
            deployment.setTag(vsAndTag.getTag());
            deployment.setTaskId(task.getId());
            deployment.setStatus(DeployStatus.CREATED);
            deployment.setLastModifiedDate(new Date());
            deploymentMapper.insertSelective(deployment);
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
