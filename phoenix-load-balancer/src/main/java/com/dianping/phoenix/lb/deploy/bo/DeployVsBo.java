package com.dianping.phoenix.lb.deploy.bo;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.model.DeployAgent;
import com.dianping.phoenix.lb.deploy.model.DeployVs;
import com.dianping.phoenix.lb.model.entity.VirtualServer;

public class DeployVsBo {

    private DeployVs                 deployVs;

    private Map<String, DeployAgent> deployAgents;

    private VirtualServer            vs;

    public DeployVs getDeployVs() {
        return deployVs;
    }

    public void setDeployVs(DeployVs deployVs) {
        this.deployVs = deployVs;
    }

    public Map<String, DeployAgent> getDeployAgents() {
        return deployAgents;
    }

    public void setDeployAgents(Map<String, DeployAgent> deployAgents) {
        this.deployAgents = deployAgents;
    }

    public VirtualServer getVs() {
        return vs;
    }

    public void setVs(VirtualServer vs) {
        this.vs = vs;
    }

}
