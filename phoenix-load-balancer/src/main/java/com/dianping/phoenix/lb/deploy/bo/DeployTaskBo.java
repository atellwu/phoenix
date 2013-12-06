package com.dianping.phoenix.lb.deploy.bo;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.model.DeployTask;

public class DeployTaskBo {

    private DeployTask              task;

    private Map<String, DeployVsBo> deployVsBos;

    public DeployTask getTask() {
        return task;
    }

    public void setTask(DeployTask task) {
        this.task = task;
    }

    public Map<String, DeployVsBo> getDeployVsBos() {
        return deployVsBos;
    }

    public void setDeployVsBos(Map<String, DeployVsBo> deploymentBos) {
        this.deployVsBos = deploymentBos;
    }

}
