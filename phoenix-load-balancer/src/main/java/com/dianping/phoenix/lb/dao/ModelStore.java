/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-21
 * 
 */
package com.dianping.phoenix.lb.dao;

import java.util.List;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.entity.VirtualServer;

/**
 * @author Leo Liang
 * 
 */
public interface ModelStore {

    public void init();

    public List<VirtualServer> listVirtualServers();

    public List<Strategy> listStrategies();

    public List<Pool> listPools();

    public List<Aspect> listCommonAspects();

    public Strategy findStrategy(String name);

    public VirtualServer findVirtualServer(String name);

    public Pool findPool(String name);

    public Aspect findCommonAspect(String name);

    public void updateOrCreateStrategy(String name, Strategy strategy) throws BizException;

    public void removeStrategy(String name) throws BizException;

    public void updateOrCreatePool(String name, Pool pool) throws BizException;

    public void removePool(String name) throws BizException;

    public void saveCommonAspects(List<Aspect> aspects) throws BizException;

    public void updateVirtualServer(String name, VirtualServer virtualServer) throws BizException;

    public void removeVirtualServer(String name) throws BizException;

    public void addVirtualServer(String name, VirtualServer virtualServer) throws BizException;

    public String tag(String name, int version, List<Pool> pools, List<Aspect> aspects) throws BizException;

    public SlbModelTree getTag(String name, String tagId) throws BizException;

    public List<String> listTagIds(String name) throws BizException;

    public String findPrevTagId(String virtualServerName, String currentTagId) throws BizException;

    public void removeTag(String virtualServerName, String tagId) throws BizException;

    public String findLatestTagId(String virtualServerName) throws BizException;

}