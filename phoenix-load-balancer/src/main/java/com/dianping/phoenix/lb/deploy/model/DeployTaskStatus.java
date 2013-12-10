package com.dianping.phoenix.lb.deploy.model;

public enum DeployTaskStatus {

    CREATED("新建的任务"), //创建完，选择了vs，未选择agent

    WAITING("已就绪，未执行"), //创建完，选择了vs和选择agent，可以被点击启动执行

    READY("即将执行"), //已准备好，等待executor调度 （只有这个状态可被执行）

    PROCESSING("正在执行"), //内存状态，不需要持久化

    PAUSING("暂停中"),

    FAILED("执行失败"), // complete with all failed

    CANCELLING("已被取消"),

    SUCCESS("执行成功"); // completed with all successful

    private String desc;

    private DeployTaskStatus(String desc) {
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

    public static boolean isFinalStatus(DeployTaskStatus status) {
        return status == SUCCESS || status == CANCELLING;
    }

    public String getDesc() {
        return desc;
    }

    //    public int getId() {
    //        return m_id;
    //    }

}
