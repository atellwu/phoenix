package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.model.PoolService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;
import com.dianping.phoenix.lb.utils.JsonBinder;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("virtualServerAction")
public class VirtualServerAction extends ActionSupport {

    private static final int     ERRORCODE_SUCCESS     = 0;

    private static final int     ERRORCODE_PARAM_ERROR = -2;

    private static final int     ERRORCODE_INNER_ERROR = -1;

    private static final Logger  LOG                   = LoggerFactory.getLogger(VirtualServerAction.class);

    private static final long    serialVersionUID      = -1084994778030229218L;

    //post的参数vs，用于save
    private String               vs;

    private Map<String, Object>  dataMap               = new HashMap<String, Object>();

    @Autowired
    private VirtualServerService virtualServerService;

    @Autowired
    private PoolService          poolService;

    private List<VirtualServer>  virtualServers;

    private String               virtualServerName;

    private String               contextPath;

    private String               editOrShow            = "show";

    @PostConstruct
    public void init() {
        virtualServers = virtualServerService.listVirtualServers();
    }

    public String index() {
        if (virtualServers.size() == 0) {
            return "noneVs";
        }
        virtualServerName = virtualServers.get(0).getName();//重定向
        return "redirect";
    }

    public String show() {
        editOrShow = "show";
        return SUCCESS;
    }

    public String edit() {
        editOrShow = "edit";
        return SUCCESS;
    }

    public String deploy() {
        editOrShow = "edit";
        return SUCCESS;
    }

    public String get() throws Exception {
        try {
            //获取vs
            VirtualServer virtualServer = virtualServerService.findVirtualServer(virtualServerName);
            dataMap.put("virtualServer", virtualServer);
            LOG.info("execute");
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Bussiness Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        try {
            String vsJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(vsJson)) {
                throw new IllegalArgumentException("vs 参数不能为空！");
            }
            VirtualServer virtualServer = JsonBinder.getNonNullBinder().fromJson(vsJson, VirtualServer.class);

            String virtualServerName = virtualServer.getName();
            VirtualServer virtualServer0 = virtualServerService.findVirtualServer(virtualServerName);
            if (virtualServer0 != null) {
                virtualServerService.modifyVirtualServer(virtualServerName, virtualServer);
            } else {
                virtualServerService.addVirtualServer(virtualServerName, virtualServer);
                virtualServers = virtualServerService.listVirtualServers();//重新更新list
            }
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Bussiness Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    public String remove() throws Exception {
        try {
            VirtualServer virtualServer0 = virtualServerService.findVirtualServer(virtualServerName);
            if (virtualServer0 == null) {
                throw new IllegalArgumentException("不存在该站点：" + virtualServerName);
            }
            virtualServerService.deleteVirtualServer(virtualServerName);
            virtualServers = virtualServerService.listVirtualServers();//重新更新list
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Bussiness Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    public String getVirtualServerList() {
        virtualServers = virtualServerService.listVirtualServers();
        return SUCCESS;
    }

    public String preview() throws Exception {
        try {
            String vsJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(vsJson)) {
                throw new IllegalArgumentException("vs 参数不能为空！");
            }
            VirtualServer virtualServer = JsonBinder.getNonNullBinder().fromJson(vsJson, VirtualServer.class);

            List<Pool> poolList = poolService.listPools();

            String nginxConfig = virtualServerService.generateNginxConfig(virtualServer, poolList);

            dataMap.put("nginxConfig", nginxConfig);
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Bussiness Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
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

    public String getVs() {
        return vs;
    }

    public void setVs(String vs) {
        this.vs = vs;
    }

    public List<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

    public String getEditOrShow() {
        return editOrShow;
    }

}
