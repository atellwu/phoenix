package com.dianping.phoenix.lb.action;

import java.util.List;

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

/**
 * @author wukezhu
 */
@Component("virtualServerAction")
public class VirtualServerAction extends MenuAction {

    private static final Logger  LOG              = LoggerFactory.getLogger(VirtualServerAction.class);

    private static final long    serialVersionUID = -1084994778030229218L;

    @Autowired
    private VirtualServerService virtualServerService;

    @Autowired
    private PoolService          poolService;

    private String               virtualServerName;

    public String index() {
        if (virtualServers.size() == 0) {
            return "noneVs";
        }
        virtualServerName = virtualServers.get(0).getName();//重定向
        return "redirect";
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

    public String deploy() {
        editOrShow = "edit";
        return SUCCESS;
    }

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

}
