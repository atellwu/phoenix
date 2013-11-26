package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.PoolService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
public abstract class MenuAction extends ActionSupport {

    public static final int        ERRORCODE_SUCCESS     = 0;

    public static final int        ERRORCODE_PARAM_ERROR = -2;

    public static final int        ERRORCODE_INNER_ERROR = -1;

    private static final long      serialVersionUID      = -1084994778030229218L;

    protected Map<String, Object>  dataMap               = new HashMap<String, Object>();

    protected String               contextPath;

    protected String               editOrShow            = "show";

    @Autowired
    protected VirtualServerService virtualServerService;

    @Autowired
    protected PoolService          poolService;

    protected List<Pool>           pools;

    protected List<VirtualServer>  virtualServers;

    @PostConstruct
    public void init() {
        virtualServers = virtualServerService.listVirtualServers();
        pools = poolService.listPools();
    }

    public String listVirtualServers() {
        return SUCCESS;
    }

    public String listPools() {
        return SUCCESS;
    }

    public String show() {
        editOrShow = "show";
        return SUCCESS;
    }

    public String edit() {
        editOrShow = "edit";
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

    public List<Pool> getPools() {
        return pools;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getEditOrShow() {
        return editOrShow;
    }

}
