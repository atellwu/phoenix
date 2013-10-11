package com.dianping.phoenix.softbalance.action;

import org.apache.struts2.ServletActionContext;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.softbalance.model.VirtualServer;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("virtualAction")
public class VirtualServerAction extends ActionSupport {

    private static final long serialVersionUID = -1084994778030229218L;

    private String            path             = "virtual";

    private String            contextPath;

    private VirtualServer     virtualServer;

    public String getContextPath() {
        return contextPath;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String execute() throws Exception {
        LOG.info("execute");
        return SUCCESS;
    }

    @Override
    public void validate() {
        contextPath = ServletActionContext.getServletContext().getContextPath();
        LOG.info("validate");
        super.validate();
    }

}
