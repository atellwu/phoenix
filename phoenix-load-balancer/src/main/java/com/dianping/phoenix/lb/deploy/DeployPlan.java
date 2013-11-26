package com.dianping.phoenix.lb.deploy;

import java.util.List;

public class DeployPlan {

    /** 待发布的VirtualServer的tag */
    private String       tag;

    /** 待发布的机器 */
    private List<String> hosts;

    /** 发布策略 */
    private DeployPolicy deployPolicy;

    /** 错误处理：一台机器发生错误时，中断还是继续 */
    private boolean      abortOnError = true;

    /** 发布控制：手动还是自动（如果是自动，需要设置时间间隔） */
    private boolean      autoContinue;

    /** 发布的时间间隔 */
    private int          deployInterval;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public DeployPolicy getDeployPolicy() {
        return deployPolicy;
    }

    public void setDeployPolicy(DeployPolicy deployPolicy) {
        this.deployPolicy = deployPolicy;
    }

    public boolean isAbortOnError() {
        return abortOnError;
    }

    public void setAbortOnError(boolean abortOnError) {
        this.abortOnError = abortOnError;
    }

    public boolean isAutoContinue() {
        return autoContinue;
    }

    public void setAutoContinue(boolean autoContinue) {
        this.autoContinue = autoContinue;
    }

    public int getDeployInterval() {
        return deployInterval;
    }

    public void setDeployInterval(int deployInterval) {
        this.deployInterval = deployInterval;
    }

    @Override
    public String toString() {
        return "DeployPlan [tag=" + tag + ", hosts=" + hosts + ", deployPolicy=" + deployPolicy + ", abortOnError=" + abortOnError + ", autoContinue=" + autoContinue + ", deployInterval="
                + deployInterval + "]";
    }

}
