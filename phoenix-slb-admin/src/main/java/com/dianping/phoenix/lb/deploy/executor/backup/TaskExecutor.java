///**
// * Project: phoenix-load-balancer
// * 
// * File Created at Nov 20, 2013
// * 
// */
//package com.dianping.phoenix.lb.deploy.executor.backup;
//
//import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
//
///**
// * 
// * 负责一个DeployTask的执行。<br>
// * 
// * 作为执行者，只负责执行，反馈状态。（状态的存储可以通过Listener回调）
// * 
// * @author atell
// * 
// */
//public interface TaskExecutor {
//
//    /**
//     * 开始继续运行
//     */
//    /**
//     * 开始任务（开始之后的任务不能再修改）<br>
//     * 
//     * (1) 通过回调：将数据库中，任务状态设置为进行中。 <br>
//     * (2) for(deployment in task.deployments) <br>
//     * for(detail in deployment.deploymentDetail) （忽略已经完成的detail）<br>
//     * 根据task.deployPlan，构建发布的策略，依照策略进行发布（一个ip对应一个agentTask）。 <br>
//     * AgentExecutor发布：向ip的agent发起请求，然后论询agent，获取结果存储到detail表。
//     * 
//     */
//    void start();
//
//    //    void pause();
//
//    /**
//     * 停止，停止后可以再次启动
//     */
//    void pause();
//
//    /**
//     * 关闭，关闭后不可再启动
//     */
//    void stop();
//
//    //    /**
//    //     * 暂停运行（最小粒度是agent，暂停时，记录进度）
//    //     */
//    //    void pause();
//
//    //    /**
//    //     * 终止/取消运行
//    //     */
//    //    void cancel();
//    //
//    //    void resume();
//
//    DeployTaskBo getDeployTaskBo();
//
//    //    void retry();
//}
