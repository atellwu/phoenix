package com.dianping.phoenix.lb.deploy;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;

/**
 * 
 * 查询状态
 * 
 * @author kezhu.wu
 * 
 */
public interface StatusContainer {

    DeployTaskBo getDeployTask(long taskId);

}
