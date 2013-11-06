package com.dianping.phoenix.lb.action;

import org.apache.struts2.ServletActionContext;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("nodeAction")
public class NodeAction extends ActionSupport {

    private String path = "node";

    private String contextPath;

    public String getContextPath() {
        return contextPath;
    }

    public String getPath() {
        return path;
    }

    private static final long serialVersionUID = -1084994778030229218L;

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
