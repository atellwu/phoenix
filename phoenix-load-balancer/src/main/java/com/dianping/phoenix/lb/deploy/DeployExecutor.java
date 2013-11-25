package com.dianping.phoenix.lb.deploy;

import java.util.List;

import com.dianping.phoenix.lb.model.deploy.entity.DeployModel;

public interface DeployExecutor {
	public DeployPolicy getPolicy();

	public void submit(DeployModel model, List<String> hosts, String warType, String logUri) throws Exception;

	public void submitOld(DeployModel model);

	public boolean isDeploying(int deployId);

	public void continueDeploy(int deployId);
}
