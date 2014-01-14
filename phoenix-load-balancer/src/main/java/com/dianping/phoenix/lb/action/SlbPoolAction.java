package com.dianping.phoenix.lb.action;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.SlbPool;
import com.dianping.phoenix.lb.service.model.SlbPoolService;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("slbPoolAction")
@Scope("prototype")
public class SlbPoolAction extends MenuAction {

    private static final Logger LOG              = LoggerFactory.getLogger(SlbPoolAction.class);

    private static final long   serialVersionUID = -1084994778030229218L;

    private static final String MENU             = "slbPool";

    private String              slbPoolName;

    private Boolean             showInfluencing;

    protected List<SlbPool>     slbPools;

    @Autowired
    private SlbPoolService      slbPoolService;

    public List<SlbPool> getSlbPools() {
        return slbPools;
    }

    public String listSlbPools() {
        return SUCCESS;
    }

    public String index() {
        if (slbPools.size() == 0) {
            return "noneVs";
        }
        slbPoolName = slbPools.get(0).getName();//重定向
        return "redirect";
    }

    public String get() throws Exception {
        try {
            //获取slbPool
            SlbPool slbPool = slbPoolService.findSlbPool(slbPoolName);

            dataMap.put("slbPool", slbPool);
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
            String slbPoolJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(slbPoolJson)) {
                throw new IllegalArgumentException("slbPool 参数不能为空！");
            }
            SlbPool slbPool = JsonBinder.getNonNullBinder().fromJson(slbPoolJson, SlbPool.class);

            String slbPoolName = slbPool.getName();
            SlbPool slbPool0 = slbPoolService.findSlbPool(slbPoolName);
            if (slbPool0 != null) {
                slbPoolService.modifySlbPool(slbPoolName, slbPool);
            } else {
                slbPoolService.addSlbPool(slbPoolName, slbPool);
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
            SlbPool slbPool0 = slbPoolService.findSlbPool(slbPoolName);
            if (slbPool0 == null) {
                throw new IllegalArgumentException("不存在该站点：" + slbPoolName);
            }
            slbPoolService.deleteSlbPool(slbPoolName);
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

    public String getSlbPoolName() {
        return slbPoolName;
    }

    public void setSlbPoolName(String slbPoolName) {
        this.slbPoolName = slbPoolName;
    }

    public Boolean getShowInfluencing() {
        return showInfluencing;
    }

    public void setShowInfluencing(Boolean showInfluencing) {
        this.showInfluencing = showInfluencing;
    }

    @Override
    public void validate() {
        super.validate();
        this.setMenu(MENU);
        slbPools = slbPoolService.listSlbPools();
    }
}
