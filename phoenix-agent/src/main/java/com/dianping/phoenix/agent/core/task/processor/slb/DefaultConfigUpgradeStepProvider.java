package com.dianping.phoenix.agent.core.task.processor.slb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.agent.core.task.workflow.Context;
import com.dianping.phoenix.agent.core.task.workflow.Step;
import com.dianping.phoenix.configure.ConfigManager;

public class DefaultConfigUpgradeStepProvider extends ContainerHolder implements ConfigUpgradeStepProvider {

    @Inject
    private ConfigManager config;

    private int runShellCmd(String shellFunc, Context ctx) throws Exception {
        ConfigUpgradeContext myCtx = (ConfigUpgradeContext) ctx;
        String script = jointShellCmd(shellFunc, (ConfigUpgradeTask) myCtx.getTask());
        int exitCode = myCtx.getScriptExecutor().exec(script, myCtx.getLogOut(), myCtx.getLogOut());
        return exitCode;
    }

    private int runDynamicRefreshShellCmd(Context ctx) throws Exception {
        ConfigUpgradeContext myCtx = (ConfigUpgradeContext) ctx;

        ConfigUpgradeTask task = (ConfigUpgradeTask) ctx.getTask();

        for (Map<String, String> postDataItem : task.getDynamicRefreshPostData()) {
            StringBuilder script = new StringBuilder();
            script.append(config.getTengineScriptFile().getAbsolutePath());
            script.append(String.format(" --env \"%s\" ", config.getEnv()));
            script.append(String.format(" --dynamic_refresh_url \"%s\" ", postDataItem.get("url")));
            script.append(String.format(" --refresh_method \"%s\" ", postDataItem.get("method").toUpperCase()));
            script.append(String.format(" --dynamic_refresh_post_data \"%s\" ", postDataItem.get("data")));
            script.append(String.format(" --func \"%s\" ", "dynamic_refresh_config"));
            int exitCode = myCtx.getScriptExecutor().exec(script.toString(), myCtx.getLogOut(), myCtx.getLogOut());

            if (exitCode != 0) {
                return exitCode;
            }
        }

        return 0;
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
        if (((ConfigUpgradeTask) ctx.getTask()).isReload()) {
            return runShellCmd("reload_config", ctx);
        } else {
            return runDynamicRefreshShellCmd(ctx);
        }
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
