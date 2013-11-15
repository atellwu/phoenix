package com.dianping.phoenix.lb.action;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.service.VirtualServerService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("mainAction")
public class MainAction extends ActionSupport {

    private static final long    serialVersionUID = -1084994778030229218L;

    private String               virtualServerName;

    private String               vsName;

    private String               contextPath;

    private List<VirtualServer>  virtualServers   = new ArrayList<VirtualServer>();

    @Autowired
    private VirtualServerService virtualServerService;

    @PostConstruct
    public void init() {
        virtualServers = virtualServerService.listVirtualServers();
    }

    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String execute() throws Exception {
        LOG.info("execute");
        return SUCCESS;
    }

    @Override
    public void validate() {
        contextPath = ServletActionContext.getServletContext().getContextPath();

        if (vsName == null) {
            vsName = virtualServers.get(0).getName();
        }

        super.validate();
    }

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public void setVirtualServers(List<VirtualServer> virtualServers) {
        this.virtualServers = virtualServers;
    }

}
