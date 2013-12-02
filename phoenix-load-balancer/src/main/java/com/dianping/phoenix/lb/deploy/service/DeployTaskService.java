package com.dianping.phoenix.lb.deploy.service;

import java.util.List;

import com.dianping.phoenix.lb.deploy.model.DeploymentTask;

public interface DeployTaskService {

    /**
     * 获取任务列表
     * @return 
     */
    List<DeploymentTask> list(int pageNum);

    /**
     * 获取某个任务
     */
    void getTask(int taskId);

    /**
     * 创建任务<br>
     * 参数是：填写的task名称，所选择的vs名/tag名列表。
     */
    void createTask();

    /**
     * 点击开始任务后，ajax首先更新task。(更新完初始化页面的显示，开始论询显示各个DeployTask的状态)<br>
     * (修改过一次后(即创建过deployment后)，不可再修改)
     * 
     * 获取用户提交的： 每个vs下的ip列表，DeployPlan，然后更新Task。 <br>
     * 更新deployTask。创建deployment。创建DeploymentDetail。<br>
     */
    void updateTask();
}
