package com.dianping.phoenix.lb.deploy.executor;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.bo.DeploymentBo;
import com.dianping.phoenix.lb.deploy.bo.DeploymentTaskBo;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;

public class DefaultTaskExecutor implements TaskExecutor {

    private final DeploymentTaskBo deploymentTaskBo;

    public DefaultTaskExecutor(DeploymentTaskBo deploymentTaskBo) {
        this.deploymentTaskBo = deploymentTaskBo;
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
