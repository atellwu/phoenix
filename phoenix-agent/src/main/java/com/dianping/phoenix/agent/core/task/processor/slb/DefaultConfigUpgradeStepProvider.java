package com.dianping.phoenix.agent.core.task.processor.slb;

import java.net.URI;
import java.net.URISyntaxException;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.agent.core.task.workflow.Context;
import com.dianping.phoenix.agent.core.task.workflow.Step;
import com.dianping.phoenix.configure.ConfigManager;

public class DefaultConfigUpgradeStepProvider extends ContainerHolder implements
        ConfigUpgradeStepProvider {

    @Inject
    private ConfigManager config;

    private int runShellCmd(String shellFunc, Context ctx) throws Exception {
        ConfigUpgradeContext myCtx = (ConfigUpgradeContext) ctx;
        String script = jointShellCmd(shellFunc, (ConfigUpgradeTask) myCtx.getTask());
        int exitCode = myCtx.getScriptExecutor().exec(script, myCtx.getLogOut(), myCtx.getLogOut());
        return exitCode;
    }

    private String jointShellCmd(String shellFunc, ConfigUpgradeTask task) {
        StringBuilder sb = new StringBuilder();
        String tengineConfigGitDocBase = String.format(config.getTengineConfigGitDocBasePattern(), task.getVersion());
        String tengineConfigDocBase = String.format(config.getTengineConfigDocBasePattern(),
                task.getVirtualServerName(), task.getVersion());

        String gitUrl = task.getGirUrl();
        String gitHost = null;
        try {
            gitHost = new URI(gitUrl).getHost();
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("error parsing host from git url %s", gitUrl), e);
        }

        sb.append(config.getTengineScriptFile().getAbsolutePath());
        sb.append(String.format(" --tengine_config_doc_base \"%s\" ", tengineConfigDocBase));
        sb.append(String.format(" --config_file \"%s\" ", task.getConfigFileName()));
        sb.append(String.format(" --virtual_server_name \"%s\" ", task.getVirtualServerName()));
        sb.append(String.format(" --version \"%s\" ", task.getVersion()));
        sb.append(String.format(" --tengine_config_git_doc_base \"%s\" ", tengineConfigGitDocBase));
        sb.append(String.format(" --env \"%s\" ", config.getEnv()));
        sb.append(String.format(" --git_url \"%s\" ", gitUrl));
        sb.append(String.format(" --git_host \"%s\" ", gitHost));
        sb.append(String.format(" --tengine_reload \"%s\" ", task.isReload() ? "1" : "0"));
        if (!task.isReload()) {
            sb.append(String.format(" --dynamic_refresh_url \"%s\" ", task.getDynamicRefreshUrl()));
            sb.append(String.format(" --dynamic_refresh_post_data \"%s\" ", task.getDynamicRefreshPostData()));
            sb.append(String.format(" --refresh_method \"%s\" ", task.getRefreshMethod()));
        }
        sb.append(String.format(" --func \"%s\" ", shellFunc));

        return sb.toString();
    }

    @Override
    public int init(Context ctx) throws Exception {
        return runShellCmd("init", ctx);
    }

    @Override
    public int checkArgument(Context ctx) throws Exception {
        return Step.CODE_OK;
    }

    @Override
    public int copyConfig(Context ctx) throws Exception {
        return runShellCmd("copy_config", ctx);
    }

    @Override
    public int gitPull(Context ctx) throws Exception {
        return runShellCmd("git_pull", ctx);
    }

    @Override
    public int reloadOrDynamicRefreshConfig(Context ctx) throws Exception {
        return runShellCmd("reload_or_dynamic_refresh_config", ctx);
    }

    @Override
    public int commit(Context ctx) throws Exception {
        return runShellCmd("commit", ctx);
    }

    @Override
    public int rollback(Context ctx) throws Exception {
        return runShellCmd("rollback", ctx);
    }
}
