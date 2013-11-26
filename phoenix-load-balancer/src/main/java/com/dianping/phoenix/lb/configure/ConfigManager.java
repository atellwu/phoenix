package com.dianping.phoenix.lb.configure;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.lb.config.entity.RuntimeConfig;
import com.dianping.phoenix.lb.config.transform.DefaultSaxParser;
import com.dianping.phoenix.lb.constant.Constants;

public class ConfigManager implements Initializable {
    @Inject
    private String        m_configFile       = "/data/appdatas/phoenix/slb/config.xml";

    private RuntimeConfig m_config;

    private boolean       m_showLogTimestamp = true;

    private void check() {
        if (m_config == null) {
            throw new RuntimeException("ConfigManager is not initialized properly!");
        }
    }

    public String getModelGitUrl() {
        check();
        if (Constants.ENV_DEV.equals(m_config.getEnv())) {
            return m_config.getModelGitUrlDev();
        } else if (Constants.ENV_PRODUCT.equals(m_config.getEnv())) {
            return m_config.getModelGitUrlProduct();
        }
        return m_config.getModelGitUrlDev();
    }

    public String getNginxCheckConfigFileName() {
        check();
        return m_config.getNginxCheckConfigFileName();
    }

    public String getNginxCheckConfigFolder() {
        check();
        return m_config.getNginxCheckConfigFolder();
    }

    public String getNginxCheckMainConfigFileName() {
        check();
        return m_config.getNginxCheckMainConfigFileName();
    }

    public int getDeployConnectTimeout() {
        check();

        return m_config.getDeployConnectTimeout();
    }

    public int getDeployGetlogRetrycount() {
        check();
        return m_config.getDeployGetlogRetrycount();
    }

    public String getDeployLogUrl(String host, int deployId) {
        check();

        String pattern = m_config.getDeployLogUrlPattern();

        return String.format(pattern, host, deployId);
    }

    public long getDeployRetryInterval() {
        check();

        int interval = m_config.getDeployRetryInterval(); // in second

        return interval;
    }

    public String getDeployStatusUrl(String host, int deployId) {
        check();

        String pattern = m_config.getDeployStatusUrlPattern();

        return String.format(pattern, host, deployId);
    }

    public String getDeployWithReloadUrl(String host, int deployId, String vsName, String configFileName, String version) {
        check();
        String gitUrl = m_config.getTengineConfigGitUrlDev();
        if (Constants.ENV_PRODUCT.equals(m_config.getEnv())) {
            gitUrl = m_config.getTengineConfigGitUrlProduct();
        }
        return String.format(m_config.getDeployUrlReloadPattern(), host, deployId, vsName, configFileName, version,
                gitUrl);
    }

    public String getDeployWithDynamicRefreshUrl(String host, int deployId, String vsName, String configFileName,
            String version, String refreshUrl, String refreshMethod) {
        check();
        String gitUrl = m_config.getTengineConfigGitUrlDev();
        if (Constants.ENV_PRODUCT.equals(m_config.getEnv())) {
            gitUrl = m_config.getTengineConfigGitUrlProduct();
        }
        return String.format(m_config.getDeployUrlDynamicRefreshPattern(), host, deployId, vsName, configFileName, version,
                gitUrl, refreshUrl, refreshMethod);
    }

    @Override
    public void initialize() throws InitializationException {
        try {
            File file = new File(m_configFile);

            if (file.isFile()) {
                m_config = DefaultSaxParser.parse(Files.forIO().readFrom(file, "utf-8"));
            } else {
                m_config = new RuntimeConfig();

            }
        } catch (Exception e) {
            throw new InitializationException(String.format("Unable to load configuration file(%s)!", m_configFile), e);
        }
        makeShellScriptExecutable();
    }

    public boolean isShowLogTimestamp() {
        return m_showLogTimestamp;
    }

    public void setConfigFile(String configFile) {
        m_configFile = configFile;
    }

    public void setDeployRetryInterval(int retryInterval) {
        check();

        m_config.setDeployRetryInterval(retryInterval);
    }

    public String getAgentTengineConfigVersionUrl(String host, String vsName) {
        check();
        return String.format(m_config.getAgentTengineConfigVersionUrlPattern(), host, vsName);
    }

    public File getLbScript() {
        check();
        return getScriptFile("lb.sh");
    }

    public File getNginxScript() {
        check();
        return getScriptFile("nginx.sh");
    }

    private File getScriptFile(String scriptFileName) {
        URL scriptUrl = this.getClass().getClassLoader().getResource("script/" + scriptFileName);
        if (scriptUrl == null) {
            throw new RuntimeException(scriptFileName + " not found");
        }
        return new File(scriptUrl.getPath());
    }

    private void makeShellScriptExecutable() {
        File scriptDir = getLbScript().getParentFile();
        Iterator<File> scriptIter = FileUtils.iterateFiles(scriptDir, new String[] { "sh" }, true);
        while (scriptIter != null && scriptIter.hasNext()) {
            scriptIter.next().setExecutable(true, false);
        }
    }
}
