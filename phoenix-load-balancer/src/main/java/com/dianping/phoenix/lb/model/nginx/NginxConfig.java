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
    private NginxServer                server;
    private Map<String, NginxUpstream> upstreams = new HashMap<String, NginxUpstream>();

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
        return new ArrayList<NginxUpstream>(upstreams.values());
    }

    public void addUpstream(NginxUpstream upstream) {
        this.upstreams.put(upstream.getName(), upstream);
    }

    public NginxUpstream getUpstream(String name) {
        return upstreams.get(name);
    }

}
