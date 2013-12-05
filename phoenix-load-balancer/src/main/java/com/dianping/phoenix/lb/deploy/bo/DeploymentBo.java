package com.dianping.phoenix.lb.deploy.bo;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.model.Deployment;
import com.dianping.phoenix.lb.deploy.model.DeploymentDetail;
import com.dianping.phoenix.lb.model.entity.VirtualServer;

public class DeploymentBo {

    private Deployment                    deployment;

    private Map<String, DeploymentDetail> deploymentDetails;

    private VirtualServer                 vs;

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public Map<String, DeploymentDetail> getDeploymentDetails() {
        return deploymentDetails;
    }

    public void setDeploymentDetails(Map<String, DeploymentDetail> deploymentDetails) {
        this.deploymentDetails = deploymentDetails;
    }

    public VirtualServer getVs() {
        return vs;
    }

    public void setVs(VirtualServer vs) {
        this.vs = vs;
    }

}
