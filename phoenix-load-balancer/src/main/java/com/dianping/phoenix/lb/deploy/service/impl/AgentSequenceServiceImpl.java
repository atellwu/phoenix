package com.dianping.phoenix.lb.deploy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.phoenix.lb.deploy.dao.AgentIdSequenceMapper;
import com.dianping.phoenix.lb.deploy.model.AgentIdSequence;
import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;

public class AgentSequenceServiceImpl implements AgentSequenceService {

    @Autowired
    private AgentIdSequenceMapper mapper;

    /**
     * 获取agentId
     */
    public long getAgentId() {
        AgentIdSequence record = new AgentIdSequence();
        mapper.insert(record);
        return record.getAgentId();
    }

}
