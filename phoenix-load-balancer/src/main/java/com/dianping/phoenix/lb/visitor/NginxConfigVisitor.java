/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Oct 30, 2013
 * 
 */
package com.dianping.phoenix.lb.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.phoenix.lb.constant.Constants;
import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.model.entity.Directive;
import com.dianping.phoenix.lb.model.entity.Location;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.model.nginx.NginxConfig;
import com.dianping.phoenix.lb.model.nginx.NginxLocation;
import com.dianping.phoenix.lb.model.nginx.NginxLocation.MatchType;
import com.dianping.phoenix.lb.model.nginx.NginxServer;
import com.dianping.phoenix.lb.model.nginx.NginxUpstream;
import com.dianping.phoenix.lb.model.nginx.NginxUpstreamServer;
import com.dianping.phoenix.lb.utils.MessageUtils;
import com.dianping.phoenix.lb.utils.PoolNameUtils;

/**
 * @author Leo Liang
 * 
 */
public class NginxConfigVisitor extends AbstractVisitor<NginxConfig> {
    private Map<String, Strategy> strategies    = new HashMap<String, Strategy>();
    private Map<String, Aspect>   commonAspects = new HashMap<String, Aspect>();

    public NginxConfigVisitor() {
        result = new NginxConfig();
    }

    @Override
    public void visitStrategy(Strategy strategy) {
        strategies.put(strategy.getName(), strategy);
    }

    @Override
    public void visitVirtualServer(VirtualServer virtualServer) {
        result.setName(virtualServer.getName());
        NginxServer server = new NginxServer();
        server.setProperties(virtualServer.getDynamicAttributes());
        server.setListen(virtualServer.getPort());
        server.setServerName(virtualServer.getDomain());
        server.setDefaultPool(virtualServer.getDefaultPoolName());
        result.setServer(server);

        super.visitVirtualServer(virtualServer);

        setUpstreamsAsUsed(
                virtualServer.getDefaultPoolName(),
                MessageUtils.getMessage(MessageID.VIRTUALSERVER_DEFAULTPOOL_NOT_EXISTS,
                        virtualServer.getDefaultPoolName()));
    }

    @Override
    public void visitAspect(Aspect aspect) {
        if (StringUtils.isNotBlank(aspect.getName())) {
            commonAspects.put(aspect.getName(), aspect);
            return;
        }

        Aspect actualAspect = null;
        if (StringUtils.isBlank(aspect.getRef())) {
            actualAspect = aspect;
        } else {
            if (!commonAspects.containsKey(aspect.getRef())) {
                throw new RuntimeException(MessageUtils.getMessage(MessageID.COMMON_ASPECT_SAVE_FAIL, aspect.getRef()));
            } else {
                actualAspect = commonAspects.get(aspect.getRef());
            }
        }
        result.getServer().addAspect(actualAspect);
        setUpstreamAsUsed(actualAspect.getDirectives(),
                MessageUtils.getMessage(MessageID.PROXY_PASS_NO_POOL, "aspect " + aspect.getPointCut()));
    }

    private void setUpstreamAsUsed(List<Directive> directives, String errorContent) {
        for (Directive directive : directives) {
            if (Constants.DIRECTIVE_PROXY_PASS.equals(directive.getType())) {
                setUpstreamsAsUsed(directive.getDynamicAttribute(Constants.DIRECTIVE_PROXY_PASS_POOL_NAME),
                        errorContent);
            } else if (Constants.DIRECTIVE_PROXY_IFELSE.equals(directive.getType())) {
                String content = StringUtils.trimToEmpty(directive.getDynamicAttribute("if-statement"));
                if (content.startsWith(Constants.DIRECTIVE_PROXY_PASS + " ")) {
                    String poolName = PoolNameUtils.extractPoolNameFromProxyPassString(content);
                    if (StringUtils.isNotBlank(poolName)) {
                        setUpstreamsAsUsed(poolName, errorContent);
                    }
                }

                content = StringUtils.trimToEmpty(directive.getDynamicAttribute("else-statement"));
                if (content.startsWith(Constants.DIRECTIVE_PROXY_PASS + " ")) {
                    String poolName = PoolNameUtils.extractPoolNameFromProxyPassString(content);
                    if (StringUtils.isNotBlank(poolName)) {
                        setUpstreamsAsUsed(poolName, errorContent);
                    }
                }
            }
        }
    }

    private void setUpstreamsAsUsed(String poolName, String errorContent) {
        List<NginxUpstream> upstreams = result.getUpstream(poolName);
        if (upstreams == null || upstreams.isEmpty()) {
            throw new RuntimeException(errorContent);
        }
        for (NginxUpstream upstream : upstreams) {
            upstream.setUsed(true);
        }
    }

    @Override
    public void visitPool(Pool pool) {

        NginxUpstream upstream = new NginxUpstream();

        upstream.setLbStrategy(strategies.get(pool.getLoadbalanceStrategyName()));
        upstream.setName(toUpstreamName(pool.getName()));

        for (Member member : pool.getMembers()) {
            NginxUpstreamServer nginxUpstreamServer = new NginxUpstreamServer();
            nginxUpstreamServer.setMember(member);
            upstream.addServer(nginxUpstreamServer);
        }

        result.addUpstream(upstream);
    }

    @Override
    public void visitLocation(Location location) {
        NginxLocation nginxLocation = new NginxLocation();
        nginxLocation.setMatchType(toNginxMatchType(location));
        nginxLocation.setPattern(location.getPattern());
        setUpstreamAsUsed(location.getDirectives(),
                MessageUtils.getMessage(MessageID.PROXY_PASS_NO_POOL, location.getPattern()));
        for (Directive directive : location.getDirectives()) {
            nginxLocation.addDirective(directive);
        }

        result.getServer().addLocations(nginxLocation);
    }

    private String toUpstreamName(String poolName) {
        return poolName;
    }

    private MatchType toNginxMatchType(Location location) {
        if (Constants.LOCATION_MATCHTYPE_PREFIX.equals(location.getMatchType())) {
            return MatchType.PREFIX;
        } else if (Constants.LOCATION_MATCHTYPE_REGEX.equals(location.getMatchType())) {
            return location.getCaseSensitive() ? MatchType.REGEX_CASE_SENSITIVE : MatchType.REGEX_CASE_INSENSITIVE;
        } else if (Constants.LOCATION_MATCHTYPE_EXACT.equals(location.getMatchType())) {
            return MatchType.EXACT;
        } else {
            return MatchType.COMMON;
        }
    }

}
