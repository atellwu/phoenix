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
import com.dianping.phoenix.lb.dao.PoolDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Pool;

/**
 * @author Leo Liang
 * 
 */
@Service
public class PoolDaoImpl extends AbstractDao implements PoolDao {

    /**
     * @param store
     */
    @Autowired(required = true)
    public PoolDaoImpl(ModelStore store) {
        super(store);
    }

    @Override
    public List<Pool> list() {
        return store.listPools();
    }

    @Override
    public Pool find(String poolName) {
        return store.findPool(poolName);
    }

    @Override
    public void add(Pool pool) throws BizException {
        store.updateOrCreatePool(pool.getName(), pool);
    }

    @Override
    public void delete(String poolName) throws BizException {
        store.removePool(poolName);
    }

    @Override
    public void update(Pool pool) throws BizException {
        store.updateOrCreatePool(pool.getName(), pool);
    }

}
