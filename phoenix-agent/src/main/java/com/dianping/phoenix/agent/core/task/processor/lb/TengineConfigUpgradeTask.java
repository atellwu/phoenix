package com.dianping.phoenix.agent.core.task.processor.lb;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.phoenix.agent.core.task.AbstractTask;

public class TengineConfigUpgradeTask extends AbstractTask {
    public static final String HTTP_METHOD_GET    = "get";
    public static final String HTTP_METHOD_POST   = "post";
    public static final String HTTP_METHOD_DELETE = "delete";

    private String             virtualServerName;
    private String             configFileName;
    private String             version;
    private String             girUrl;
    private boolean            reload             = true;
    private String             dynamicRefreshUrl;
    private String             dynamicRefreshPostData;
    private String             refreshMethod      = HTTP_METHOD_GET;

    public TengineConfigUpgradeTask(String virtualServerName, String configFileName, String version, String girUrl,
            boolean reload, String dynamicRefreshUrl, String dynamicRefreshPostData, String refreshMethod) {
        super();
        this.virtualServerName = virtualServerName;
        this.configFileName = configFileName;
        this.version = version;
        this.girUrl = girUrl;
        this.reload = reload;
        this.dynamicRefreshUrl = dynamicRefreshUrl;
        this.dynamicRefreshPostData = dynamicRefreshPostData;
        this.refreshMethod = refreshMethod;
    }

    /**
     * for serialization
     */
    @SuppressWarnings("unused")
    private TengineConfigUpgradeTask() {
    }

    /**
     * @return the virtualServerName
     */
    public String getVirtualServerName() {
        return virtualServerName;
    }

    /**
     * @return the configFileName
     */
    public String getConfigFileName() {
        return configFileName;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the girUrl
     */
    public String getGirUrl() {
        return girUrl;
    }

    /**
     * @return the reload
     */
    public boolean isReload() {
        return reload;
    }

    /**
     * @return the dynamicRefreshUrl
     */
    public String getDynamicRefreshUrl() {
        return dynamicRefreshUrl;
    }

    public String getDynamicRefreshPostData() {
        return dynamicRefreshPostData;
    }

    public String getRefreshMethod() {
        return refreshMethod;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configFileName == null) ? 0 : configFileName.hashCode());
        result = prime * result + ((dynamicRefreshPostData == null) ? 0 : dynamicRefreshPostData.hashCode());
        result = prime * result + ((dynamicRefreshUrl == null) ? 0 : dynamicRefreshUrl.hashCode());
        result = prime * result + ((girUrl == null) ? 0 : girUrl.hashCode());
        result = prime * result + ((refreshMethod == null) ? 0 : refreshMethod.hashCode());
        result = prime * result + (reload ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((virtualServerName == null) ? 0 : virtualServerName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TengineConfigUpgradeTask other = (TengineConfigUpgradeTask) obj;
        if (configFileName == null) {
            if (other.configFileName != null)
                return false;
        } else if (!configFileName.equals(other.configFileName))
            return false;
        if (dynamicRefreshPostData == null) {
            if (other.dynamicRefreshPostData != null)
                return false;
        } else if (!dynamicRefreshPostData.equals(other.dynamicRefreshPostData))
            return false;
        if (dynamicRefreshUrl == null) {
            if (other.dynamicRefreshUrl != null)
                return false;
        } else if (!dynamicRefreshUrl.equals(other.dynamicRefreshUrl))
            return false;
        if (girUrl == null) {
            if (other.girUrl != null)
                return false;
        } else if (!girUrl.equals(other.girUrl))
            return false;
        if (refreshMethod == null) {
            if (other.refreshMethod != null)
                return false;
        } else if (!refreshMethod.equals(other.refreshMethod))
            return false;
        if (reload != other.reload)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        if (virtualServerName == null) {
            if (other.virtualServerName != null)
                return false;
        } else if (!virtualServerName.equals(other.virtualServerName))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
