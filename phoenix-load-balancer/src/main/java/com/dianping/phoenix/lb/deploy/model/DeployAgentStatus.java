package com.dianping.phoenix.lb.deploy.model;

import java.util.HashSet;
import java.util.Set;

public enum DeployAgentStatus {
    CREATED, INIT, REJECTED, PROCESSING, FAILED, SUCCESS, KILLED;

    private final static Set<DeployAgentStatus> COMPLETED_STATUS_SET = new HashSet<DeployAgentStatus>();
    static {
        COMPLETED_STATUS_SET.add(REJECTED);
        COMPLETED_STATUS_SET.add(FAILED);
        COMPLETED_STATUS_SET.add(SUCCESS);
        COMPLETED_STATUS_SET.add(KILLED);
    };

    private final static Set<DeployAgentStatus> ERROR_STATUS_SET     = new HashSet<DeployAgentStatus>();
    static {
        ERROR_STATUS_SET.add(REJECTED);
        ERROR_STATUS_SET.add(FAILED);
        ERROR_STATUS_SET.add(KILLED);
    };

    /**
     * 是否是终结状态
     * 
     * @return
     */
    public boolean isCompleted() {
        return COMPLETED_STATUS_SET.contains(this);
    }

    public boolean isNotSuccess() {
        return ERROR_STATUS_SET.contains(this);
    }

}
