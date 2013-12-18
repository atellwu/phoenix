/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.constant.Constants;
import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.PoolDao;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.dao.VirtualServerDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Directive;
import com.dianping.phoenix.lb.model.entity.Location;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.service.ConcurrentControlServiceTemplate;
import com.dianping.phoenix.lb.service.GitService;
import com.dianping.phoenix.lb.service.NginxService;
import com.dianping.phoenix.lb.service.NginxService.NginxCheckResult;
import com.dianping.phoenix.lb.utils.ExceptionUtils;
import com.dianping.phoenix.lb.utils.PoolNameUtils;
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
    private PoolDao          poolDao;
    private NginxService     nginxService;
    private GitService       gitService;
    private ConfigManager    configManager;

    @Autowired(required = true)
    public VirtualServerServiceImpl(VirtualServerDao virtualServerDao, StrategyDao strategyDao, PoolDao poolDao,
            NginxService nginxService, GitService gitService) throws ComponentLookupException {
        super();
        this.virtualServerDao = virtualServerDao;
        this.strategyDao = strategyDao;
        this.poolDao = poolDao;
        this.gitService = gitService;
        this.nginxService = nginxService;
    }

    @PostConstruct
    public void init() throws ComponentLookupException, BizException {
        configManager = PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class);

        gitService.clone(configManager.getTengineConfigGitUrl(), configManager.getTengineConfigBaseDir(), null);
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

    public List<String> findVirtualServerByPool(String poolName) throws BizException {
        final String poolNamePrefix = PoolNameUtils.getPoolNamePrefix(poolName);
        try {
            return read(new ReadOperation<List<String>>() {

                @Override
                public List<String> doRead() throws Exception {
                    List<String> vsNames = new ArrayList<String>();
                    for (VirtualServer vs : virtualServerDao.list()) {

                        boolean found = false;

                        if (StringUtils.equals(vs.getDefaultPoolName(), poolNamePrefix)) {
                            vsNames.add(vs.getName());
                            found = true;
                        }

                        if (found) {
                            continue;
                        }

                        for (Location location : vs.getLocations()) {
                            if (found) {
                                break;
                            }
                            for (Directive directive : location.getDirectives()) {
                                if (Constants.DIRECTIVE_PROXY_PASS.equals(directive.getType())
                                        && StringUtils
                                                .equals(directive
                                                        .getDynamicAttribute(Constants.DIRECTIVE_PROXY_PASS_POOL_NAME),
                                                        poolNamePrefix)) {
                                    vsNames.add(vs.getName());
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    return vsNames;
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

        if (StringUtils.isBlank(virtualServer.getName())) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (virtualServer.getPort() == null) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_PORT_EMPTY);
        }

        if (StringUtils.isBlank(virtualServer.getDefaultPoolName())) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NO_DEFAULT_POOL_NAME);
        }

        if (poolDao.find(virtualServer.getDefaultPoolName()) == null) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_DEFAULTPOOL_NOT_EXISTS,
                    virtualServer.getDefaultPoolName());
        }

        for (Location location : virtualServer.getLocations()) {

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
            }

        }
    }

    @Override
    public String generateNginxConfig(VirtualServer virtualServer, List<Pool> pools) throws BizException {

        try {
            SlbModelTree tmpSlbModelTree = new SlbModelTree();
            for (Strategy strategy : strategyDao.list()) {
                tmpSlbModelTree.addStrategy(strategy);
            }

            if (pools == null) {
                for (Pool pool : poolDao.list()) {
                    tmpSlbModelTree.addPool(pool);
                }
            } else {
                for (Pool pool : pools) {
                    tmpSlbModelTree.addPool(pool);
                }
            }

            tmpSlbModelTree.addVirtualServer(virtualServer);

            NginxConfigVisitor visitor = new NginxConfigVisitor();
            tmpSlbModelTree.accept(visitor);
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
    public String tag(final String virtualServerName, final int virtualServerVersion, final List<Pool> pools)
            throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        return write(new WriteOperation<String>() {

            @Override
            public String doWrite() throws BizException {
                try {
                    VirtualServer virtualServer = virtualServerDao.find(virtualServerName);

                    if (virtualServer.getVersion() != virtualServerVersion) {
                        ExceptionUtils.logAndRethrowBizException(new ConcurrentModificationException(),
                                MessageID.VIRTUALSERVER_CONCURRENT_MOD, virtualServerName);
                    }

                    String nginxConfigContent = generateNginxConfig(virtualServer, pools);
                    NginxCheckResult nginxCheckResult = nginxService.checkConfig(nginxConfigContent);
                    if (!nginxCheckResult.isSucess()) {
                        ExceptionUtils.throwBizException(MessageID.NGINX_CHECK_EXCEPTION, nginxCheckResult.getMsg());
                    }

                    String tagId = virtualServerDao.tag(virtualServerName, virtualServerVersion, pools);
                    File serverConfFile = new File(
                            new File(configManager.getTengineConfigBaseDir(), virtualServerName),
                            configManager.getTengineConfigFileName());

                    try {
                        FileUtils.writeStringToFile(serverConfFile, nginxConfigContent);

                        gitService.commitAllChangesAndTagAndPush(configManager.getTengineConfigGitUrl(),
                                configManager.getTengineConfigBaseDir(), tagId,
                                String.format("update vs(%s) to tag(%s)", virtualServerName, tagId));
                    } catch (Exception e) {
                        virtualServerDao.removeTag(virtualServerName, tagId);
                        gitService.rollback(configManager.getTengineConfigBaseDir());
                        ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAG_FAIL, virtualServerName);
                    }
                    return tagId;
                } catch (Exception e) {
                    ExceptionUtils.rethrowBizException(e);
                }
                return null;
            }

        });

    }

    @Override
    public SlbModelTree findTagById(final String virtualServerName, final String tagId) throws BizException {
        if (StringUtils.isBlank(virtualServerName)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NAME_EMPTY);
        }

        if (StringUtils.isBlank(tagId)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAGID_EMPTY);
        }

        return read(new ReadOperation<SlbModelTree>() {

            @Override
            public SlbModelTree doRead() throws BizException {
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
