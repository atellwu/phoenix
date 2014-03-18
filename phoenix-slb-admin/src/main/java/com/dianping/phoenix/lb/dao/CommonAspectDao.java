/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-16
 * 
 */
package com.dianping.phoenix.lb.dao;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Aspect;

/**
 * @author Leo Liang
 * 
 */
public interface CommonAspectDao {

    List<Aspect> list();

    Aspect find(String name);

    void save(List<Aspect> aspects) throws BizException;

}
