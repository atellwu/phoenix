package com.dianping.phoenix.agent.core.task.processor.slb;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.phoenix.agent.core.task.AbstractTask;

public class ConfigUpgradeTask extends AbstractTask {

    private String                    virtualServerName;
    private String                    configFileName;
    private String                    version;
    private String                    girUrl;
    private boolean                   reload = true;
    private List<Map<String, String>> dynamicRefreshPostData;

    public ConfigUpgradeTask(String virtualServerName, String configFileName, String version, String girUrl,
            boolean reload, List<Map<String, String>> dynamicRefreshPostData) {
        super();
        this.virtualServerName = virtualServerName;
        this.configFileName = configFileName;
        this.version = version;
        this.girUrl = girUrl;
        this.reload = reload;
        this.dynamicRefreshPostData = dynamicRefreshPostData;
    }

    /**
     * for serialization
     */
    @SuppressWarnings("unused")
    private ConfigUpgradeTask() {
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

    public List<Map<String, String>> getDynamicRefreshPostData() {
        return dynamicRefreshPostData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configFileName == null) ? 0 : configFileName.hashCode());
        result = prime * result + ((dynamicRefreshPostData == null) ? 0 : dynamicRefreshPostData.hashCode());
        result = prime * result + ((girUrl == null) ? 0 : girUrl.hashCode());
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
        ConfigUpgradeTask other = (ConfigUpgradeTask) obj;
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
        if (girUrl == null) {
            if (other.girUrl != null)
                return false;
        } else if (!girUrl.equals(other.girUrl))
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
