package com.dianping.phoenix.lb.deploy.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.deploy.dao.AgentIdSequenceMapper;
import com.dianping.phoenix.lb.deploy.model.AgentIdSequence;
import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;

@Service
public class AgentSequenceServiceImpl implements AgentSequenceService {

    @Autowired
    private AgentIdSequenceMapper mapper;

    /**
     * 获取agentId
     */
    public long getAgentId() {
        AgentIdSequence record = new AgentIdSequence();
        record.setCreationDate(new Date());
        mapper.insert(record);
        return record.getAgentId();
    }

}
