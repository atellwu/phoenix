/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 1, 2013
 * 
 */
package com.dianping.phoenix.lb.velocity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.dianping.phoenix.lb.constant.Constants;
import com.dianping.phoenix.lb.model.Availability;
import com.dianping.phoenix.lb.model.State;
import com.dianping.phoenix.lb.model.entity.Directive;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.nginx.NginxLocation.MatchType;
import com.dianping.phoenix.lb.model.nginx.NginxUpstreamServer;
import com.dianping.phoenix.lb.utils.PoolNameUtils;

/**
 * @author Leo Liang
 * 
 */
public class NginxVelocityTools {

    public String locationMatchOp(MatchType matchType) {
        switch (matchType) {
            case COMMON:
                return "";
            case PREFIX:
                return "^~";
            case REGEX_CASE_INSENSITIVE:
                return "~*";
            case REGEX_CASE_SENSITIVE:
                return "~";
            case EXACT:
                return "=";
            default:
                return "";
        }
    }

    public String rewriteProxyPassStringIfNeeded(String prefix, String text) {
        if (text.startsWith(Constants.DIRECTIVE_PROXY_PASS + " ")) {
            int poolNameStart = text.indexOf("http://");
            if (poolNameStart >= 0) {
                String poolName = text.substring(poolNameStart + "http://".length());
                return text.substring(0, poolNameStart) + "http://"
                        + PoolNameUtils.rewriteToPoolNamePrefix(prefix, poolName);
            }
        }
        return text;
    }

    public String poolName(String prefix, String poolName) {
        return PoolNameUtils.rewriteToPoolNamePrefix(prefix, poolName);
    }

    public String properties(Map<String, String> properties) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String template = getTemplate("properties", entry.getKey());
            if (StringUtils.isNotBlank(template)) {
                Map<String, Object> context = new HashMap<String, Object>();
                context.put("value", entry.getValue());
                content.append(VelocityEngineManager.INSTANCE.merge(template, context));
            } else {
                content.append("    " + entry.getKey() + " " + entry.getValue()).append(";");
            }
            content.append("\n");
        }
        return content.toString();
    }

    public String lbStrategy(Strategy strategy) {
        String template = getTemplate("strategy", strategy.getType());
        if (StringUtils.isNotBlank(template)) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("strategy", strategy);
            return VelocityEngineManager.INSTANCE.merge(template, context);
        } else {
            return "";
        }
    }

    public String directive(String vsName, Directive directive) {
        String template = getTemplate("directive", directive.getType());
        if (StringUtils.isNotBlank(template)) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("directive", directive);
            if (Constants.DIRECTIVE_PROXY_PASS.equals(directive.getType())) {
                context.put(
                        "dp_domain",
                        PoolNameUtils.rewriteToPoolNamePrefix(vsName,
                                directive.getDynamicAttribute(Constants.DIRECTIVE_PROXY_PASS_POOL_NAME)));
            } else if (Constants.DIRECTIVE_PROXY_IFELSE.equals(directive.getType())) {
                context.put("vsName", vsName);
            }
            return VelocityEngineManager.INSTANCE.merge(template, context);
        } else {
            return "";
        }
    }

    public String upstreamServer(NginxUpstreamServer server, Strategy strategy) {
        if (server.getMember().getAvailability() == Availability.AVAILABLE
                && server.getMember().getState() == State.ENABLED) {
            String template = getTemplate("upstream", "hash".equals(strategy.getType()) ? "default_hash" : "default");
            if (StringUtils.isNotBlank(template)) {
                Map<String, Object> context = new HashMap<String, Object>();
                context.put("server", server);
                return VelocityEngineManager.INSTANCE.merge(template, context);
            }
        }
        return "";
    }

    public String nowTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private String getTemplate(String schema, String file) {
        return TemplateManager.INSTANCE.getTemplate(schema, file);
    }
}
