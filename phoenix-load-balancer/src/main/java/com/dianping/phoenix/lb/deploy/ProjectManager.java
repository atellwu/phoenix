package com.dianping.phoenix.lb.deploy;

import com.dianping.phoenix.lb.dal.deploy.Deployment;
import com.dianping.phoenix.lb.model.deploy.entity.DeployModel;

public interface ProjectManager {
	public Deployment findActiveDeploy(String type, String name);

	public DeployModel findModel(int deployId);

	public void storeModel(DeployModel model);
}
