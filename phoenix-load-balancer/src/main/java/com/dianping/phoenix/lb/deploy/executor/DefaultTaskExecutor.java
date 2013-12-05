package com.dianping.phoenix.lb.deploy.executor;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.bo.DeploymentBo;
import com.dianping.phoenix.lb.deploy.bo.DeploymentTaskBo;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;

public class DefaultTaskExecutor implements TaskExecutor {

    private final DeploymentTaskBo deploymentTaskBo;

    //记录任务的发布进度
    /** 正在发布的哪个站点 */
    private String currentVsName;
    /** 已经完成到第几台agent */
    private int index;
    
    public DefaultTaskExecutor(DeploymentTaskBo deploymentTaskBo) {
        this.deploymentTaskBo = deploymentTaskBo;
        
        //
        int totalCount = this.deploymentTaskBo.getDeploymentBos().size();
//        int intervalCount = this.deploymentTaskBo.getTask().getDeployPolicy().getIntervalCount(totalCount);
    }

    @Override
    public void start() {

        //遍历vs

        Map<String, DeploymentBo> deploymentBos = deploymentTaskBo.getDeploymentBos();

        for (Map.Entry<String, DeploymentBo> entry : deploymentBos.entrySet()) {
            String vsName = entry.getKey();
            DeploymentBo deployment = entry.getValue();

            
        }

    }

    @Override
    public void stop() {

    }

    @Override
    public void cancle() {

    }

    @Override
    public TaskStatus getStatus() {
        return null;
    }

}
