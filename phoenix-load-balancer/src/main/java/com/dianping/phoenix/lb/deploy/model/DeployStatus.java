package com.dianping.phoenix.lb.deploy.model;

public enum DeployStatus {

    CREATED("新建的任务"),

    DEPLOYING("正在执行"),

    CANCELLING("已被取消"),

    PAUSING("暂停中"),

    UNKNOWN("未知状态"), WARNING("执行完成(有警告信息)"), // completed with partial failures

    FAILED("执行失败"), // complete with all failed

    SUCCESS("执行成功"); // completed with all successful

    private String desc;

    private DeployStatus(String desc) {
        this.desc = desc;
    }

    //    public static DeployStatus getById(int id, DeployStatus defaultStatus) {
    //        for (DeployStatus status : DeployStatus.values()) {
    //            if (status.getId() == id) {
    //                return status;
    //            }
    //        }
    //
    //        return defaultStatus;
    //    }

    //    public static DeployStatus getByName(String name, DeployStatus defaultStatus) {
    //        for (DeployStatus status : DeployStatus.values()) {
    //            if (status.getName().equals(name)) {
    //                return status;
    //            }
    //        }
    //
    //        return defaultStatus;
    //    }

    public static boolean isFinalStatus(DeployStatus status) {
        return status == SUCCESS || status == WARNING;
    }

    public String getDesc() {
        return desc;
    }

    //    public int getId() {
    //        return m_id;
    //    }

}
