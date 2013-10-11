package com.dianping.phoenix.softbalance.model;

public class Pool {

    private String lbStrategy;

    private int    minAvailMemberPct;

    public String getLbStrategy() {
        return lbStrategy;
    }

    public void setLbStrategy(String lbStrategy) {
        this.lbStrategy = lbStrategy;
    }

    public int getMinAvailMemberPct() {
        return minAvailMemberPct;
    }

    public void setMinAvailMemberPct(int minAvailMemberPct) {
        this.minAvailMemberPct = minAvailMemberPct;
    }

}
