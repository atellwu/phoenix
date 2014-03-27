/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 1, 2013
 * 
 */
package com.dianping.phoenix.lb.service.model;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.phoenix.lb.dao.CommonAspectDao;
import com.dianping.phoenix.lb.dao.PoolDao;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.dao.VirtualServerDao;
import com.dianping.phoenix.lb.dao.impl.CommonAspectDaoImpl;
import com.dianping.phoenix.lb.dao.impl.LocalFileModelStoreImpl;
import com.dianping.phoenix.lb.dao.impl.PoolDaoImpl;
import com.dianping.phoenix.lb.dao.impl.StrategyDaoImpl;
import com.dianping.phoenix.lb.dao.impl.VirtualServerDaoImpl;
import com.dianping.phoenix.lb.service.DefaultGitServiceImpl;
import com.dianping.phoenix.lb.service.DefaultNginxServiceImpl;

/**
 * @author Leo Liang
 * 
 */
public class VirtualServerServiceImplTest {
    private LocalFileModelStoreImpl store;
    private File                    baseDir;
    private File                    tmpDir;
    private VirtualServerService    virtualServerService;
    private VirtualServerDao        virtualServerDao;
    private StrategyDao             strategyDao;
    private CommonAspectDao         commonAspectDao;
    private PoolDao                 poolDao;
    private DefaultNginxServiceImpl nginxService;
    private DefaultGitServiceImpl   gitService;

    @Before
    public void before() throws Exception {
        baseDir = new File(".", "src/test/resources/virtualServerServiceTest");
        tmpDir = new File(System.getProperty("java.io.tmpdir"), "virtualServerServiceTest");
        if (tmpDir.exists()) {
            FileUtils.forceDelete(tmpDir);
        }
        FileUtils.copyDirectory(baseDir, tmpDir);
        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        virtualServerDao = new VirtualServerDaoImpl(store);

        strategyDao = new StrategyDaoImpl(store);
        poolDao = new PoolDaoImpl(store);
        commonAspectDao = new CommonAspectDaoImpl(store);

        gitService = new DefaultGitServiceImpl();
        gitService.init();
        nginxService = new DefaultNginxServiceImpl();
        nginxService.init();

        virtualServerService = new VirtualServerServiceImpl(virtualServerDao, strategyDao, poolDao, commonAspectDao,
                nginxService, gitService);
    }

    @After
    public void after() throws Exception {
        if (tmpDir.exists()) {
            tmpDir.setWritable(true);
            FileUtils.forceDelete(tmpDir);
        }
    }

    @Test
    public void testNginxConf() throws Exception {
        System.out.println(virtualServerService.generateNginxConfig(virtualServerDao.find("www"), null, null));
    }
}
