package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.deploy.DeploySetting;
import com.dianping.phoenix.lb.deploy.model.DeploymentTask;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.PoolService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("deployAction")
@Scope("prototype")
public class DeployAction extends ActionSupport {

    private static final long    serialVersionUID      = -7250754630706893980L;

    private static final Logger  LOG                   = LoggerFactory.getLogger(DeployAction.class);

    private static final int     ERRORCODE_SUCCESS     = 0;

    private static final int     ERRORCODE_PARAM_ERROR = -2;

    private static final int     ERRORCODE_INNER_ERROR = -1;

    private Map<String, Object>  dataMap               = new HashMap<String, Object>();

    @Autowired
    private VirtualServerService virtualServerService;

    @Autowired
    private PoolService          poolService;

    @Autowired
    private DeployTaskService    deployTaskService;

    private String[]             virtualServerNames;

    private List<VirtualServer>  virtualServers;

    private String               contextPath;

    private DeploySetting        deployPlan;

    private int                  pageNum               = 1;

    private int                  MAX_PAGE_NUM          = 50;

    private List<DeploymentTask> list;

    @PostConstruct
    public void init() {
    }

    /**
     * 进入发布的页面，需要的参数是vsName列表
     */
    public String list() {
        if (pageNum > MAX_PAGE_NUM) {
            pageNum = 1;
        }
        list = deployTaskService.list(pageNum);

        return SUCCESS;
    }

    /**
     * 进入发布的页面，需要的参数是vsName列表
     */
    public String task() {
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

    public List<DeploymentTask> getList() {
        return list;
    }

    public void setList(List<DeploymentTask> list) {
        this.list = list;
    }

}
