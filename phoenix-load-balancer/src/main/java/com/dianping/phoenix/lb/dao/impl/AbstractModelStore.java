/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-21
 * 
 */
package com.dianping.phoenix.lb.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xml.sax.SAXException;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.ModelStore;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
public abstract class AbstractModelStore implements ModelStore {

    protected SlbModelTree                      slbModelTree                   = new SlbModelTree();
    protected ConfigMeta                        baseConfigMeta;
    protected ConcurrentMap<String, ConfigMeta> virtualServerConfigFileMapping = new ConcurrentHashMap<String, ConfigMeta>();

    protected static class ConfigMeta {
        protected String                 key;
        protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        protected SlbModelTree           slbModelTree;

        public ConfigMeta(String key, SlbModelTree slbModelTree) {
            this.key = key;
            this.slbModelTree = slbModelTree;
        }

    }

    public void init() {
        initConfigMetas();
        initCustomizedMetas();
    }

    protected abstract void initCustomizedMetas();

    protected abstract void initConfigMetas();

    public List<VirtualServer> listVirtualServers() {
        // ignore concurrent issue, since it will introduce unnecessary
        // complexity
        return new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values());
    }

    public List<Strategy> listStrategies() {
        baseConfigMeta.lock.readLock().lock();
        try {
            return new ArrayList<Strategy>(slbModelTree.getStrategies().values());
        } finally {
            baseConfigMeta.lock.readLock().unlock();
        }
    }

    public Strategy findStrategy(String name) {
        baseConfigMeta.lock.readLock().lock();
        try {
            return slbModelTree.findStrategy(name);
        } finally {
            baseConfigMeta.lock.readLock().unlock();
        }
    }

    public VirtualServer findVirtualServer(String name) {
        // ignore concurrent issue, since it will introduce unnecessary
        // complexity
        return slbModelTree.findVirtualServer(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.dao.impl.ModelStore#updateOrCreateStrategy(java
     * .lang.String, com.dianping.phoenix.lb.model.slbModelTree.entity.Strategy)
     */
    @Override
    public void updateOrCreateStrategy(String name, Strategy strategy) throws BizException {
        Strategy originalStrategy = null;
        baseConfigMeta.lock.writeLock().lock();
        try {
            Date now = new Date();

            originalStrategy = baseConfigMeta.slbModelTree.findStrategy(name);
            strategy.setLastModifiedDate(now);

            if (baseConfigMeta.slbModelTree.findStrategy(name) == null) {
                strategy.setCreationDate(now);
            } else {
                strategy.setCreationDate(originalStrategy.getCreationDate());
            }
            baseConfigMeta.slbModelTree.addStrategy(strategy);
            slbModelTree.addStrategy(strategy);
            save(baseConfigMeta.key, baseConfigMeta.slbModelTree);

        } catch (Exception e) {
            if (originalStrategy == null) {
                baseConfigMeta.slbModelTree.removeStrategy(name);
                slbModelTree.removeStrategy(name);
            } else {
                baseConfigMeta.slbModelTree.addStrategy(originalStrategy);
                slbModelTree.addStrategy(originalStrategy);
            }
            ExceptionUtils.logAndRethrowBizException(e, MessageID.STRATEGY_SAVE_FAIL, name);
        } finally {
            baseConfigMeta.lock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.dao.impl.ModelStore#removeStrategy(java.lang.
     * String)
     */
    @Override
    public void removeStrategy(String name) throws BizException {
        Strategy originalStrategy = null;
        baseConfigMeta.lock.writeLock().lock();
        try {
            originalStrategy = baseConfigMeta.slbModelTree.findStrategy(name);

            if (originalStrategy == null) {
                return;
            }

            baseConfigMeta.slbModelTree.removeStrategy(name);
            slbModelTree.removeStrategy(name);
            save(baseConfigMeta.key, baseConfigMeta.slbModelTree);

        } catch (Exception e) {
            baseConfigMeta.slbModelTree.addStrategy(originalStrategy);
            slbModelTree.addStrategy(originalStrategy);
            ExceptionUtils.logAndRethrowBizException(e, MessageID.STRATEGY_SAVE_FAIL, name);
        } finally {
            baseConfigMeta.lock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.dao.impl.ModelStore#updateVirtualServer(java.
     * lang.String,
     * com.dianping.phoenix.lb.model.slbModelTree.entity.VirtualServer)
     */
    @Override
    public void updateVirtualServer(String name, VirtualServer virtualServer) throws BizException {
        VirtualServer originalVirtualServer = null;
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(name);
        if (configFileEntry != null) {
            configFileEntry.lock.writeLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(name) == null
                        || slbModelTree.findVirtualServer(name) == null) {
                    return;
                }

                originalVirtualServer = configFileEntry.slbModelTree.findVirtualServer(name);

                if (originalVirtualServer.getVersion() != virtualServer.getVersion()) {
                    ExceptionUtils.logAndRethrowBizException(new ConcurrentModificationException(),
                            MessageID.VIRTUALSERVER_CONCURRENT_MOD, name);
                } else {
                    virtualServer.setVersion(originalVirtualServer.getVersion() + 1);
                    virtualServer.setLastModifiedDate(new Date());
                    virtualServer.setCreationDate(originalVirtualServer.getCreationDate());
                }

                configFileEntry.slbModelTree.addVirtualServer(virtualServer);
                slbModelTree.addVirtualServer(virtualServer);
                save(configFileEntry.key, configFileEntry.slbModelTree);
            } catch (Exception e) {
                configFileEntry.slbModelTree.addVirtualServer(originalVirtualServer);
                slbModelTree.addVirtualServer(originalVirtualServer);
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_SAVE_FAIL, name);
            } finally {
                configFileEntry.lock.writeLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.dao.impl.ModelStore#removeVirtualServer(java.
     * lang.String)
     */
    @Override
    public void removeVirtualServer(String name) throws BizException {
        VirtualServer originalVirtualServer = null;
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(name);
        if (configFileEntry != null) {
            configFileEntry.lock.writeLock().lock();
            try {
                if (configFileEntry.slbModelTree.findVirtualServer(name) == null
                        || slbModelTree.findVirtualServer(name) == null) {
                    return;
                }

                originalVirtualServer = configFileEntry.slbModelTree.findVirtualServer(name);

                if (originalVirtualServer == null) {
                    return;
                }

                configFileEntry.slbModelTree.removeVirtualServer(name);
                slbModelTree.removeVirtualServer(name);

                if (configFileEntry.slbModelTree.getVirtualServers().size() == 0) {
                    if (!delete(configFileEntry.key)) {
                        throw new IOException();
                    } else {
                        virtualServerConfigFileMapping.remove(name);
                    }
                } else {
                    save(configFileEntry.key, configFileEntry.slbModelTree);
                }
            } catch (Exception e) {
                configFileEntry.slbModelTree.addVirtualServer(originalVirtualServer);
                slbModelTree.addVirtualServer(originalVirtualServer);
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_SAVE_FAIL, name);
            } finally {
                configFileEntry.lock.writeLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.lb.dao.impl.ModelStore#addVirtualServer(java.lang
     * .String, com.dianping.phoenix.lb.model.slbModelTree.entity.VirtualServer)
     */
    @Override
    public void addVirtualServer(String name, VirtualServer virtualServer) throws BizException {
        if (virtualServerConfigFileMapping.containsKey(name)) {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_ALREADY_EXISTS, name);
        }

        Date now = new Date();

        virtualServer.setVersion(1);
        virtualServer.setCreationDate(now);
        virtualServer.setLastModifiedDate(now);

        SlbModelTree newSlbModelTree = new SlbModelTree();
        newSlbModelTree.addVirtualServer(virtualServer);

        ConfigMeta originalMeta = virtualServerConfigFileMapping.putIfAbsent(name, new ConfigMeta(convertToKey(name),
                newSlbModelTree));

        if (originalMeta != null) {
            return;
        } else {
            ConfigMeta configMeta = virtualServerConfigFileMapping.get(name);
            configMeta.lock.writeLock().lock();
            try {
                if (slbModelTree.findVirtualServer(name) != null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_ALREADY_EXISTS, name);
                } else {
                    slbModelTree.addVirtualServer(virtualServer);
                    save(configMeta.key, newSlbModelTree);
                }

            } catch (Exception e) {
                slbModelTree.removeVirtualServer(name);
                virtualServerConfigFileMapping.remove(name);
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_SAVE_FAIL, name);
            } finally {
                configMeta.lock.writeLock().unlock();
            }
        }
    }

    @Override
    public String tag(String name, int version, List<Pool> pools) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(name);
        if (configFileEntry != null) {
            configFileEntry.lock.writeLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(name) == null
                        || slbModelTree.findVirtualServer(name) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
                }

                int currentVersion = configFileEntry.slbModelTree.findVirtualServer(name).getVersion();

                if (currentVersion != version) {
                    ExceptionUtils.logAndRethrowBizException(new ConcurrentModificationException(),
                            MessageID.VIRTUALSERVER_CONCURRENT_MOD, name);
                }

                SlbModelTree tagSlbModelTree = new SlbModelTree();
                for (Pool pool : pools) {
                    tagSlbModelTree.addPool(pool);
                }
                tagSlbModelTree.addVirtualServer(configFileEntry.slbModelTree.findVirtualServer(name));

                return saveTag(configFileEntry.key, name, tagSlbModelTree);
            } catch (Exception e) {
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_TAG_FAIL, name);
            } finally {
                configFileEntry.lock.writeLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
        }

        return null;
    }

    @Override
    public SlbModelTree getTag(String name, String tagId) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(name);
        if (configFileEntry != null) {
            configFileEntry.lock.readLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(name) == null
                        || slbModelTree.findVirtualServer(name) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
                }

                SlbModelTree tagSlbModelTree = loadTag(configFileEntry.key, name, tagId);
                if (tagSlbModelTree != null && tagSlbModelTree.findVirtualServer(name) != null) {
                    return tagSlbModelTree;
                } else {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_TAG_NOT_FOUND, tagId, name);
                }
            } catch (Exception e) {
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_TAG_LOAD_FAIL, name, tagId);
            } finally {
                configFileEntry.lock.readLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
        }

        return null;
    }

    @Override
    public List<String> listTagIds(String name) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(name);
        if (configFileEntry != null) {
            configFileEntry.lock.readLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(name) == null
                        || slbModelTree.findVirtualServer(name) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
                }

                return doListTagIds(name);

            } catch (Exception e) {
                ExceptionUtils.logAndRethrowBizException(e, MessageID.VIRTUALSERVER_TAG_LIST_FAIL, name);
            } finally {
                configFileEntry.lock.readLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, name);
        }

        return null;
    }

    @Override
    public String findPrevTagId(String virtualServerName, String currentTagId) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(virtualServerName);
        if (configFileEntry != null) {
            configFileEntry.lock.readLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(virtualServerName) == null
                        || slbModelTree.findVirtualServer(virtualServerName) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
                }

                List<String> tagIds = listTagIds(virtualServerName);
                if (tagIds != null && !tagIds.isEmpty()) {
                    return doFindPrevTagId(virtualServerName, currentTagId, tagIds);
                }

            } finally {
                configFileEntry.lock.readLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
        }

        return null;

    }

    @Override
    public void removeTag(String virtualServerName, String tagId) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(virtualServerName);
        if (configFileEntry != null) {
            configFileEntry.lock.readLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(virtualServerName) == null
                        || slbModelTree.findVirtualServer(virtualServerName) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
                }

                List<String> tagIds = listTagIds(virtualServerName);
                if (tagIds != null && tagIds.contains(tagId)) {
                    doRemoveTag(virtualServerName, tagId);
                }

            } finally {
                configFileEntry.lock.readLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
        }

    }

    @Override
    public String findLatestTagId(String virtualServerName) throws BizException {
        ConfigMeta configFileEntry = virtualServerConfigFileMapping.get(virtualServerName);
        if (configFileEntry != null) {
            configFileEntry.lock.readLock().lock();

            try {
                if (configFileEntry.slbModelTree.findVirtualServer(virtualServerName) == null
                        || slbModelTree.findVirtualServer(virtualServerName) == null) {
                    ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
                }

                List<String> tagIds = listTagIds(virtualServerName);
                if (tagIds != null && !tagIds.isEmpty()) {
                    return doFindLatestTagId(virtualServerName, tagIds);
                }

            } finally {
                configFileEntry.lock.readLock().unlock();
            }
        } else {
            ExceptionUtils.throwBizException(MessageID.VIRTUALSERVER_NOT_EXISTS, virtualServerName);
        }

        return null;
    }

    @Override
    public List<Pool> listPools() {
        baseConfigMeta.lock.readLock().lock();
        try {
            return new ArrayList<Pool>(slbModelTree.getPools().values());
        } finally {
            baseConfigMeta.lock.readLock().unlock();
        }
    }

    @Override
    public Pool findPool(String name) {
        baseConfigMeta.lock.readLock().lock();
        try {
            return slbModelTree.findPool(name);
        } finally {
            baseConfigMeta.lock.readLock().unlock();
        }
    }

    @Override
    public void updateOrCreatePool(String name, Pool pool) throws BizException {
        Pool originalPool = null;
        baseConfigMeta.lock.writeLock().lock();
        try {
            Date now = new Date();

            originalPool = baseConfigMeta.slbModelTree.findPool(name);
            pool.setLastModifiedDate(now);

            if (baseConfigMeta.slbModelTree.findPool(name) == null) {
                pool.setCreationDate(now);
            } else {
                pool.setCreationDate(originalPool.getCreationDate());
            }
            baseConfigMeta.slbModelTree.addPool(pool);
            slbModelTree.addPool(pool);
            save(baseConfigMeta.key, baseConfigMeta.slbModelTree);

        } catch (Exception e) {
            if (originalPool == null) {
                baseConfigMeta.slbModelTree.removePool(name);
                slbModelTree.removePool(name);
            } else {
                baseConfigMeta.slbModelTree.addPool(originalPool);
                slbModelTree.addPool(originalPool);
            }
            ExceptionUtils.logAndRethrowBizException(e, MessageID.POOL_SAVE_FAIL, name);
        } finally {
            baseConfigMeta.lock.writeLock().unlock();
        }
    }

    @Override
    public void removePool(String name) throws BizException {
        Pool originalPool = null;
        baseConfigMeta.lock.writeLock().lock();
        try {
            originalPool = baseConfigMeta.slbModelTree.findPool(name);

            if (originalPool == null) {
                return;
            }

            baseConfigMeta.slbModelTree.removePool(name);
            slbModelTree.removePool(name);
            save(baseConfigMeta.key, baseConfigMeta.slbModelTree);

        } catch (Exception e) {
            baseConfigMeta.slbModelTree.addPool(originalPool);
            slbModelTree.addPool(originalPool);
            ExceptionUtils.logAndRethrowBizException(e, MessageID.POOL_SAVE_FAIL, name);
        } finally {
            baseConfigMeta.lock.writeLock().unlock();
        }
    }

    protected abstract String doFindLatestTagId(String virtualServerName, List<String> tagIds);

    protected abstract void doRemoveTag(String virtualServerName, String tagId) throws BizException;

    protected abstract String doFindPrevTagId(String virtualServerName, String currentTagId, List<String> tagIds);

    protected abstract List<String> doListTagIds(String vsName) throws IOException;

    protected abstract SlbModelTree loadTag(String key, String vsName, String tagId) throws IOException, SAXException;

    protected abstract String saveTag(String key, String vsName, SlbModelTree slbModelTree) throws IOException, BizException;

    protected abstract void save(String key, SlbModelTree slbModelTree) throws IOException, BizException;

    protected abstract boolean delete(String key) throws BizException;

    protected abstract String convertToKey(String virtualServerName);
}