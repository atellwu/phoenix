package com.dianping.phoenix.lb.action;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("strategyAction")
public class StrategyAction extends ActionSupport {

    private static final long serialVersionUID = -6727172351979878969L;

    @Autowired
    private StrategyService   strategyService;

    private List<Strategy>    strategies;

    private String            poolName;

    @PostConstruct
    public void init() {
        strategies = strategyService.listStrategies();
    }

    public String listStrategies() {
        return SUCCESS;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

}
