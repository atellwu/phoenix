package com.dianping.phoenix.lb.deploy;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;

/**
 * 
 * 查询状态
 * 
 * @author kezhu.wu
 * 
 */
public interface TaskContainer {

    void submitTask(DeployTaskBo task);

}
