/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.List;
import java.util.Map;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.VirtualServerGroup;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.VirtualServer;

/**
 * @author Leo Liang
 * 
 */
public interface VirtualServerService {
    List<VirtualServer> listVirtualServers();

    VirtualServer findVirtualServer(String virtualServerName) throws BizException;

    void addVirtualServer(String virtualServerName, VirtualServer virtualServer) throws BizException;

    void deleteVirtualServer(String virtualServerName) throws BizException;

    void modifyVirtualServer(String virtualServerName, VirtualServer virtualServer) throws BizException;

    String generateNginxConfig(VirtualServer virtualServer, List<Pool> pools, List<Aspect> commonAspects)
            throws BizException;

    String tag(String virtualServerName, int virtualServerVersion, List<Pool> pools, List<Aspect> commonAspects)
            throws BizException;

    SlbModelTree findTagById(String virtualServerName, String tagId) throws BizException;

    String findPrevTagId(String virtualServerName, String tagId) throws BizException;

    void removeTag(String virtualServerName, String tagId) throws BizException;

    String findLatestTagId(String virtualServerName) throws BizException;

    List<String> listTag(String virtualServerName, int maxNum) throws BizException;

    List<String> findVirtualServerByPool(String poolName) throws BizException;

    Map<String, VirtualServerGroup> listGroups();
}
