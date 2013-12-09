package com.dianping.phoenix.lb.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("virtualServerAction")
@Scope("prototype")
public class VirtualServerAction extends MenuAction {

    private static final int    MAX_TAG_NUM      = 10;

    private static final Logger LOG              = LoggerFactory.getLogger(VirtualServerAction.class);

    private static final long   serialVersionUID = -1084994778030229218L;

    private String              virtualServerName;

    private String              tagId;

    private Integer             version;

    private List<String>        tags;

    private List<VirtualServer> list;

    private String[]            vsListToTag;

    private String              vsListToTagStr;

    private String              tagIdsStr;

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

            pools = poolService.listPools();
            String nginxConfig = virtualServerService.generateNginxConfig(virtualServer, pools);

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

    public String addTag() throws Exception {
        try {
            Validate.notNull(virtualServerName);
            Validate.notNull(version);
            dataMap.put("tagId", virtualServerService.tag(virtualServerName, version, pools));

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            //            LOG.error("Param Error: " + e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    public String addBatchTag() throws Exception {
        System.out.println(vsListToTag);
        List<String> tagIds = new ArrayList<String>();
        if (vsListToTag != null) {
            for (String vs : vsListToTag) {
                System.out.println(vs);
                VirtualServer virtualServer = virtualServerService.findVirtualServer(vs);
                Validate.notNull(virtualServer, "vs(" + vs + ") not found.");
                tagIds.add(virtualServerService.tag(vs, virtualServer.getVersion(), pools));
            }
        }
        vsListToTagStr = StringUtils.join(vsListToTag, ',');
        tagIdsStr = StringUtils.join(tagIds, ',');
        return "redirect";
    }

    /**
     * 查看某个tagId当时的config快照
     */
    public String getNginxConfigByTagId() throws Exception {
        try {
            SlbModelTree tree = virtualServerService.findTagById(virtualServerName, tagId);
            if (tree != null) {
                List<Pool> poolList = new ArrayList<Pool>();
                Map<String, Pool> pools0 = tree.getPools();
                for (Entry<String, Pool> entry : pools0.entrySet()) {
                    Pool pool0 = entry.getValue();
                    poolList.add(pool0);
                }
                Map<String, VirtualServer> virtualServers0 = tree.getVirtualServers();
                for (Entry<String, VirtualServer> entry : virtualServers0.entrySet()) {
                    VirtualServer virtualServer = entry.getValue();
                    String config = virtualServerService.generateNginxConfig(virtualServer, poolList);
                    dataMap.put("nginxConfig", config);
                    break;
                }
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

    public String listTags() throws Exception {
        try {
            tags = virtualServerService.listTag(virtualServerName, MAX_TAG_NUM);
            //            dataMap.put("tags", tags);

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

    public String list() throws Exception {
        try {
            list = virtualServerService.listVirtualServers();

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<VirtualServer> getList() {
        return list;
    }

    public void setList(List<VirtualServer> list) {
        this.list = list;
    }

    public String[] getVsListToTag() {
        return vsListToTag;
    }

    public void setVsListToTag(String[] vsListToTag) {
        this.vsListToTag = vsListToTag;
    }

    public String getVsListToTagStr() {
        return vsListToTagStr;
    }

    public void setVsListToTagStr(String vsListToTagStr) {
        this.vsListToTagStr = vsListToTagStr;
    }

    public String getTagIdsStr() {
        return tagIdsStr;
    }

    public void setTagIdsStr(String tagIdsStr) {
        this.tagIdsStr = tagIdsStr;
    }

}
