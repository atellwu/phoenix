package com.dianping.phoenix.lb.action;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("mainAction")
public class MainAction extends ActionSupport {

    private static final long   serialVersionUID = -1084994778030229218L;

    private String              virtualServerName;

    private String              tabName;

    private String              contextPath;

    private VirtualServer       virtualServer;

    private List<VirtualServer> virtualServers   = new ArrayList<VirtualServer>();

    @PostConstruct
    public void init() {
        //TODO mock virtualServers
        VirtualServer vs1 = new VirtualServer();
        vs1.setName("dpcomm");
        vs1.setDomain("www.dianping.com");
        VirtualServer vs2 = new VirtualServer();
        vs2.setName("tuangou");
        vs2.setDomain("t.dianping.com");

        virtualServers.add(vs1);
        virtualServers.add(vs2);

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

        this.virtualServer = virtualServers.get(0);

        for (VirtualServer virtualServer : virtualServers) {
            if (StringUtils.equalsIgnoreCase(virtualServer.getName(), virtualServerName)) {
                this.virtualServer = virtualServer;
                break;
            }
        }

        if (virtualServerName == null) {
            virtualServerName = virtualServer.getName();
        }

        super.validate();
    }

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public VirtualServer getVirtualServer() {
        return virtualServer;
    }

    public void setVirtualServer(VirtualServer virtualServer) {
        this.virtualServer = virtualServer;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public void setVirtualServers(List<VirtualServer> virtualServers) {
        this.virtualServers = virtualServers;
    }

}
