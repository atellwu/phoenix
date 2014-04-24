/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 25, 2013
 * 
 */
package com.dianping.phoenix.lb.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.shell.ScriptExecutor;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
@Service
public class DefaultGitServiceImpl implements GitService {

    private ConfigManager configManager;

    @PostConstruct
    public void init() throws ComponentLookupException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);
    }

    private ScriptExecutor getScriptExecutor() throws BizException {
        try {
            return PlexusComponentContainer.INSTANCE.lookup(ScriptExecutor.class);
        } catch (ComponentLookupException e) {
            ExceptionUtils.logAndRethrowBizException(e);
        }
        return null;
    }

    private String getShellCmd(String shellFunc, String gitUrl, String targetDir, String tag, String comment)
            throws BizException {
        StringBuilder sb = new StringBuilder();

        String gitHost = null;
        if (StringUtils.isNotBlank(gitUrl)) {
            try {
                gitHost = new URI(gitUrl).getHost();
            } catch (URISyntaxException e) {
                ExceptionUtils.logAndRethrowBizException(e);
                throw new RuntimeException(String.format("error parsing host from git url %s", gitUrl), e);
            }
        }

        sb.append(configManager.getGitScript().getAbsolutePath());
        if (StringUtils.isNotBlank(gitUrl)) {
            sb.append(String.format(" --git_url \"%s\" ", gitUrl));
            sb.append(String.format(" --git_host \"%s\" ", gitHost));
        }
        sb.append(String.format(" --target_dir \"%s\" ", targetDir));
        sb.append(String.format(" --tag \"%s\" ", StringUtils.trimToEmpty(tag)));
        if (StringUtils.isNotBlank(comment)) {
            sb.append(String.format(" --comment \"%s\" ", comment));
        }
        sb.append(String.format(" --func \"%s\" ", shellFunc));

        return sb.toString();
    }

    private void exec(String script) throws BizException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int exitCode = getScriptExecutor().exec(script, baos, baos);
            if (exitCode != 0) {
                ExceptionUtils.throwBizException(MessageID.GIT_EXCEPTION, baos.toString());
            }
        } catch (IOException e) {
            ExceptionUtils.logAndRethrowBizException(e);
        }
    }

    @Override
    public synchronized void commitAllChanges(String targetDir, String comment) throws BizException {
        exec(getShellCmd("commit_all_changes", null, targetDir, null, comment));
    }

    @Override
    public synchronized void tagAndPush(String gitUrl, String targetDir, String tag, String comment)
            throws BizException {
        exec(getShellCmd("tag_and_push", gitUrl, targetDir, tag, comment));
    }

    @Override
    public synchronized void push(String gitUrl, String targetDir) throws BizException {
        exec(getShellCmd("push", gitUrl, targetDir, null, null));
    }

    @Override
    public synchronized void clone(String gitUrl, String targetDir, String tag) throws BizException {
        exec(getShellCmd("clone", gitUrl, targetDir, tag, null));

    }

    @Override
    public synchronized void rollback(String targetDir) throws BizException {
        exec(getShellCmd("rollback", null, targetDir, null, null));
    }

    public static void main(String[] args) throws ComponentLookupException, BizException, IOException {
        DefaultGitServiceImpl service = new DefaultGitServiceImpl();
        service.init();
        String targetDir = "/Users/leoleung/test";
        String gitUrl = "ssh://git@code.dianpingoa.com/arch/phoenix-load-balancer.git";
        service.clone(gitUrl, targetDir, null);
        FileUtils.writeStringToFile(new File(targetDir, System.currentTimeMillis() + ".t"),
                Long.toString(System.currentTimeMillis()));
        // service.commitAllChanges(targetDir, "test-lll");
        // service.tagAndPush(gitUrl, targetDir, "test-lll", "ccccd");
        service.rollback(targetDir);
    }

    @Override
    public synchronized void commitAllChangesAndTagAndPush(String gitUrl, String targetDir, String tag, String comment)
            throws BizException {
        commitAllChanges(targetDir, comment);
        tagAndPush(gitUrl, targetDir, tag, comment);
    }

}
