package com.dianping.phoenix.lb.deploy.agent;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.phoenix.agent.response.entity.Response;
import com.dianping.phoenix.agent.response.transform.DefaultJsonParser;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.model.nginx.NginxUpstreamServer;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;
import com.dianping.phoenix.lb.utils.GsonUtils;
import com.dianping.phoenix.lb.velocity.TemplateManager;
import com.dianping.phoenix.lb.velocity.VelocityEngineManager;
import com.dianping.phoenix.lb.visitor.NginxConfigVisitor;

public class DefaultAgentClient implements AgentClient {

    private int                  deployId;
    private String               vsName;
    private String               tag;
    private String               ip;
    private VirtualServerService virtualServerService;
    private ConfigManager        configManager;
    private AgentClientResult    result;
    private StrategyService      strategyService;

    private static final String  RESP_MSG_OK = "ok";

    private DefaultAgentClient(int deployId, String vsName, String tag, String ip,
            VirtualServerService virtualServerService, StrategyService strategyService, ConfigManager configManager) {
        super();
        this.deployId = deployId;
        this.vsName = vsName;
        this.tag = tag;
        this.ip = ip;
        this.virtualServerService = virtualServerService;
        this.configManager = configManager;
        this.strategyService = strategyService;
        this.result = new AgentClientResult();
    }

    @Override
    public void execute() {
        result.setStatus(DeployAgentStatus.PROCESSING);
        result.logInfo(String.format("Deploying phoenix-slb config(%s) to host(%s) for deploy(%s) of vs(%s)  ... ",
                tag, ip, deployId, vsName));

        String currentWorkingVersion = getAgentConfigVersion();
        if (StringUtils.isNotBlank(currentWorkingVersion)) {
            try {
                if (currentWorkingVersion.equals(tag)) {
                    result.logInfo(String
                            .format("Config of vs(%s) for host(%s) is already version %s, no need to redeploy", vsName,
                                    ip, tag));
                    endWithSuccess();
                    return;
                }

                SlbModelTree currentWorkingSlbModelTree = virtualServerService.findTagById(vsName,
                        currentWorkingVersion);
                if (currentWorkingSlbModelTree == null || currentWorkingSlbModelTree.findVirtualServer(vsName) == null) {
                    result.logError(String.format("Config with version %s not found", currentWorkingVersion));
                    endWithFail();
                    return;
                }

                SlbModelTree deployingSlbModelTree = virtualServerService.findTagById(vsName, tag);
                if (deployingSlbModelTree == null || deployingSlbModelTree.findVirtualServer(vsName) == null) {
                    result.logError(String.format("Config with version %s not found", tag));
                    endWithFail();
                    return;
                }

                VsCompareResult compareResult = compare(currentWorkingSlbModelTree, deployingSlbModelTree);

                if (compareResult.needReload) {
                    result.logInfo("Need to reload nginx");
                    URL deployUrl = new URL(configManager.getDeployWithReloadUrl(ip, deployId, vsName,
                            configManager.getTengineConfigFileName(), tag));
                    result.logInfo(String.format("Deploy url is %s", deployUrl));
                    HttpURLConnection conn = (HttpURLConnection) deployUrl.openConnection();
                    conn.setConnectTimeout(configManager.getDeployConnectTimeout());

                    Response response;

                    response = DefaultJsonParser.parse(IOUtils.toString(conn.getInputStream()));

                    if (!RESP_MSG_OK.equals(response.getStatus())) {
                        result.logError(String.format("Failed to deploy (status: %s, error msg: %s)",
                                response.getStatus(), response.getMessage()));
                        endWithFail();
                        return;
                    }
                } else {
                    result.logInfo("No need to reload nginx, use dynamic refresh strategy");
                    URL deployUrl = new URL(configManager.getDeployWithDynamicRefreshUrl(ip, deployId, vsName,
                            configManager.getTengineConfigFileName(), tag));

                    String postData = getDynamicRefreshPostData(compareResult);
                    result.logInfo(String.format("Deploy url is %s, post data is %s", deployUrl, postData));
                    HttpURLConnection conn = (HttpURLConnection) deployUrl.openConnection();
                    conn.setConnectTimeout(configManager.getDeployConnectTimeout());
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    PrintWriter out = new PrintWriter(conn.getOutputStream());
                    out.print(URLEncoder.encode("refreshPostData", "UTF-8") + "="
                            + URLEncoder.encode(postData, "UTF-8"));
                    out.flush();
                    out.close();

                    Response response;

                    response = DefaultJsonParser.parse(IOUtils.toString(conn.getInputStream()));
                    if (!RESP_MSG_OK.equals(response.getStatus())) {
                        result.logError(String.format("Failed to deploy (status: %s, error msg: %s)",
                                response.getStatus(), response.getMessage()));
                        endWithFail();
                        return;
                    }

                }

            } catch (Exception e) {
                result.logError("Exception occurs", e);
                endWithFail();
                return;
            }
        } else {
            result.logError(String.format("Failed to deploy phoenix-slb config(%s) to host(%s)", tag, ip));
            endWithFail();
            return;
        }
    }

    private VsCompareResult compare(SlbModelTree currentWorkingSlbModelTree, SlbModelTree deployingSlbModelTree) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDynamicRefreshPostData(VsCompareResult compareResult) throws BizException {
        List<Map<String, String>> postDataList = new ArrayList<Map<String, String>>();
        for (Pool pool : compareResult.addedPools) {
            Map<String, String> postData = new HashMap<String, String>();
            postData.put("url", configManager.getNginxDynamicAddUpstreamUrlPattern(pool.getName()));
            postData.put("method", "post");
            postData.put("data", generateUpstreamContent(pool));
            postDataList.add(postData);
        }

        for (Pool pool : compareResult.modifiedPools) {
            Map<String, String> postData = new HashMap<String, String>();
            postData.put("url", configManager.getNginxDynamicAddUpstreamUrlPattern(pool.getName()));
            postData.put("method", "post");
            postData.put("data", generateUpstreamContent(pool));
            postDataList.add(postData);
        }

        for (Pool pool : compareResult.deletedPools) {
            Map<String, String> postData = new HashMap<String, String>();
            postData.put("url", configManager.getNginxDynamicAddUpstreamUrlPattern(pool.getName()));
            postData.put("method", "delete");
            postDataList.add(postData);
        }

        return GsonUtils.toJson(postDataList);
    }

    private String generateUpstreamContent(Pool pool) throws BizException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("strategy", strategyService.findStrategy(pool.getLoadbalanceStrategyName()));
        List<NginxUpstreamServer> nginxUpstreamServers = new ArrayList<NginxUpstreamServer>();
        for (Member member : pool.getMembers()) {
            NginxUpstreamServer server = new NginxUpstreamServer();
            server.setMember(member);
            nginxUpstreamServers.add(server);
        }
        context.put("servers", nginxUpstreamServers);
        return VelocityEngineManager.INSTANCE.merge(
                TemplateManager.INSTANCE.getTemplate("upstream", "dynamic_upstream"), context);
    }


    private static class VsCompareResult {
        private boolean    needReload    = true;
        private List<Pool> addedPools    = new ArrayList<Pool>();
        private List<Pool> deletedPools  = new ArrayList<Pool>();
        private List<Pool> modifiedPools = new ArrayList<Pool>();
    }

    private void endWithSuccess() {
        result.setStatus(DeployAgentStatus.SUCCESS);
        result.logInfo("End deploying with status success");
    }

    private void endWithFail() {
        result.setStatus(DeployAgentStatus.FAILED);
        result.logInfo("End deploying with status failed");
    }

    private String getAgentConfigVersion() {
        try {
            URL versionUrl = new URL(configManager.getAgentTengineConfigVersionUrl(ip, vsName));
            result.logInfo(String.format("Fetching version of current working config through url %s",
                    versionUrl.toString()));

            HttpURLConnection connection = (HttpURLConnection) versionUrl.openConnection();
            connection.setConnectTimeout(configManager.getDeployConnectTimeout());

            String responseStr = IOUtils.toString(connection.getInputStream());

            Response response;

            response = DefaultJsonParser.parse(responseStr);

            if (RESP_MSG_OK.equals(response.getStatus()) && StringUtils.isNotBlank(response.getMessage())) {
                String version = StringUtils.trim(response.getMessage());
                result.logInfo(String.format("Version fetched, current working config version is %s", version));
                return version;
            } else {
                result.logError(String.format("Failed to fetch version(error msg: %s)", response.getMessage()));
                result.setStatus(DeployAgentStatus.FAILED);
            }
        } catch (Exception e) {
            result.logError("Exception occurs while fetching version of current working config", e);
            result.setStatus(DeployAgentStatus.FAILED);
        }
        return null;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
