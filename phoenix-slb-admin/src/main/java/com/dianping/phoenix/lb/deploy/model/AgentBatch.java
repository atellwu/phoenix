package com.dianping.phoenix.lb.deploy.model;

public enum AgentBatch {
    ONE_BY_ONE("one-by-one", 1, "1 -> 1 -> 1 -> 1 (每次一台)"),

    TWO_BY_TWO("two-by-two", 2, "1 -> 2 -> 2 -> 2 (每次两台)"),

    THREE_BY_THREE("three-by-three", 3, "1 -> 3 -> 3 -> 3 (每次三台)");

    private String m_id;

    private int    m_batchSize;

    private String desc;

    private AgentBatch(String id, int batchSize, String description) {
        m_id = id;
        m_batchSize = batchSize;
        desc = description;
    }

    public int getBatchSize() {
        return m_batchSize;
    }

    public String getId() {
        return m_id;
    }

    public String getDesc() {
        return desc;
    }
}
