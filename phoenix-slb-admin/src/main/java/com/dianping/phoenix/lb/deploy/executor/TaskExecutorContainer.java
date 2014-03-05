package com.dianping.phoenix.lb.deploy.executor;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.exception.BizException;

public interface TaskExecutorContainer {

    /**
     * 获取一个已存在的TaskExecutor
     */
    TaskExecutor getTaskExecutor(long taskId);

    /**
     * 创建一个TaskExecutor，并且返回
     * @throws BizException 
     */
    TaskExecutor submitTaskExecutor(long taskId) throws BizException;

    /**
     * 创建一个TaskExecutor，并且返回
     */
    TaskExecutor submitTaskExecutor(DeployTaskBo deployTaskBo);
}
