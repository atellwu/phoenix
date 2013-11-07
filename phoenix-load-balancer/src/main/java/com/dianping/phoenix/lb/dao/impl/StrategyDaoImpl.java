/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-18
 * 
 */
package com.dianping.phoenix.lb.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.dao.ModelStore;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.model.configure.entity.Strategy;

/**
 * @author Leo Liang
 * 
 */
@Service
public class StrategyDaoImpl extends AbstractDao implements StrategyDao {

    /**
     * @param store
     */
    @Autowired(required = true)
    public StrategyDaoImpl(ModelStore store) {
        super(store);
    }

    @Override
    public List<Strategy> list() {
        return store.listStrategies();
    }

}
