package com.dianping.phoenix.lb.deploy.executor;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;

public interface TaskExecutorContainer {

    /**
     * 获取一个已存在的TaskExecutor
     */
    TaskExecutor getTaskExecutor(long taskId);

    /**
     * 创建一个TaskExecutor，并且返回
     */
    TaskExecutor submitTaskExecutor(long taskId);

    /**
     * 创建一个TaskExecutor，并且返回
     */
    TaskExecutor submitTaskExecutor(DeployTaskBo deployTaskBo);
}
