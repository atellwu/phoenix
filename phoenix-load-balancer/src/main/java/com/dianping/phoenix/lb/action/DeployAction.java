package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo;
import com.dianping.phoenix.lb.deploy.executor.TaskExecutor;
import com.dianping.phoenix.lb.deploy.executor.TaskExecutorContainer;
import com.dianping.phoenix.lb.deploy.model.DeployTask;
import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("deployAction")
@Scope("prototype")
public class DeployAction extends MenuAction {

    private static final long     serialVersionUID      = -7250754630706893980L;

    private static final Logger   LOG                   = LoggerFactory.getLogger(DeployAction.class);

    private static final int      ERRORCODE_SUCCESS     = 0;

    private static final int      ERRORCODE_PARAM_ERROR = -2;

    private static final int      ERRORCODE_INNER_ERROR = -1;

    private static final String   MENU                  = "deploy";

    private Map<String, Object>   dataMap               = new HashMap<String, Object>();

    @Autowired
    private DeployTaskService     deployTaskService;

    private String[]              virtualServerNames;

    private List<VirtualServer>   virtualServers;

    private String                contextPath;

    private int                   pageNum               = 1;

    private List<DeployTask>      list;

    private Paginator             paginator;

    private long                  deployTaskId;

    private DeployTaskBo          deployTaskBo;

    //    /** 该pool影响到的vs自动弹出创建 */
    //    private String                autoShowByPool;

    @Autowired
    private TaskExecutorContainer taskContainer;

    @PostConstruct
    public void init() {
    }

    /**
     * 进入发布的页面，需要的参数是vsName列表
     */
    public String list() {
        // 获取用户的历史重发记录
        paginator = new Paginator();
        list = deployTaskService.list(paginator, pageNum);

        return SUCCESS;
    }

    /**
     * Task页面
     */
    public String showDeployTask() {
        return SUCCESS;
    }

    /**
     * task对象（包含所有静态信息）
     */
    public String getDeployTask() {
        try {
            //获取task
            deployTaskBo = deployTaskService.getTask(deployTaskId);

            dataMap.put("task", deployTaskBo);
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * task对象（包含所有静态信息）
     */
    public String addDeployTask() {
        try {
            //获取task
            String taskJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(taskJson)) {
                throw new IllegalArgumentException("vs 参数不能为空！");
            }
            NewTaskInfo newTaskInfo = JsonBinder.getNonNullBinder().fromJson(taskJson, NewTaskInfo.class);

            deployTaskService.addTask(newTaskInfo);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * 选择了host列表，发布策略，进行保存。
     */
    public String updateDeployTask() {
        try {
            //获取task
            deployTaskBo = deployTaskService.getTask(deployTaskId);
            //如果状态不是CREATED，则不让修改
            DeployTaskStatus status = deployTaskBo.getTask().getStatus();
            if (status != DeployTaskStatus.CREATED) {
                throw new IllegalArgumentException("任务已经启动过，不可再修改。");
            }

            String taskJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(taskJson)) {
                throw new IllegalArgumentException("vs 参数不能为空！");
            }
            deployTaskBo = JsonBinder.getNonNullBinder().fromJson(taskJson, DeployTaskBo.class);

            deployTaskService.updateTask(deployTaskBo);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * 启动Task
     */
    public String startDeployTask() {
        try {
            //提交任务
            TaskExecutor taskExecutor = taskContainer.submitTaskExecutor(deployTaskId);

            taskExecutor.start();

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    //    /**
    //     * 暂停Task
    //     */
    //    public String pauseDeployTask() {
    //        try {
    //            TaskExecutor taskExecutor = taskContainer.getTaskExecutor(deployTaskId);
    //            if (taskExecutor != null) {
    //                taskExecutor.pause();
    //            }
    //
    //            dataMap.put("errorCode", ERRORCODE_SUCCESS);
    //        } catch (IllegalArgumentException e) {
    //            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
    //            dataMap.put("errorMessage", e.getMessage());
    //        } catch (Exception e) {
    //            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
    //            dataMap.put("errorMessage", e.getMessage());
    //            LOG.error(e.getMessage(), e);
    //        }
    //        return SUCCESS;
    //    }

    public String stopDeployTask() {
        try {
            TaskExecutor taskExecutor = taskContainer.getTaskExecutor(deployTaskId);
            if (taskExecutor != null) {
                taskExecutor.stop();
            }

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    //    /**
    //     * 重试Task
    //     */
    //    public String retryDeployTask() {
    //        try {
    //            TaskExecutor taskExecutor = taskContainer.getTaskExecutor(deployTaskId);
    //            if (taskExecutor != null) {
    //                //                taskExecutor.retry();
    //            }
    //
    //            dataMap.put("errorCode", ERRORCODE_SUCCESS);
    //        } catch (IllegalArgumentException e) {
    //            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
    //            dataMap.put("errorMessage", e.getMessage());
    //        } catch (Exception e) {
    //            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
    //            dataMap.put("errorMessage", e.getMessage());
    //            LOG.error(e.getMessage(), e);
    //        }
    //        return SUCCESS;
    //    }

    public String getStatus() {
        try {
            TaskExecutor taskExecutor = taskContainer.getTaskExecutor(deployTaskId);
            if (taskExecutor != null) {
                //从内存拿
                deployTaskBo = taskExecutor.getDeployTaskBo();
            } else {
                //从数据库读取
                deployTaskBo = deployTaskService.getTask(deployTaskId);
            }

            dataMap.put("task", deployTaskBo);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    public String deploy() {
        return SUCCESS;
    }

    public String getLog() {
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        if (contextPath == null) {
            contextPath = ServletActionContext.getServletContext().getContextPath();
        }
        this.setMenu(MENU);
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String[] getVirtualServerNames() {
        return virtualServerNames;
    }

    public void setVirtualServerNames(String[] virtualServerNames) {
        this.virtualServerNames = virtualServerNames;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public List<DeployTask> getList() {
        return list;
    }

    public void setList(List<DeployTask> list) {
        this.list = list;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    public long getDeployTaskId() {
        return deployTaskId;
    }

    public void setDeployTaskId(long deployTaskId) {
        this.deployTaskId = deployTaskId;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

}
