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
import com.dianping.phoenix.lb.model.configure.entity.Strategy;
import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.service.StrategyService;
import com.dianping.phoenix.lb.service.VirtualServerService;
import com.dianping.phoenix.lb.utils.JsonBinder;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("virtualServerAction")
public class VirtualServerAction extends ActionSupport {

    private static final int     ERRORCODE_SUCCESS     = 0;

    private static final int     ERRORCODE_INNER_ERROR = -1;

    private static final Logger  LOG                   = LoggerFactory.getLogger(VirtualServerAction.class);

    private static final long    serialVersionUID      = -1084994778030229218L;

    private String               vsName;

    //post的参数vs，用于save
    private String               vs;

    private Map<String, Object>  dataMap               = new HashMap<String, Object>();

    @Autowired
    private VirtualServerService virtualServerService;

    @Autowired
    private StrategyService      strategyService;

    private List<Strategy>       strategies;

    @PostConstruct
    public void init() {

    }

    public String strategies() {
        strategies = strategyService.listStrategies();
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        try {
            if ("GET".equalsIgnoreCase(ServletActionContext.getRequest().getMethod())) {
                //获取vs
                VirtualServer virtualServer = virtualServerService.findVirtualServer(vsName);
                dataMap.put("virtualServer", virtualServer);
                LOG.info("execute");
            } else {
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
                }
            }
            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Bussiness Error.", e);
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error("Param Error.", e);
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
    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public String getVs() {
        return vs;
    }

    public void setVs(String vs) {
        this.vs = vs;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<Strategy> strategies) {
        this.strategies = strategies;
    }

}
