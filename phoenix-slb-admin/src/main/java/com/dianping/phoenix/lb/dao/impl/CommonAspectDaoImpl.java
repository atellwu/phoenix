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

import com.dianping.phoenix.lb.dao.CommonAspectDao;
import com.dianping.phoenix.lb.dao.ModelStore;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Aspect;

/**
 * @author Leo Liang
 * 
 */
@Service
public class CommonAspectDaoImpl extends AbstractDao implements CommonAspectDao {

    /**
     * @param store
     */
    @Autowired(required = true)
    public CommonAspectDaoImpl(ModelStore store) {
        super(store);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.phoenix.lb.dao.CommonAspectDao#list()
     */
    @Override
    public List<Aspect> list() {
        return store.listCommonAspects();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.phoenix.lb.dao.CommonAspectDao#find(java.lang.String)
     */
    @Override
    public Aspect find(String name) {
        return store.findCommonAspect(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.phoenix.lb.dao.CommonAspectDao#save(java.util.List)
     */
    @Override
    public void save(List<Aspect> aspects) throws BizException {
        store.saveCommonAspects(aspects);
    }

}
