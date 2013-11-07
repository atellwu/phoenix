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
@Component("vsNameListAction")
public class VsNameListAction extends ActionSupport {

    private static final long    serialVersionUID = 2150069350934991522L;

    private String               contextPath;

    private List<VirtualServer>  virtualServers   = new ArrayList<VirtualServer>();

    @Autowired
    private VirtualServerService virtualServerService;

    @PostConstruct
    public void init() {
        // mock virtualServers
        //        VirtualServer vs1 = new VirtualServer();
        //        vs1.setName("dpcomm");
        //        vs1.setDomain("www.dianping.com");
        //        VirtualServer vs2 = new VirtualServer();
        //        vs2.setName("tuangou");
        //        vs2.setDomain("t.dianping.com");
        //
        //        virtualServers.add(vs1);
        //        virtualServers.add(vs2);

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

        super.validate();
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

}
