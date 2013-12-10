package com.dianping.phoenix.lb.deploy.agent;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.phoenix.lb.service.model.VirtualServerService;

public class DefaultAgentClient implements AgentClient {

    private long                  deployId;
    private String               vsName;
    private String               tag;
    private VirtualServerService virtualServerService;

    public DefaultAgentClient(long deployId, String vsName, String tag, VirtualServerService virtualServerService) {
        super();
        this.deployId = deployId;
        this.vsName = vsName;
        this.tag = tag;
        this.virtualServerService = virtualServerService;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
