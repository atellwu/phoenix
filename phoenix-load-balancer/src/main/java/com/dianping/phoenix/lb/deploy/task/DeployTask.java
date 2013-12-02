package com.dianping.phoenix.lb.deploy.task;

import com.dianping.phoenix.lb.deploy.model.Deployment;

/**
 * 
 * 负责一个DeployTask的执行。<br>
 * 
 * 作为执行者，只负责执行，反馈状态。（状态的存储可以通过Listener回调）
 * 
 * @author atell
 * 
 */
public class DeployTask {

    private Deployment task;

    /**
     * 开始任务（开始之后的任务不能再修改）<br>
     * 
     * (1) 通过回调：将数据库中，任务状态设置为进行中。 <br>
     * (2) for(deployment in task.deployments) <br>
     * for(detail in deployment.deploymentDetail) （忽略已经完成的detail）<br>
     * 根据task.deployPlan，构建发布的策略，依照策略进行发布（一个ip对应一个agentTask）。 <br>
     * AgentExecutor发布：向ip的agent发起请求，然后论询agent，获取结果存储到detail表。
     * 
     */
    void startTask() {

    }

    /**
     * 暂停任务。<br>
     * 在某个AgentTask结束后，暂停。
     */
    void stopTask() {

    }

    //显示任务状态和进度的，再独立做只读的查询。

}
