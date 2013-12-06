package com.dianping.phoenix.lb.deploy.executor;

import java.util.Map;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;

public class DefaultTaskExecutor implements TaskExecutor {

    private final DeployTaskBo deploymentTaskBo;

    //记录任务的发布进度
    /** 正在发布的哪个站点 */
    private String             currentVsName;
    /** 已经完成到第几台agent */
    private int                index;

    public DefaultTaskExecutor(DeployTaskBo deploymentTaskBo) {
        this.deploymentTaskBo = deploymentTaskBo;

        //
        int totalCount = this.deploymentTaskBo.getDeployVsBos().size();
        //        int intervalCount = this.deploymentTaskBo.getTask().getDeployPolicy().getIntervalCount(totalCount);
    }

    @Override
    public void start() {

        //遍历vs

        Map<String, DeployVsBo> deploymentBos = deploymentTaskBo.getDeployVsBos();

        for (Map.Entry<String, DeployVsBo> entry : deploymentBos.entrySet()) {
            String vsName = entry.getKey();
            DeployVsBo deployment = entry.getValue();

        }

    }

    /**
     * 获取上次发布的进度，初始化时需要使用。
     */

    @Override
    public void stop() {

    }

    @Override
    public void cancle() {

    }

    @Override
    public TaskStatus getStatus() {
        return null;
    }

}
