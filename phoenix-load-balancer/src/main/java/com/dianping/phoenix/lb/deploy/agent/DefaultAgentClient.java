package com.dianping.phoenix.lb.deploy.agent;

import com.dianping.phoenix.lb.deploy.model.AgentStatus;


//需要使用service，怎么办
public class DefaultAgentClient implements AgentCleint {

    private int    deployId;
    private String vsName;
    private String tag;

    private DefaultAgentClient(int deployId, String vsName, String tag) {
        super();
        this.deployId = deployId;
        this.vsName = vsName;
        this.tag = tag;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

    @Override
    public AgentStatus getAgentStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRawLog() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getDeployId() {
        return deployId;
    }

    public void setDeployId(int deployId) {
        this.deployId = deployId;
    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return String.format("DefaultAgentClient [deployId=%s, vsName=%s, tag=%s]", deployId, vsName, tag);
    }

}
