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

import com.dianping.phoenix.lb.deploy.StatusContainer;
import com.dianping.phoenix.lb.deploy.TaskContainer;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.NewTaskInfo;
import com.dianping.phoenix.lb.deploy.model.DeployTask;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.utils.JsonBinder;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("deployAction")
@Scope("prototype")
public class DeployAction extends ActionSupport {

    private static final long   serialVersionUID      = -7250754630706893980L;

    private static final Logger LOG                   = LoggerFactory.getLogger(DeployAction.class);

    private static final int    ERRORCODE_SUCCESS     = 0;

    private static final int    ERRORCODE_PARAM_ERROR = -2;

    private static final int    ERRORCODE_INNER_ERROR = -1;

    private Map<String, Object> dataMap               = new HashMap<String, Object>();

    @Autowired
    private DeployTaskService   deployTaskService;

    private String[]            virtualServerNames;

    private List<VirtualServer> virtualServers;

    private String              contextPath;

    private int                 pageNum               = 1;

    private List<DeployTask>    list;

    private Paginator           paginator;

    private long                deployTaskId;

    private DeployTaskBo        deployTaskBo;

//    @Autowired
    private TaskContainer       taskContainer;

//    @Autowired
    private StatusContainer     statusContainer;

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
            LOG.error("Param Error: " + e.getMessage());
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
            LOG.error("Param Error: " + e.getMessage());
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
            //获取task
            deployTaskBo = deployTaskService.getTask(deployTaskId);

            //提交任务
            taskContainer.submitTask(deployTaskBo);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * js使用长polling不断调用console()。<br>
     * <br>
     * console()通过app和pageid获取对应的JavaProject对象, 从JavaProject对象获取其正在运行的jvm的InputStream，<br>
     * 1 如果获取不到InputStream，说明没有正在运行的jvm，返回map.put("status", "done")，指示前端js停止轮询;<br>
     * 2 如果获取到InputStream，则尝试从InputStream读取数据:<br>
     * ---2.1 尝试available()+read() 10次，直到10次结束(注意，此处为了不阻塞，无法知道read()返回-1的情况) <br>
     * ---2.2 将读到的data(无论data是否有数据)，输出给前端<br>
     * 
     */
    public String getStatus(String app, String pageid) {
        try {
            DeployTaskBo deployTask = statusContainer.getDeployTask(deployTaskId);

            dataMap.put("deployTask", deployTask);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
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
