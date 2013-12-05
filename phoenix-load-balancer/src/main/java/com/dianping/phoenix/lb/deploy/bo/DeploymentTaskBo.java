package com.dianping.phoenix.lb.deploy.bo;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.model.DeploymentTask;

public class DeploymentTaskBo {

    private DeploymentTask            task;

    private Map<String, DeploymentBo> deploymentBos;

    public DeploymentTask getTask() {
        return task;
    }

    public void setTask(DeploymentTask task) {
        this.task = task;
    }

    public Map<String, DeploymentBo> getDeploymentBos() {
        return deploymentBos;
    }

    public void setDeploymentBos(Map<String, DeploymentBo> deploymentBos) {
        this.deploymentBos = deploymentBos;
    }

}
