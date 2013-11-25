/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.configure.entity.Pool;

/**
 * @author Leo Liang
 * 
 */
public interface PoolService {
    List<Pool> listPools();

    Pool findPool(String poolName) throws BizException;

    void addPool(String poolName, Pool pool) throws BizException;

    void deletePool(String poolName) throws BizException;

    void modifyPool(String poolName, Pool pool) throws BizException;
}
