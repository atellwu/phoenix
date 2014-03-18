//package com.dianping.phoenix.lb.deploy.executor;
//
//import java.util.Map;
//
//import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
//
//public class TaskStatus {
//
//    private DeployTaskStatus          status;
//
//    private Map<String, VsStatus> vsStatusMap;
//
//    public static class VsStatus {
//        private Map<String, HostStatus> hostStatusMap;
//
//        public Map<String, HostStatus> getHostStatusMap() {
//            return hostStatusMap;
//        }
//
//        public void setHostStatusMap(Map<String, HostStatus> hostStatusMap) {
//            this.hostStatusMap = hostStatusMap;
//        }
//
//    }
//
//    public static class HostStatus {
//        private DeployTaskStatus status;
//        private String       rawLog;
//
//        public DeployTaskStatus getStatus() {
//            return status;
//        }
//
//        public void setStatus(DeployTaskStatus status) {
//            this.status = status;
//        }
//
//        public String getRawLog() {
//            return rawLog;
//        }
//
//        public void setRawLog(String rawLog) {
//            this.rawLog = rawLog;
//        }
//
//    }
//
//    public DeployTaskStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(DeployTaskStatus status) {
//        this.status = status;
//    }
//
//    public Map<String, VsStatus> getVsStatusMap() {
//        return vsStatusMap;
//    }
//
//    public void setVsStatusMap(Map<String, VsStatus> vsStatusMap) {
//        this.vsStatusMap = vsStatusMap;
//    }
//
//}
