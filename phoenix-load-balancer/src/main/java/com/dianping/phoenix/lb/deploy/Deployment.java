package com.dianping.phoenix.lb.deploy;

import java.util.List;


public class Deployment {
    /** 发布的VirtualServer的tag */
    private String                        tag;

    /** 发布的vs */
    private List<VirtualServerDeployment> vsList;

    /** 发布策略 */
    private BatchPolicy                   batchPolicy;

    /** 错误处理：一台机器发生错误时，中断还是继续 */
    private boolean                       abortOnError = true;

    /** 发布控制：手动还是自动（如果是自动，需要设置时间间隔） */
    private boolean                       autoContinue;

    /** 发布的时间间隔 */
    private int                           deployInterval;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BatchPolicy getDeployPolicy() {
        return batchPolicy;
    }

    public void setDeployPolicy(BatchPolicy deployPolicy) {
        this.batchPolicy = deployPolicy;
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

    public static enum BatchPolicy {
        ONE_BY_ONE("one-by-one", 1, "1 -> 1 -> 1 -> 1 (每次一台)"),

        TWO_BY_TWO("two-by-two", 2, "1 -> 2 -> 2 -> 2 (每次两台)"),

        THREE_BY_THREE("three-by-three", 3, "1 -> 3 -> 3 -> 3 (每次三台)");

        private String m_id;

        private int    m_batchSize;

        private String m_description;

        private BatchPolicy(String id, int batchSize, String description) {
            m_id = id;
            m_batchSize = batchSize;
            m_description = description;
        }

        public int getBatchSize() {
            return m_batchSize;
        }

        public String getId() {
            return m_id;
        }

        public String getDescription() {
            return m_description;
        }
    }

}
