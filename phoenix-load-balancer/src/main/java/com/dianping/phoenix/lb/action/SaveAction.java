package com.dianping.phoenix.lb.action;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.service.VirtualServerService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("saveAction")
public class SaveAction extends ActionSupport {

    private static final long    serialVersionUID = -1084994778030229218L;

    @Autowired
    private VirtualServerService virtualServerService;

    @Override
    public String execute() throws Exception {
        LOG.info("execute");

        String requestBody = IOUtils.toString(ServletActionContext.getRequest().getInputStream());

        System.out.println(requestBody);

        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
    }

}
