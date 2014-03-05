/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-16
 * 
 */
package com.dianping.phoenix.lb.dao;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.SlbPool;

/**
 * @author Leo Liang
 * 
 */
public interface SlbPoolDao {

    List<SlbPool> list();

    SlbPool find(String poolName);

    void add(SlbPool pool) throws BizException;

    void delete(String poolName) throws BizException;

    void update(SlbPool pool) throws BizException;

}
