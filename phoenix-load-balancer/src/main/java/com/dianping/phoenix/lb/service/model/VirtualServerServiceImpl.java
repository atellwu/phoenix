/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.dao.VirtualServerDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.Availability;
import com.dianping.phoenix.lb.model.State;
import com.dianping.phoenix.lb.model.configure.entity.Configure;
import com.dianping.phoenix.lb.model.configure.entity.Directive;
import com.dianping.phoenix.lb.model.configure.entity.Location;
import com.dianping.phoenix.lb.model.configure.entity.Member;
import com.dianping.phoenix.lb.model.configure.entity.Pool;
import com.dianping.phoenix.lb.model.configure.entity.Strategy;
import com.dianping.phoenix.lb.model.configure.entity.VirtualServer;
import com.dianping.phoenix.lb.service.ConcurrentControlServiceTemplate;
import com.dianping.phoenix.lb.utils.ExceptionUtils;
import com.dianping.phoenix.lb.velocity.TemplateManager;
import com.dianping.phoenix.lb.velocity.VelocityEngineManager;
import com.dianping.phoenix.lb.visitor.NginxConfigVisitor;

/**
 * @author Leo Liang
 * 
 */
@Service
public class VirtualServerServiceImpl extends ConcurrentControlServiceTemplate implements VirtualServerService {

    private VirtualServerDao virtualServerDao;
    private StrategyDao      strategyDao;

    /**
     * @param virtualServerDao
     * @param templateDao
     */
    @Autowired(required = true)
    public VirtualServerServiceImpl(VirtualServerDao virtualServerDao, StrategyDao strategyDao)
            throws ComponentLookupException {
        super();
        this.virtualServerDao = virtualServerDao;
        this.strategyDao = strategyDao;
    }

    /**
     * @param strategyDao
     *            the strategyDao to set
     */
    public void setStrategyDao(StrategyDao strategyDao) {
        this.strategyDao = strategyDao;
    }

    /**
     * @param virtualServerDao
     *            the virtualServerDao to set
     */
    public void setVirtualServerDao(VirtualServerDao virtualServerDao) {
        this.virtualServerDao = virtualServerDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.VirtualServerService#listVirtualServers()
     */
    @Override
    public List<VirtualServer> listVirtualServers() {
        try {
            return read(new ReadOperation<List<VirtualServer>>() {

                @Override
                public List<VirtualServer> doRead() throws Exception {
                    return virtualServerDao.list();
                }
            });
        } catch (BizException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.VirtualServerService#findVirtualServer
     * (java.lang.String)
     */
    @Override
    public VirtualServer findVirtualServer(final String virtualServerName) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        return read(new ReadOperation<VirtualServer>() {

            @Override
            public VirtualServer doRead() throws BizException {
                return virtualServerDao.find(virtualServerName);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.VirtualServerService#addVirtualServer
     * (java.lang.String,
     * com.dianping.phoenix.lb.model.configure.entity.VirtualServer)
     */
    @Override
    public void addVirtualServer(String virtualServerName, final VirtualServer virtualServer) throws BizException {
        if (virtualServerName == null || virtualServer == null) {
            return;
        }

        if (!virtualServerName.equals(virtualServer.getName())) {
            return;
        }

        validate(virtualServer);

        write(new WriteOperation<Void>() {

            @Override
            public Void doWrite() throws Exception {
                virtualServerDao.add(virtualServer);
                return null;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.VirtualServerService#deleteVirtualServer
     * (java.lang.String)
     */
    @Override
    public void deleteVirtualServer(final String virtualServerName) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        try {
            write(new WriteOperation<Void>() {

                @Override
                public Void doWrite() throws Exception {
                    virtualServerDao.delete(virtualServerName);
                    return null;
                }
            });
        } catch (BizException e) {
            // ignore
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.service.VirtualServerService#modifyVirtualServer
     * (java.lang.String,
     * com.dianping.phoenix.lb.model.configure.entity.VirtualServer)
     */
    @Override
    public void modifyVirtualServer(final String virtualServerName, final VirtualServer virtualServer)
            throws BizException {
        if (virtualServerName == null || virtualServer == null) {
            return;
        }

        if (!virtualServerName.equals(virtualServer.getName())) {
            return;
        }

        validate(virtualServer);

        write(new WriteOperation<Void>() {

            @Override
            public Void doWrite() throws Exception {
                virtualServerDao.update(virtualServer);
                return null;
            }
        });

    }

    private void validate(VirtualServer virtualServer) throws BizException {
        boolean deafultPoolExist = false;
        for (Pool pool : virtualServer.getPools()) {
            if (pool.getName().equals(virtualServer.getDefaultPoolName())) {
                deafultPoolExist = true;
                break;
            }
        }

        if (StringUtils.isBlank(virtualServer.getName())) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (virtualServer.getPort() == null) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_PORT_EMPTY);
        }

        if (!deafultPoolExist) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_DEFAULTPOOL_NOT_EXISTS,
                    virtualServer.getDefaultPoolName());
        }

        List<Strategy> strategies = strategyDao.list();
        List<String> strategyNames = new ArrayList<String>();
        for (Strategy strategy : strategies) {
            strategyNames.add(strategy.getName());
        }

        for (Pool pool : virtualServer.getPools()) {
            if (!strategyNames.contains(pool.getLoadbalanceStrategyName())) {
                ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_STRATEGY_NOT_SUPPORT,
                        pool.getLoadbalanceStrategyName(), pool.getName());
            }

            if (pool.getMembers().size() == 0) {
                ExceptionUtils.throwBizException(MessageID.POOL_NO_MEMBER, pool.getName());
            }

            for (Member member : pool.getMembers()) {
                if (StringUtils.isBlank(member.getName())) {
                    ExceptionUtils.throwBizException(MessageID.POOL_MEMBER_NO_NAME);
                }
                if (StringUtils.isBlank(member.getIp())) {
                    ExceptionUtils.throwBizException(MessageID.POOL_MEMBER_NO_IP, member.getName());
                }
            }

            int availMemberCount = 0;
            for (Member member : pool.getMembers()) {
                if (member.getAvailability() == Availability.AVAILABLE && member.getState() == State.ENABLED) {
                    availMemberCount++;
                }
            }

            if (availMemberCount * 100.0d / pool.getMembers().size() < pool.getMinAvailableMemberPercentage()) {
                ExceptionUtils.throwBizException(MessageID.POOL_LOWER_THAN_MINAVAIL_PCT,
                        pool.getMinAvailableMemberPercentage(), pool.getName());
            }
        }

        for (Location location : virtualServer.getLocations()) {

            boolean proxyPassExists = false;

            if (StringUtils.isBlank(location.getPattern())) {
                ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_LOCATION_NO_PATTERN);
            }

            if (StringUtils.isBlank(location.getMatchType())) {
                ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_LOCATION_NO_MATCHTYPE);
            }

            if (location.getDirectives().size() == 0) {
                ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_LOCATION_NO_DIRECTIVE, location.getPattern());
            }

            for (Directive directive : location.getDirectives()) {
                if (!TemplateManager.INSTANCE.availableFiles("directive").contains(directive.getType())) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_DIRECTIVE_TYPE_NOT_SUPPORT,
                            directive.getType());
                }
                if ("proxy_pass".equals(directive.getType())) {
                    if (!proxyPassExists) {
                        proxyPassExists = true;
                    } else {
                        ExceptionUtils.throwBizException(MessageID.PROXY_PASS_MORE_THAN_ONE, location.getPattern());
                    }
                }
            }

        }
    }

    @Override
    public String generateNginxConfig(VirtualServer virtualServer) throws BizException {

        try {
            Configure tmpConfigure = new Configure();
            for (Strategy strategy : strategyDao.list()) {
                tmpConfigure.addStrategy(strategy);
            }

            tmpConfigure.addVirtualServer(virtualServer);

            NginxConfigVisitor visitor = new NginxConfigVisitor();
            tmpConfigure.accept(visitor);
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("config", visitor.getVisitorResult());
            return VelocityEngineManager.INSTANCE.merge(TemplateManager.INSTANCE.getTemplate("server", "default"),
                    context);
        } catch (Exception e) {
            ExceptionUtils.logAndRethrowBizException(e);
        }
        return "";
    }

    @Override
    public String tag(final String virtualServerName, final int virtualServerVersion) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        return read(new ReadOperation<String>() {

            @Override
            public String doRead() throws BizException {
                return virtualServerDao.tag(virtualServerName, virtualServerVersion);
            }

        });

    }

    @Override
    public VirtualServer findTagById(final String virtualServerName, final String tagId) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (StringUtils.isBlank(tagId)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAGID_EMPTY);
        }

        return read(new ReadOperation<VirtualServer>() {

            @Override
            public VirtualServer doRead() throws BizException {
                return virtualServerDao.findTagById(virtualServerName, tagId);
            }

        });
    }

    @Override
    public String findPrevTagId(final String virtualServerName, final String tagId) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (StringUtils.isBlank(tagId)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAGID_EMPTY);
        }

        return read(new ReadOperation<String>() {

            @Override
            public String doRead() throws BizException {
                return virtualServerDao.findPrevTagId(virtualServerName, tagId);
            }

        });
    }

    @Override
    public void removeTag(final String virtualServerName, final String tagId) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (StringUtils.isBlank(tagId)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAGID_EMPTY);
        }

        read(new ReadOperation<Void>() {

            @Override
            public Void doRead() throws BizException {
                virtualServerDao.removeTag(virtualServerName, tagId);
                return null;
            }

        });
    }

    @Override
    public String findLatestTagId(final String virtualServerName) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        return read(new ReadOperation<String>() {

            @Override
            public String doRead() throws BizException {
                return virtualServerDao.findLatestTagId(virtualServerName);
            }

        });
    }

    @Override
    public List<String> listTag(final String virtualServerName, final int maxNum) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        return read(new ReadOperation<List<String>>() {

            @Override
            public List<String> doRead() throws BizException {
                List<String> tags = virtualServerDao.listTags(virtualServerName);
                if (tags != null && tags.size() < maxNum) {
                    return tags;
                } else {
                    List<String> result = new ArrayList<String>(maxNum);
                    if (tags != null) {
                        for (int pos = 0; pos < tags.size() && pos < maxNum; pos++) {
                            result.add(tags.get(pos));
                        }
                    }
                    return result;
                }
            }

        });
    }
}
