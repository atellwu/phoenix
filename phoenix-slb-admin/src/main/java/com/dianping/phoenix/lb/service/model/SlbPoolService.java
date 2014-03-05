/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.SlbPool;

/**
 * @author Leo Liang
 * 
 */
public interface SlbPoolService {
    List<SlbPool> listSlbPools();

    SlbPool findSlbPool(String poolName) throws BizException;

    void addSlbPool(String poolName, SlbPool pool) throws BizException;

    void deleteSlbPool(String poolName) throws BizException;

    void modifySlbPool(String poolName, SlbPool pool) throws BizException;
}
