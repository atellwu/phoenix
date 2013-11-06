package com.dianping.phoenix.lb.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.velocity.TemplateManager;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("ajaxAction")
public class AjaxAction extends ActionSupport {

    private static final String  VM_SUFFIX        = ".vm";

    private static final long    serialVersionUID = -1084994778030229218L;

    private static final Pattern PATTERN          = Pattern.compile(".*/template/properties/access_log\\.vm");

    private String               virtualServerName;

    private String               tabName;

    private String               contextPath;

    private VirtualServer        virtualServer;

    private List<VirtualServer>  virtualServers   = new ArrayList<VirtualServer>();

    private Set<String>          definedNameList;

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

        System.out.println(ServletActionContext.getRequest().getRequestURI());

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
        definedNameList = TemplateManager.INSTANCE.availableFiles("properties");
        //        if (names != null && names.size() > 0) {
        //            definedNameList = new ArrayList<String>();
        //            for (String name : names) {
        //                String name0 = name.endsWith(VM_SUFFIX) ? name.substring(0, name.length() - 3) : name;
        //                definedNameList.add(name0);
        //            }
        //        }
        //        System.out.println(definedNameList);

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

    public Set<String> getDefinedNameList() {
        return definedNameList;
    }

    public void setDefinedNameList(Set<String> definedNameList) {
        this.definedNameList = definedNameList;
    }

}
