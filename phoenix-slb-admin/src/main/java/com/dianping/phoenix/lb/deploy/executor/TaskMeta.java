//package com.dianping.phoenix.lb.deploy.executor;
//
//public class TaskMeta {
//    /**
//     * private String m_deployLogUrlPattern = "http://%s:3473/phoenix/agent/nginx?op=log&deployId=%s";
//     * 
//     * private String m_deployStatusUrlPattern = "http://%s:3473/phoenix/agent/nginx?op=status&deployId=%s";
//     * 
//     * private String m_deployUrlReloadPattern = "http://%s:3473/phoenix/agent/nginx?op=deploy&deployId=%s&vs=%s&config=%s&version=%s&gitUrl=%s&reload=true";
//     * 
//     * private String m_deployUrlDynamicRefreshPattern = "http://%s:3473/phoenix/agent/nginx?op=deploy&deployId=%s&vs=%s&config=%s&version=%s&gitUrl=%s&reload=false&refreshUrl=%s&refreshMethod=%s";
//     * 
//     * private String m_agentTengineConfigVersionUrlPattern = "http://%s:3473/phoenix/agent/nginx?op=version&vs=%s";
//     */
//
//    /**
//     * 
//     * http://127.0.0.1:3473/phoenix/agent/nginx?op=deploy&deployId=%s&vs=tuangou&config=server.conf&version=tuangou-4&gitUrl=ssh://git@code.dianpingoa.com/arch/phoenix-slb-tengine-config-dev.git&
//     * reload=true
//     * 
//     * http://127.0.0.1:3473/phoenix/agent/nginx?op=log&deployId=%s
//     * 
//     */
//
//    private Integer deployId;
//
//    private String  vs;
//
//    private String  version;
//
//    public Integer getDeployId() {
//        return deployId;
//    }
//
//    public void setDeployId(Integer deployId) {
//        this.deployId = deployId;
//    }
//
//    public String getVs() {
//        return vs;
//    }
//
//    public void setVs(String vs) {
//        this.vs = vs;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    @Override
//    public String toString() {
//        return String.format("TaskMeta [deployId=%s, vs=%s, version=%s]", deployId, vs, version);
//    }
//
//}
