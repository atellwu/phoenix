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

import com.dianping.phoenix.lb.utils.PoolNameUtils;

/**
 * @author Leo Liang
 * 
 */
public class NginxConfig {
    private NginxServer                      server;
    private Map<String, List<NginxUpstream>> upstreams = new HashMap<String, List<NginxUpstream>>();

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
            upstreamName = PoolNameUtils.getPoolNamePrefix(upstreamName);
            if (!this.upstreams.containsKey(upstreamName)) {
                this.upstreams.put(upstreamName, new ArrayList<NginxUpstream>());
            }
            this.upstreams.get(upstreamName).add(upstream);
        }
    }

    public List<NginxUpstream> getUpstream(String name) {
        return upstreams.get(PoolNameUtils.getPoolNamePrefix(name));
    }

}
