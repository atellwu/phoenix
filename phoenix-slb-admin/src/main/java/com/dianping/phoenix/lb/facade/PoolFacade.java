package com.dianping.phoenix.lb.facade;

import java.util.List;

import com.dianping.phoenix.lb.deploy.executor.TaskExecutor;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Member;

public interface PoolFacade {

	void addMember(String poolName, List<Member> members) throws BizException;

	void delMember(String poolName, List<String> memberNames) throws BizException;

	TaskExecutor deploy(String poolName) throws BizException;

}
