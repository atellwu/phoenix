/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Oct 30, 2013
 * 
 */
package com.dianping.phoenix.lb.model.nginx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leo Liang
 * 
 */
public class NginxConfig {
    private static final String              UPSTREAM_NAME_PREFIX_SEPARATOR = "@";
    private NginxServer                      server;
    private Map<String, List<NginxUpstream>> upstreams                      = new HashMap<String, List<NginxUpstream>>();

    /**
     * @return the servers
     */
    public NginxServer getServer() {
        return server;
    }

    /**
     * @param servers
     *            the servers to set
     */
    public void setServer(NginxServer server) {
        this.server = server;
    }

    /**
     * @return the upstreams
     */
    public List<NginxUpstream> getUpstreams() {
        List<NginxUpstream> upstreamList = new ArrayList<NginxUpstream>();
        for (List<NginxUpstream> ele : upstreams.values()) {
            upstreamList.addAll(ele);
        }
        return upstreamList;
    }

    public void addUpstream(NginxUpstream upstream) {
        String upstreamName = upstream.getName();
        if (upstreamName != null) {
            upstreamName = removeUpstreamNameSuffix(upstreamName);
            if (!this.upstreams.containsKey(upstreamName)) {
                this.upstreams.put(upstreamName, new ArrayList<NginxUpstream>());
            }
            this.upstreams.get(upstreamName).add(upstream);
        }
    }

    private String removeUpstreamNameSuffix(String upstreamName) {
        int prefixSeparatorPos = upstreamName.indexOf(UPSTREAM_NAME_PREFIX_SEPARATOR);
        if (prefixSeparatorPos > 0) {
            upstreamName = upstreamName.substring(0, prefixSeparatorPos);
        }

        return upstreamName;
    }

    public List<NginxUpstream> getUpstream(String name) {
        return upstreams.get(removeUpstreamNameSuffix(name));
    }

}
