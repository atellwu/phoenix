/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-16
 * 
 */
package com.dianping.phoenix.lb.dao;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Pool;

/**
 * @author Leo Liang
 * 
 */
public interface PoolDao {

    List<Pool> list();

    Pool find(String poolName);

    void add(Pool pool) throws BizException;

    void delete(String poolName) throws BizException;

    void update(Pool pool) throws BizException;

}
