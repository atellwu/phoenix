/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.PoolDao;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.Availability;
import com.dianping.phoenix.lb.model.State;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.service.ConcurrentControlServiceTemplate;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
@Service
public class PoolServiceImpl extends ConcurrentControlServiceTemplate implements PoolService {

    private PoolDao     poolDao;
    private StrategyDao strategyDao;

    /**
     * @param poolDao
     */
    @Autowired(required = true)
    public PoolServiceImpl(PoolDao poolDao, StrategyDao strategyDao) {
        super();
        this.poolDao = poolDao;
        this.strategyDao = strategyDao;
    }

    /**
     * @param poolDao
     *            the poolDao to set
     */
    public void setStrategyDao(PoolDao poolDao) {
        this.poolDao = poolDao;
    }

    @Override
    public List<Pool> listPools() {
        try {
            return read(new ReadOperation<List<Pool>>() {

                @Override
                public List<Pool> doRead() throws Exception {
                    return poolDao.list();
                }
            });
        } catch (BizException e) {
            // ignore
            return null;
        }
    }

    @Override
    public Pool findPool(final String poolName) throws BizException {
        if (StringUtils.isBlank(poolName)) {
            ExceptionUtils.throwBizException(MessageID.POOL_NAME_EMPTY);
        }

        return read(new ReadOperation<Pool>() {

            @Override
            public Pool doRead() throws BizException {
                return poolDao.find(poolName);
            }
        });
    }

    @Override
    public void addPool(String poolName, final Pool pool) throws BizException {
        if (poolName == null || pool == null) {
            return;
        }

        if (!poolName.equals(pool.getName())) {
            return;
        }

        validate(pool);

        write(new WriteOperation<Void>() {

            @Override
            public Void doWrite() throws Exception {
                poolDao.add(pool);
                return null;
            }
        });
    }

    @Override
    public void deletePool(final String poolName) throws BizException {
        if (StringUtils.isBlank(poolName)) {
            ExceptionUtils.throwBizException(MessageID.POOL_NAME_EMPTY);
        }

        try {
            write(new WriteOperation<Void>() {

                @Override
                public Void doWrite() throws Exception {
                    poolDao.delete(poolName);
                    return null;
                }
            });
        } catch (BizException e) {
            // ignore
        }
    }

    @Override
    public void modifyPool(final String poolName, final Pool pool) throws BizException {
        if (poolName == null || pool == null) {
            return;
        }

        if (!poolName.equals(pool.getName())) {
            return;
        }

        validate(pool);

        write(new WriteOperation<Void>() {

            @Override
            public Void doWrite() throws Exception {
                poolDao.update(pool);
                return null;
            }
        });

    }

    private void validate(Pool pool) throws BizException {
        if (StringUtils.isBlank(pool.getName())) {
            ExceptionUtils.throwBizException(MessageID.POOL_NAME_EMPTY);
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

        List<Strategy> strategies = strategyDao.list();
        List<String> strategyNames = new ArrayList<String>();
        for (Strategy strategy : strategies) {
            strategyNames.add(strategy.getName());
        }

        if (!strategyNames.contains(pool.getLoadbalanceStrategyName())) {
            ExceptionUtils.throwBizException(MessageID.POOL_STRATEGY_NOT_SUPPORT,
                    pool.getLoadbalanceStrategyName(), pool.getName());
        }

    }
}
