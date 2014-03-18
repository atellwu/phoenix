package com.dianping.phoenix.lb.facade.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.bo.DeployAgentBo;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo.VsAndTag;
import com.dianping.phoenix.lb.deploy.executor.TaskExecutor;
import com.dianping.phoenix.lb.deploy.executor.TaskExecutorContainer;
import com.dianping.phoenix.lb.deploy.model.AgentBatch;
import com.dianping.phoenix.lb.deploy.model.DeployAgent;
import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.facade.PoolFacade;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.model.entity.Instance;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbPool;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.CommonAspectService;
import com.dianping.phoenix.lb.service.model.PoolService;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

@Service
public class PoolFacadeImpl implements PoolFacade {

	private static final String PATTERN = " yyyyMMdd HH:mm";

	private static final String TASK_NAME_PREFIX = "ApiCall:";

	@Autowired
	private PoolService poolService;

	@Autowired
	private DeployTaskService deployTaskService;

	@Autowired
	protected VirtualServerService virtualServerService;

	@Autowired
	protected CommonAspectService commonAspectService;

	@Autowired
	protected AgentSequenceService agentSequenceService;

	@Autowired
	protected StrategyService strategyService;

	protected ConfigManager configManager;

	@Autowired
	private TaskExecutorContainer taskContainer;

	@PostConstruct
	public void init() throws ComponentLookupException {
		configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
	}

	@Override
	public void addMember(String poolName, List<Member> members) throws BizException {
		if (members == null || members.size() <= 0) {
			return;
		}

		// 修改pool
		Pool pool = poolService.findPool(poolName);
		if (pool == null) {
			throw new IllegalArgumentException("pool not found: '" + poolName + "'");
		}

		for (Member member : members) {
			pool.addMember(member);
		}
		poolService.modifyPool(poolName, pool);

	}

	@Override
	public void delMember(String poolName, List<String> memberNames) throws BizException {
		if (memberNames == null || memberNames.size() <= 0) {
			return;
		}

		// 修改pool
		Pool pool = poolService.findPool(poolName);
		if (pool == null) {
			throw new IllegalArgumentException("pool not found: '" + poolName + "'");
		}

		List<Member> members = pool.getMembers();

		Iterator<Member> iterator = members.iterator();
		while (iterator.hasNext()) {
			Member member = iterator.next();
			if (memberNames.contains(member.getName())) {
				iterator.remove();
			}
		}

		poolService.modifyPool(poolName, pool);
	}

	@Override
	public TaskExecutor deploy(String poolName) throws BizException {

		// 找出pool所影响的vs列表
		List<Pool> pools = poolService.listPools();
		List<String> influencingVsList = virtualServerService.findVirtualServerByPool(poolName);
		if (influencingVsList == null || influencingVsList.size() <= 0) {
			return null;
		}

		// 为vs列表打tag
		List<Aspect> commonAspects = commonAspectService.listCommonAspects();
		List<VsAndTag> selectedVsAndTags = new ArrayList<VsAndTag>();
		if (influencingVsList != null) {
			for (String vsName : influencingVsList) {
				VirtualServer virtualServer = virtualServerService.findVirtualServer(vsName);
				Validate.notNull(virtualServer, "vs(" + vsName + ") not found.");
				String tag = virtualServerService.tag(vsName, virtualServer.getVersion(), pools, commonAspects);

				VsAndTag vsAndTag = new VsAndTag();
				vsAndTag.setVsName(vsName);
				vsAndTag.setTag(tag);
				selectedVsAndTags.add(vsAndTag);
			}
		}

		// 根据系列vs，tag和任务名，创建一个任务
		NewTaskInfo newTaskInfo = new NewTaskInfo();
		newTaskInfo
		      .setTaskName(TASK_NAME_PREFIX + poolName + DateFormatUtils.format(System.currentTimeMillis(), PATTERN));
		newTaskInfo.setSelectedVsAndTags(selectedVsAndTags);
		long deployTaskId = deployTaskService.addTask(newTaskInfo);

		DeployTaskBo deployTaskBo = deployTaskService.getTask(deployTaskId);
		Map<String, DeployVsBo> deployVsBos = deployTaskBo.getDeployVsBos();

		// 选择页面的所有agent的ip
		for (Map.Entry<String, DeployVsBo> entry : deployVsBos.entrySet()) {
			DeployVsBo deployVsBo = entry.getValue();

			SlbPool slbPool = deployVsBo.getSlbPool();

			Map<String, DeployAgentBo> deployAgentBos = new HashMap<String, DeployAgentBo>();

			for (Instance instance : slbPool.getInstances()) {
				String ip = instance.getIp();
				DeployAgent deployAgent = new DeployAgent();
				deployAgent.setIpAddress(ip);

				DeployAgentBo deployAgentBo = new DeployAgentBo(deployAgent);

				deployAgentBos.put(ip, deployAgentBo);
			}

			deployVsBo.setDeployAgentBos(deployAgentBos);
		}

		// 选择时间间隔等设置
		deployTaskBo.getTask().setAgentBatch(AgentBatch.TWO_BY_TWO);
		deployTaskBo.getTask().setAutoContinue(true);
		deployTaskBo.getTask().setDeployInterval(-1);

		// 更新task
		deployTaskService.updateTask(deployTaskBo);

		// 提交任务
		TaskExecutor taskExecutor = taskContainer.submitTaskExecutor(deployTaskId);

		taskExecutor.start();

		return taskExecutor;
	}

}
