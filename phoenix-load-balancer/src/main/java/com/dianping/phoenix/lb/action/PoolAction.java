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
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.service.model.PoolService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("poolAction")
@Scope("prototype")
public class PoolAction extends MenuAction {

    private static final Logger    LOG              = LoggerFactory.getLogger(PoolAction.class);

    private static final long      serialVersionUID = -1084994778030229218L;

    private static final String    MENU             = "pool";

    private String                 poolName;

    private Boolean                showInfluencing;

    protected List<Pool>           pools;

    @Autowired
    protected PoolService          poolService;

    @Autowired
    protected VirtualServerService virtualServerService;

    public List<Pool> getPools() {
        return pools;
    }

    public String index() {
        if (pools.size() == 0) {
            return "noneVs";
        }
        poolName = pools.get(0).getName();//重定向
        return "redirect";
    }

    public String get() throws Exception {
        try {
            //获取pool
            Pool pool = poolService.findPool(poolName);

            //该pool影响的vs
            if (showInfluencing != null && showInfluencing) {
                List<String> influencingVsList = virtualServerService.findVirtualServerByPool(poolName);
                if (influencingVsList != null && influencingVsList.size() > 0) {
                    dataMap.put("influencingVsList", influencingVsList);
                }
            }

            dataMap.put("pool", pool);
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
            String poolJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(poolJson)) {
                throw new IllegalArgumentException("pool 参数不能为空！");
            }
            Pool pool = JsonBinder.getNonNullBinder().fromJson(poolJson, Pool.class);

            String poolName = pool.getName();
            Pool pool0 = poolService.findPool(poolName);
            if (pool0 != null) {
                poolService.modifyPool(poolName, pool);
            } else {
                poolService.addPool(poolName, pool);
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
            Pool pool0 = poolService.findPool(poolName);
            if (pool0 == null) {
                throw new IllegalArgumentException("不存在该站点：" + poolName);
            }
            poolService.deletePool(poolName);
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

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
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
        pools = poolService.listPools();
    }
}
