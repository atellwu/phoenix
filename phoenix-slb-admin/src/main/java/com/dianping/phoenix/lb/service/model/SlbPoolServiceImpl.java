/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-17
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.SlbPoolDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Instance;
import com.dianping.phoenix.lb.model.entity.SlbPool;
import com.dianping.phoenix.lb.service.ConcurrentControlServiceTemplate;
import com.dianping.phoenix.lb.utils.ExceptionUtils;

/**
 * @author Leo Liang
 * 
 */
@Service
public class SlbPoolServiceImpl extends ConcurrentControlServiceTemplate implements SlbPoolService {

    private SlbPoolDao slbPoolDao;

    /**
     * @param poolDao
     */
    @Autowired(required = true)
    public SlbPoolServiceImpl(SlbPoolDao slbPoolDao) {
        super();
        this.slbPoolDao = slbPoolDao;
    }

    @Override
    public List<SlbPool> listSlbPools() {
        try {
            return read(new ReadOperation<List<SlbPool>>() {

                @Override
                public List<SlbPool> doRead() throws Exception {
                    return slbPoolDao.list();
                }
            });
        } catch (BizException e) {
            // ignore
            return null;
        }
    }

    @Override
    public SlbPool findSlbPool(final String poolName) throws BizException {
        if (StringUtils.isBlank(poolName)) {
            ExceptionUtils.throwBizException(MessageID.SLBPOOL_NAME_EMPTY);
        }

        return read(new ReadOperation<SlbPool>() {

            @Override
            public SlbPool doRead() throws BizException {
                return slbPoolDao.find(poolName);
            }
        });
    }

    @Override
    public void addSlbPool(String poolName, final SlbPool pool) throws BizException {
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
                slbPoolDao.add(pool);
                return null;
            }
        });
    }

    @Override
    public void deleteSlbPool(final String poolName) throws BizException {
        if (StringUtils.isBlank(poolName)) {
            ExceptionUtils.throwBizException(MessageID.SLBPOOL_NAME_EMPTY);
        }

        try {
            write(new WriteOperation<Void>() {

                @Override
                public Void doWrite() throws Exception {
                    slbPoolDao.delete(poolName);
                    return null;
                }
            });
        } catch (BizException e) {
            // ignore
        }
    }

    @Override
    public void modifySlbPool(final String poolName, final SlbPool pool) throws BizException {
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
                slbPoolDao.update(pool);
                return null;
            }
        });

    }

    private void validate(SlbPool pool) throws BizException {
        if (StringUtils.isBlank(pool.getName())) {
            ExceptionUtils.throwBizException(MessageID.SLBPOOL_NAME_EMPTY);
        }

        if (pool.getInstances().size() == 0) {
            ExceptionUtils.throwBizException(MessageID.SLBPOOL_NO_MEMBER, pool.getName());
        }

        for (Instance member : pool.getInstances()) {
            if (StringUtils.isBlank(member.getIp())) {
                ExceptionUtils.throwBizException(MessageID.SLBPOOL_MEMBER_NO_IP, pool.getName());
            }
        }

    }
}
