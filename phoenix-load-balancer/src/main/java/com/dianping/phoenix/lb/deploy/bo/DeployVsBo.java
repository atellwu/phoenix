package com.dianping.phoenix.lb.deploy.bo;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.model.DeployVs;
import com.dianping.phoenix.lb.model.entity.VirtualServer;

public class DeployVsBo {

    private DeployVs                   deployVs;

    private Map<String, DeployAgentBo> deployAgentBos;

    private VirtualServer              vs;

    public DeployVs getDeployVs() {
        return deployVs;
    }

    public void setDeployVs(DeployVs deployVs) {
        this.deployVs = deployVs;
    }

    public Map<String, DeployAgentBo> getDeployAgentBos() {
        return deployAgentBos;
    }

    public void setDeployAgentBos(Map<String, DeployAgentBo> deployAgentBos) {
        this.deployAgentBos = deployAgentBos;
    }

    public VirtualServer getVs() {
        return vs;
    }

    public void setVs(VirtualServer vs) {
        this.vs = vs;
    }

}
