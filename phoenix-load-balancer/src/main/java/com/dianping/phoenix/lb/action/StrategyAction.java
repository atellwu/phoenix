package com.dianping.phoenix.lb.action;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.model.configure.entity.Strategy;
import com.dianping.phoenix.lb.service.StrategyService;
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

    @PostConstruct
    public void init() {
        strategies = strategyService.listStrategies();
    }

    public String strategies() {
        return SUCCESS;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

}
