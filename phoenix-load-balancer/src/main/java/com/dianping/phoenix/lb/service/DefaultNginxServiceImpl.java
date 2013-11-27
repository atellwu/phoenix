/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 26, 2013
 * 
 */
package com.dianping.phoenix.lb.service;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.shell.ScriptExecutor;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
@Service
public class DefaultNginxServiceImpl implements NginxService {

    private static Logger log = Logger.getLogger(DefaultNginxServiceImpl.class);
    private ConfigManager configManager;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    @Override
    public synchronized NginxCheckResult checkConfig(String configContent) throws BizException {
        File serverConf = new File(configManager.getNginxCheckConfigFolder(),
                configManager.getNginxCheckConfigFileName());
        try {
            if (serverConf.exists()) {
                FileUtils.deleteQuietly(serverConf);
            }
            FileUtils.forceMkdir(serverConf.getParentFile());
            FileUtils.writeStringToFile(serverConf, configContent);

            ScriptExecutor scriptExecutor = PlexusComponentContainer.INSTANCE.lookup(ScriptExecutor.class);
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            int exitCode = scriptExecutor.exec(getNginxCheckScriptCmd(), stdOut, stdErr);
            log.info("Nginx-check: " + stdOut);
            if (exitCode != 0) {
                return new NginxCheckResult(false, stdErr.toString());
            } else {
                return new NginxCheckResult(true, "");
            }
        } catch (Exception e) {
            ExceptionUtils.logAndRethrowBizException(e);
        } finally {
            FileUtils.deleteQuietly(serverConf);
        }
        return null;
    }

    private String getNginxCheckScriptCmd() {
        StringBuilder sb = new StringBuilder();

        sb.append(configManager.getNginxScript().getAbsolutePath());
        sb.append(String.format(" --func \"%s\" ", "nginx_check"));
        sb.append(String.format(" --config \"%s\" ", configManager.getNginxCheckMainConfigFileName()));

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        DefaultNginxServiceImpl service = new DefaultNginxServiceImpl();
        service.init();
        NginxCheckResult res = service.checkConfig("server {}");
        if (!res.isSucess()) {
            System.out.println(res.getMsg());
        } else {
            System.out.println("ok");
        }
    }

}
