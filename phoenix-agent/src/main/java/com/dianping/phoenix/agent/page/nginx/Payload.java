package com.dianping.phoenix.agent.page.nginx;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.ErrorObject;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.phoenix.agent.AgentPage;
import com.dianping.phoenix.agent.core.task.processor.lb.TengineConfigUpgradeTask;

public class Payload implements ActionPayload<AgentPage, Action> {
    private AgentPage m_page;

    @FieldMeta("op")
    private Action    m_action               = Action.VIEW;
    @FieldMeta("deployId")
    private long      m_deployId;
    @FieldMeta("vs")
    private String    m_virtualServerName;
    @FieldMeta("config")
    private String    m_configFileName;
    @FieldMeta("version")
    private String    m_version;
    @FieldMeta("gitUrl")
    private String    m_girUrl;
    @FieldMeta("reload")
    private String    m_reload               = "true";
    @FieldMeta("refreshUrl")
    private String    m_dynamicRefreshUrl;
    @FieldMeta("refreshPostData")
    private String    m_dynamicRefreshPostData;
    @FieldMeta("refreshMethod")
    private String    m_dynamicRefreshMethod = TengineConfigUpgradeTask.HTTP_METHOD_GET;
    @FieldMeta("offset")
    private int       m_offset;
    @FieldMeta("br")
    private int       m_br;

    public int getOffset() {
        return m_offset;
    }

    public void setOffset(int offset) {
        m_offset = offset;
    }

    public int getBr() {
        return m_br;
    }

    public void setBr(int br) {
        m_br = br;
    }

    public void setAction(String action) {
        m_action = Action.getByName(action, Action.VIEW);
    }

    @Override
    public Action getAction() {
        return m_action;
    }

    @Override
    public AgentPage getPage() {
        return m_page;
    }

    @Override
    public void setPage(String page) {
        m_page = AgentPage.getByName(page, AgentPage.NGINX);
    }

    public String getVirtualServerName() {
        return m_virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        m_virtualServerName = virtualServerName;
    }

    public String getConfigFileName() {
        return m_configFileName;
    }

    public void setConfigFileName(String configFileName) {
        m_configFileName = configFileName;
    }

    public String getVersion() {
        return m_version;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    public String getGirUrl() {
        return m_girUrl;
    }

    public void setGirUrl(String girUrl) {
        m_girUrl = girUrl;
    }

    public boolean isReload() {
        return Boolean.valueOf(m_reload);
    }

    public void setReload(String reload) {
        m_reload = reload;
    }

    public String getDynamicRefreshUrl() {
        return m_dynamicRefreshUrl;
    }

    public void setDynamicRefreshUrl(String dynamicRefreshUrl) {
        m_dynamicRefreshUrl = dynamicRefreshUrl;
    }

    public String getDynamicRefreshPostData() {
        return m_dynamicRefreshPostData;
    }

    public void setDynamicRefreshPostData(String dynamicRefreshPostData) {
        m_dynamicRefreshPostData = dynamicRefreshPostData;
    }

    public String getDynamicRefreshMethod() {
        return m_dynamicRefreshMethod;
    }

    public void setDynamicRefreshMethod(String refreshMethod) {
        m_dynamicRefreshMethod = refreshMethod;
    }

    public long getDeployId() {
        return m_deployId;
    }

    public void setDeployId(long deployId) {
        m_deployId = deployId;
    }

    @Override
    public void validate(ActionContext<?> ctx) {
        switch (m_action) {
            case DEPLOY:
                checkCommonArguments(ctx);
                if (StringUtils.isBlank(m_virtualServerName)) {
                    ctx.addError(new ErrorObject("vsName.missing"));
                }
                if (StringUtils.isBlank(m_girUrl)) {
                    ctx.addError(new ErrorObject("gitUrl.missing"));
                }

                if (StringUtils.isBlank(m_version)) {
                    ctx.addError(new ErrorObject("version.missing"));
                }

                if (StringUtils.isBlank(m_configFileName)) {
                    ctx.addError(new ErrorObject("configFileName.missing"));
                }

                if (StringUtils.isBlank(m_reload) && !"true".equalsIgnoreCase(m_reload)
                        && !"false".equalsIgnoreCase(m_reload)) {
                    ctx.addError(new ErrorObject("reload.missing"));
                }

                if (!Boolean.parseBoolean(m_reload) && StringUtils.isBlank(m_dynamicRefreshUrl)) {
                    ctx.addError(new ErrorObject("refreshUrl.missing"));
                }

                if (!Boolean.parseBoolean(m_reload) && StringUtils.isBlank(m_dynamicRefreshMethod)
                        && !TengineConfigUpgradeTask.HTTP_METHOD_DELETE.equalsIgnoreCase(m_dynamicRefreshMethod)
                        && !TengineConfigUpgradeTask.HTTP_METHOD_GET.equalsIgnoreCase(m_dynamicRefreshMethod)
                        && !TengineConfigUpgradeTask.HTTP_METHOD_POST.equalsIgnoreCase(m_dynamicRefreshMethod)) {
                    ctx.addError(new ErrorObject("refreshMethod.missing"));
                }

                break;

        }
    }

    private void checkCommonArguments(ActionContext<?> ctx) {
        if (!validDeployId(m_deployId)) {
            ctx.addError(new ErrorObject("deployId.invalid"));
        }
    }

    private boolean validDeployId(long deployId) {
        return deployId > 0;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
