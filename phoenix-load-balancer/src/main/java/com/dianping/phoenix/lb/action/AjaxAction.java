package com.dianping.phoenix.lb.action;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.service.VirtualServerService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("ajaxAction")
public class AjaxAction extends ActionSupport {

    private static final long    serialVersionUID = -1084994778030229218L;

    private String               vsName;

    private VirtualServer        virtualServer;

    @Autowired
    private VirtualServerService virtualServerService;

    @PostConstruct
    public void init() {

    }

    @Override
    public String execute() throws Exception {
        virtualServer = virtualServerService.findVirtualServer(vsName);
        LOG.info("execute");
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
    }

    public VirtualServer getVirtualServer() {
        return virtualServer;
    }

    public void setVirtualServer(VirtualServer virtualServer) {
        this.virtualServer = virtualServer;
    }

}
