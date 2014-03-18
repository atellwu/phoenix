/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-21
 * 
 */
package com.dianping.phoenix.lb.dao.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.Availability;
import com.dianping.phoenix.lb.model.PointCut;
import com.dianping.phoenix.lb.model.State;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.model.entity.Directive;
import com.dianping.phoenix.lb.model.entity.Instance;
import com.dianping.phoenix.lb.model.entity.Location;
import com.dianping.phoenix.lb.model.entity.Member;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.SlbPool;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.model.transform.DefaultMerger;
import com.dianping.phoenix.lb.model.transform.DefaultSaxParser;

/**
 * @author Leo Liang
 * 
 */
public class LocalFileModelStoreTest {
    private LocalFileModelStoreImpl store;
    private File                    baseDir;
    private File                    tmpDir;

    @Before
    public void before() throws Exception {
        baseDir = new File(".", "src/test/resources/storeTest");
        tmpDir = new File(System.getProperty("java.io.tmpdir"), "model-test");
        if (tmpDir.exists()) {
            FileUtils.forceDelete(tmpDir);
        }
        FileUtils.copyDirectory(baseDir, tmpDir);
        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();
    }

    @After
    public void after() throws Exception {
        if (tmpDir.exists()) {
            tmpDir.setWritable(true);
            FileUtils.forceDelete(tmpDir);
        }
    }

    @Test
    public void testListVirtualServers() throws Exception {
        SlbModelTree wwwSlbModelTree = DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir,
                "slb_www.xml")));

        SlbModelTree tuangouSlbModelTree = DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir,
                "slb_tuangou.xml")));

        List<VirtualServer> expected = new ArrayList<VirtualServer>(tuangouSlbModelTree.getVirtualServers().values()
                .size()
                + wwwSlbModelTree.getVirtualServers().values().size());
        expected.addAll(tuangouSlbModelTree.getVirtualServers().values());
        expected.addAll(wwwSlbModelTree.getVirtualServers().values());

        List<VirtualServer> actual = store.listVirtualServers();

        assertEquals(expected, actual);
    }

    @Test
    public void testListStrategies() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());
    }

    @Test
    public void testListSlbPools() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());
    }

    @Test
    public void testListCommonAspectss() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(slbModelTree.getAspects(), store.listCommonAspects());
    }

    @Test
    public void testListPools() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<Pool>(slbModelTree.getPools().values()), store.listPools());
    }

    @Test
    public void testFindStrategy() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        Strategy expected = slbModelTree.findStrategy("uri-hash");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findStrategy("uri-hash"), true));
    }

    @Test
    public void testFindSlbPool() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        SlbPool expected = slbModelTree.findSlbPool("test-pool");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findSlbPool("test-pool"), true));
        expected = slbModelTree.findSlbPool("test-pool2");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findSlbPool("test-pool2"), true));
    }

    @Test
    public void testFindPool() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        Pool expected = slbModelTree.findPool("Web.Tuangou");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findPool("Web.Tuangou"), true));
    }

    @Test
    public void testFindCommonAspect() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        Aspect expected = null;
        for (Aspect aspect : slbModelTree.getAspects()) {
            if ("commonRequest".equalsIgnoreCase(aspect.getName())) {
                expected = aspect;
                break;
            }
        }

        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findCommonAspect("commonRequest"), true));
    }

    @Test
    public void testFindVirtualServer() throws Exception {
        SlbModelTree slbModelTree = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        VirtualServer expected = slbModelTree.findVirtualServer("www");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, store.findVirtualServer("www"), true));
    }

    @Test
    public void testAddStrategy() throws Exception {
        Strategy newStrategy = new Strategy("dper-hash");
        newStrategy.setType("hash");
        newStrategy.setDynamicAttribute("target", "dper");
        newStrategy.setDynamicAttribute("method", "crc32");
        store.updateOrCreateStrategy("dper-hash", newStrategy);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        slbModelTree.addStrategy(newStrategy);

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());
        Assert.assertNotNull(newStrategy.getCreationDate());
        Assert.assertEquals(newStrategy.getLastModifiedDate(), newStrategy.getCreationDate());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testAddPool() throws Exception {
        Pool pool = new Pool("TestPool");
        pool.setMinAvailableMemberPercentage(40);
        pool.setLoadbalanceStrategyName("uri-hash");
        Member member1 = new Member("test01");
        member1.setIp("10.1.1.1");
        pool.addMember(member1);
        Member member2 = new Member("test02");
        member2.setIp("10.1.1.2");
        pool.addMember(member2);
        store.updateOrCreatePool("TestPool", pool);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        slbModelTree.addPool(pool);

        assertEquals(new ArrayList<Pool>(slbModelTree.getPools().values()), store.listPools());
        Assert.assertNotNull(pool.getCreationDate());
        Assert.assertEquals(pool.getLastModifiedDate(), pool.getCreationDate());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testAddSlbPool() throws Exception {
        SlbPool slbPool = new SlbPool("ut-pool");

        Instance instance = new Instance();
        instance.setIp("1.1.1.1");
        slbPool.addInstance(instance);
        store.updateOrCreateSlbPool("ut-pool", slbPool);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        slbModelTree.addSlbPool(slbPool);

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testSaveAspects() throws Exception {
        List<Aspect> aspects = new ArrayList<Aspect>();
        Aspect aspect1 = new Aspect();
        aspect1.setName("t1");
        aspect1.setPointCut(PointCut.BEFORE);
        Directive d1 = new Directive();
        d1.setType("t1");
        aspect1.addDirective(d1);
        aspects.add(aspect1);
        Aspect aspect2 = new Aspect();
        aspect2.setName("t2");
        aspect2.setPointCut(PointCut.AFTER);
        Directive d2 = new Directive();
        d2.setType("t2");
        aspect2.addDirective(d2);
        aspects.add(aspect2);

        store.saveCommonAspects(aspects);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        slbModelTree.getAspects().clear();
        slbModelTree.getAspects().addAll(aspects);
        assertEquals(slbModelTree.getAspects(), store.listCommonAspects());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testUpdateStrategy() throws Exception {
        Strategy modifiedStrategy = new Strategy("uri-hash");
        modifiedStrategy.setType("hash");
        modifiedStrategy.setDynamicAttribute("target", "$request_uri");
        modifiedStrategy.setDynamicAttribute("method", "md5");
        Date now = new Date();
        store.updateOrCreateStrategy("uri-hash", modifiedStrategy);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        Strategy expectedStrategy = slbModelTree.findStrategy("uri-hash");
        expectedStrategy.setDynamicAttribute("method", "md5");
        expectedStrategy.setLastModifiedDate(now);

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());
        Assert.assertEquals(now, modifiedStrategy.getLastModifiedDate());
        Assert.assertEquals(expectedStrategy.getCreationDate(), modifiedStrategy.getCreationDate());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testUpdatePool() throws Exception {
        Pool modifiedPool = new Pool("Web.Tuangou");
        modifiedPool.setMinAvailableMemberPercentage(10);
        modifiedPool.setLoadbalanceStrategyName("roundrobin");
        Member member = new Member("t1");
        member.setIp("12.12.12.12");
        modifiedPool.addMember(member);

        Date now = new Date();
        store.updateOrCreatePool("Web.Tuangou", modifiedPool);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        Pool expectedPool = slbModelTree.findPool("Web.Tuangou");
        expectedPool.setMinAvailableMemberPercentage(10);
        expectedPool.setLoadbalanceStrategyName("roundrobin");
        Member member2 = new Member("t1");
        member2.setIp("12.12.12.12");
        expectedPool.removeMember("tuangou-web01");
        expectedPool.removeMember("tuangou-web02");
        expectedPool.addMember(member2);
        expectedPool.setLastModifiedDate(now);

        assertEquals(new ArrayList<Pool>(slbModelTree.getPools().values()), store.listPools());
        Assert.assertEquals(now, modifiedPool.getLastModifiedDate());
        Assert.assertEquals(expectedPool.getCreationDate(), modifiedPool.getCreationDate());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testUpdateSlbPool() throws Exception {
        SlbPool modifiedPool = new SlbPool("test-pool");
        Instance instance = new Instance();
        instance.setIp("2.2.2.2");
        modifiedPool.addInstance(instance);

        store.updateOrCreateSlbPool("test-pool", modifiedPool);

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        SlbPool expectedPool = slbModelTree.findSlbPool("test-pool");
        expectedPool.getInstances().clear();
        expectedPool.addInstance(instance);

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testSaveAspectsRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);
        List<Aspect> aspects = new ArrayList<Aspect>();
        Aspect aspect1 = new Aspect();
        aspect1.setName("t1");
        aspect1.setPointCut(PointCut.BEFORE);
        Directive d1 = new Directive();
        d1.setType("t1");
        aspect1.addDirective(d1);
        aspects.add(aspect1);
        Aspect aspect2 = new Aspect();
        aspect2.setName("t2");
        aspect2.setPointCut(PointCut.AFTER);
        Directive d2 = new Directive();
        d2.setType("t2");
        aspect2.addDirective(d2);
        aspects.add(aspect2);

        try {
            store.saveCommonAspects(aspects);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.COMMON_ASPECT_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        assertEquals(slbModelTree.getAspects(), store.listCommonAspects());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testAddStrategyRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        Strategy newStrategy = new Strategy("dper-hash");
        newStrategy.setType("hash");
        newStrategy.setDynamicAttribute("target", "dper");
        newStrategy.setDynamicAttribute("method", "crc32");

        try {
            store.updateOrCreateStrategy("dper-hash", newStrategy);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.STRATEGY_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());

        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testAddSlbPoolRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        SlbPool slbPool = new SlbPool("aaa");
        Instance instance = new Instance();
        instance.setIp("0.0.0.0");
        slbPool.addInstance(instance);

        try {
            store.updateOrCreateSlbPool("aaa", slbPool);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.SLBPOOL_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testUpdateStrategyRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        Strategy modifiedStrategy = new Strategy("uri-hash");
        modifiedStrategy.setType("hash");
        modifiedStrategy.setDynamicAttribute("target", "uri");
        modifiedStrategy.setDynamicAttribute("method", "md5");
        try {
            store.updateOrCreateStrategy("uri-hash", modifiedStrategy);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.STRATEGY_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    public void testUpdateSlbPoolRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        SlbPool slbPool = new SlbPool("test-pool");
        Instance instance = new Instance();
        instance.setIp("0.0.0.0");
        slbPool.addInstance(instance);

        try {
            store.updateOrCreateSlbPool("test-pool", slbPool);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.SLBPOOL_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testRemoveStrategy() throws Exception {
        store.removeStrategy("uri-hash");

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        slbModelTree.removeStrategy("uri-hash");

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testRemoveSlbPool() throws Exception {
        store.removeSlbPool("test-pool");

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        slbModelTree.removeSlbPool("test-pool");

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testRemovePool() throws Exception {
        store.removePool("Web.Tuangou");

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        slbModelTree.removePool("Web.Tuangou");

        assertEquals(new ArrayList<Pool>(slbModelTree.getPools().values()), store.listPools());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testRemoveStrategyRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        try {
            store.removeStrategy("uri-hash");
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.STRATEGY_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<Strategy>(slbModelTree.getStrategies().values()), store.listStrategies());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testRemoveSlbPoolRollback() throws Exception {
        new File(tmpDir, "slb_base.xml").setWritable(false);

        try {
            store.removeSlbPool("test-pool");
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.SLBPOOL_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));

        assertEquals(new ArrayList<SlbPool>(slbModelTree.getSlbPools().values()), store.listSlbPools());

        assertEquals(slbModelTree, "slb_base.xml");
        assertRawFileNotChanged("slb_base.xml");
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
    }

    @Test
    public void testUpdateVirtualServer() throws Exception {

        VirtualServer newVirtualServer = DefaultSaxParser.parse(
                FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))).findVirtualServer("www");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        VirtualServer originalVirtualServer = store.findVirtualServer("www");
        int originalVersion = originalVirtualServer.getVersion();
        Date originalCreationDate = originalVirtualServer.getCreationDate();

        Date now = new Date();
        store.updateVirtualServer("www", newVirtualServer);
        SlbModelTree slbModelTree = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));

        slbModelTree.addVirtualServer(newVirtualServer);

        Assert.assertEquals(originalVersion + 1, store.findVirtualServer("www").getVersion());
        Assert.assertEquals(originalCreationDate, store.findVirtualServer("www").getCreationDate());
        Assert.assertEquals(now, store.findVirtualServer("www").getLastModifiedDate());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(newVirtualServer, store.findVirtualServer("www"), "m_version",
                "m_creationDate", "m_lastModifiedDate"));
        // assert the whole model
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        SlbModelTree wwwSlbModelTree = DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir,
                "slb_www.xml")));
        wwwSlbModelTree.addVirtualServer(newVirtualServer);
        // assert www slbModelTree has updated
        assertEquals(wwwSlbModelTree, "slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testUpdateVirtualServerConcurrentModification() throws Exception {

        VirtualServer newVirtualServer = DefaultSaxParser.parse(
                FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))).findVirtualServer("www");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        VirtualServer originalVirtualServer = store.findVirtualServer("www");
        int originalVersion = originalVirtualServer.getVersion();
        Date originalCreationDate = originalVirtualServer.getCreationDate();

        Date now = new Date();
        store.updateVirtualServer("www", newVirtualServer);

        // modify concurrent
        VirtualServer newVirtualServer1 = DefaultSaxParser.parse(
                FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))).findVirtualServer("www");
        newVirtualServer1.setAvailability(Availability.OFFLINE);
        newVirtualServer1.setState(State.DISABLED);
        newVirtualServer1.setDefaultPoolName("test-pool1");
        Location newLocation1 = new Location();
        newLocation1.setCaseSensitive(true);
        newLocation1.setMatchType("prefix");
        newLocation1.setPattern("/");
        Directive newDirective1 = new Directive();
        newDirective1.setType("static-resource1");
        newDirective1.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs1");
        newDirective1.setDynamicAttribute("expires", "300d");
        newLocation1.addDirective(newDirective1);
        newVirtualServer1.addLocation(newLocation1);
        try {
            store.updateVirtualServer("www", newVirtualServer1);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_CONCURRENT_MOD, e.getMessageId());
        } catch (Exception e1) {
            Assert.fail();
        }
        // modify concurrent end

        SlbModelTree slbModelTree = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));

        slbModelTree.addVirtualServer(newVirtualServer);

        Assert.assertEquals(originalVersion + 1, store.findVirtualServer("www").getVersion());
        Assert.assertEquals(originalCreationDate, store.findVirtualServer("www").getCreationDate());
        Assert.assertEquals(now, store.findVirtualServer("www").getLastModifiedDate());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(newVirtualServer, store.findVirtualServer("www"), "m_version",
                "m_creationDate", "m_lastModifiedDate"));
        // assert the whole model
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        SlbModelTree wwwSlbModelTree = DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir,
                "slb_www.xml")));
        wwwSlbModelTree.addVirtualServer(newVirtualServer);
        // assert www slbModelTree has updated
        assertEquals(wwwSlbModelTree, "slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");

    }

    @Test
    public void testUpdateVirtualServerNotExists() throws Exception {

        VirtualServer newVirtualServer = DefaultSaxParser.parse(
                FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))).findVirtualServer("www");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        try {
            store.updateVirtualServer("test", newVirtualServer);
            Assert.fail();
        } catch (BizException e) {

        } catch (Exception e1) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));

        // assert the whole model
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");

    }

    @Test
    public void testUpdateVirtualServerRollback() throws Exception {
        new File(tmpDir, "slb_www.xml").setWritable(false);

        VirtualServer newVirtualServer = DefaultSaxParser.parse(
                FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))).findVirtualServer("www");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        try {
            store.updateVirtualServer("www", newVirtualServer);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();

        }
        SlbModelTree slbModelTree = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));

        // assert the whole model
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testAddVirtualServer() throws Exception {
        VirtualServer newVirtualServer = new VirtualServer("testVs");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        Date now = new Date();
        store.addVirtualServer("testVs", newVirtualServer);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(newVirtualServer, store.findVirtualServer("testVs"), true));
        Assert.assertEquals(1, newVirtualServer.getVersion());
        Assert.assertEquals(now, newVirtualServer.getCreationDate());
        Assert.assertEquals(now, newVirtualServer.getLastModifiedDate());

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));
        slbModelTree.addVirtualServer(newVirtualServer);
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        // assert new file created
        Assert.assertTrue(new File(tmpDir, "slb_testVs.xml").exists());
        Assert.assertEquals(1, DefaultSaxParser.parse(FileUtils.readFileToString(new File(tmpDir, "slb_testVs.xml")))
                .getVirtualServers().size());
        VirtualServer virtualServerFromFile = DefaultSaxParser
                .parse(FileUtils.readFileToString(new File(tmpDir, "slb_testVs.xml"))).getVirtualServers()
                .get("testVs");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertTrue(EqualsBuilder.reflectionEquals(newVirtualServer, virtualServerFromFile, "m_creationDate",
                "m_lastModifiedDate"));
        Assert.assertEquals(sdf.format(newVirtualServer.getCreationDate()),
                sdf.format(virtualServerFromFile.getCreationDate()));
        Assert.assertEquals(sdf.format(newVirtualServer.getLastModifiedDate()),
                sdf.format(virtualServerFromFile.getLastModifiedDate()));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");

    }

    @Test
    public void testAddVirtualServerExists() throws Exception {
        VirtualServer newVirtualServer = new VirtualServer("www");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        try {
            store.addVirtualServer("www", newVirtualServer);
            Assert.fail();
        } catch (BizException e) {

        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        // assert new file not created
        Assert.assertFalse(new File(tmpDir, "slb_testVs.xml").exists());
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");

    }

    @Test
    public void testAddVirtualServerRollback() throws Exception {
        tmpDir.setWritable(false);

        VirtualServer newVirtualServer = new VirtualServer("test");
        newVirtualServer.setAvailability(Availability.OFFLINE);
        newVirtualServer.setState(State.DISABLED);
        newVirtualServer.setDefaultPoolName("test-pool");
        Location newLocation = new Location();
        newLocation.setCaseSensitive(false);
        newLocation.setMatchType("exact");
        newLocation.setPattern("/favicon.ico");
        Directive newDirective = new Directive();
        newDirective.setType("static-resource");
        newDirective.setDynamicAttribute("root-doc", "/var/www/virtual/big.server.com/htdocs");
        newDirective.setDynamicAttribute("expires", "30d");
        newLocation.addDirective(newDirective);
        newVirtualServer.addLocation(newLocation);

        try {
            store.addVirtualServer("test", newVirtualServer);
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        SlbModelTree slbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(baseDir, "slb_base.xml")));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml"))));
        new DefaultMerger().merge(slbModelTree,
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_tuangou.xml"))));
        assertEquals(new ArrayList<VirtualServer>(slbModelTree.getVirtualServers().values()),
                store.listVirtualServers());
        // assert new file not created
        Assert.assertFalse(new File(tmpDir, "slb_testVs.xml").exists());
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");

    }

    @Test
    public void testRemoveVirtualServer() throws Exception {
        store.removeVirtualServer("www");

        Assert.assertNull(store.findVirtualServer("www"));
        // assert www slbModelTree deleted
        Assert.assertFalse(new File(tmpDir, "slb_www.xml").exists());

        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testRemoveVirtualServerRollback() throws Exception {
        tmpDir.setWritable(false);
        new File(tmpDir, "slb_www.xml").setWritable(false);
        try {
            store.removeVirtualServer("www");
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_SAVE_FAIL, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertNotNull(store.findVirtualServer("www"));
        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testRemoveVirtualServerNotExist() throws Exception {
        try {
            store.removeVirtualServer("sss");
            Assert.fail();
        } catch (BizException e) {
        } catch (Exception e) {
            Assert.fail();
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testTag() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        File tagFile = new File(tmpDir, "tag/www/" + sdf.format(new Date()) + "/slb_www.xml_1");
        File tagFile2 = new File(tmpDir, "tag/www/" + sdf.format(new Date()) + "/slb_www.xml_2");
        Assert.assertTrue(tagFile.exists());
        Assert.assertTrue(tagFile2.exists());
        SlbModelTree expected = DefaultSaxParser.parse(FileUtils.readFileToString(new File(baseDir, "slb_www.xml")));
        for (Pool pool : store.listPools()) {
            expected.addPool(pool);
        }
        for (Aspect aspect : store.listCommonAspects()) {
            expected.addAspect(aspect);
        }
        Assert.assertEquals(expected, DefaultSaxParser.parse(FileUtils.readFileToString(tagFile)));
        Assert.assertEquals(expected, DefaultSaxParser.parse(FileUtils.readFileToString(tagFile2)));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testTagVSNotExists() throws Exception {
        try {
            store.tag("www2", 1, store.listPools(), store.listCommonAspects());
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(e.getMessageId(), MessageID.VIRTUALSERVER_NOT_EXISTS);
        } catch (Exception e) {
            Assert.fail();
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testTagConcurrentMod() throws Exception {
        try {
            store.tag("www", 2, store.listPools(), store.listCommonAspects());
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_CONCURRENT_MOD, e.getMessageId());
            Assert.assertTrue(e.getCause() instanceof ConcurrentModificationException);
        } catch (Exception e) {
            Assert.fail();
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testTagIOException() throws Exception {
        File tagDir = new File(tmpDir, "tag/www");
        FileUtils.forceMkdir(tagDir);
        tagDir.setWritable(false);
        try {
            store.tag("www", 1, store.listPools(), store.listCommonAspects());
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(e.getMessageId(), MessageID.VIRTUALSERVER_TAG_FAIL);
            Assert.assertTrue(e.getCause() instanceof IOException);
        } catch (Exception e) {
            Assert.fail();
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testListTagIds() throws Exception {
        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());

        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        List<String> wwwTagIds = store.listTagIds("www");
        List<String> tuangouTagIds = store.listTagIds("tuangou");

        Assert.assertArrayEquals(new String[] { "www-2", "www-1" }, wwwTagIds.toArray(new String[0]));
        Assert.assertArrayEquals(new String[] { "tuangou-3", "tuangou-2", "tuangou-1" },
                tuangouTagIds.toArray(new String[0]));

        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());

        wwwTagIds = store.listTagIds("www");
        tuangouTagIds = store.listTagIds("tuangou");

        Assert.assertArrayEquals(new String[] { "www-3", "www-2", "www-1" }, wwwTagIds.toArray(new String[0]));
        Assert.assertArrayEquals(new String[] { "tuangou-4", "tuangou-3", "tuangou-2", "tuangou-1" },
                tuangouTagIds.toArray(new String[0]));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testListTagIdsVsNotExists() throws Exception {

        try {
            store.listTagIds("test");
            Assert.fail();
        } catch (BizException e) {
            Assert.assertEquals(MessageID.VIRTUALSERVER_NOT_EXISTS, e.getMessageId());
        } catch (Exception e) {
            Assert.fail();
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testListTagIdsNoTags() throws Exception {

        Assert.assertTrue(store.listTagIds("www").size() == 0);

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testGetTag() throws Exception {
        String tagId = store.tag("www", 1, store.listPools(), store.listCommonAspects());

        SlbModelTree tagSlbModelTree = store.getTag("www", tagId);

        Assert.assertEquals(store.findVirtualServer("www").toString(), tagSlbModelTree.findVirtualServer("www")
                .toString());

        for (Pool expPool : store.listPools()) {
            Assert.assertTrue(EqualsBuilder.reflectionEquals(expPool, tagSlbModelTree.findPool(expPool.getName()), true));
        }

        assertEquals(store.listCommonAspects(), tagSlbModelTree.getAspects());

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testFindPrevTagId() throws Exception {
        String tag1 = store.tag("www", 1, store.listPools(), store.listCommonAspects());
        String tag2 = store.tag("www", 1, store.listPools(), store.listCommonAspects());

        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        Assert.assertEquals(tag1, store.findPrevTagId("www", tag2));
        Assert.assertNull(store.findPrevTagId("www", tag1));

        String tag3 = store.tag("www", 1, store.listPools(), store.listCommonAspects());
        Assert.assertEquals(tag2, store.findPrevTagId("www", tag3));
        Assert.assertEquals(tag1, store.findPrevTagId("www", tag2));
        Assert.assertNull(store.findPrevTagId("www", tag1));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testListMultiFolderTags() throws Exception {
        FileUtils.copyFile(new File(tmpDir, "slb_www.xml"), new File(tmpDir, "tag/www/20120101/slb_www.xml_1"));
        FileUtils.copyFile(new File(tmpDir, "slb_www.xml"), new File(tmpDir, "tag/www/20120102/slb_www.xml_2"));

        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("www", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());
        store.tag("tuangou", 1, store.listPools(), store.listCommonAspects());

        List<String> wwwTagIds = store.listTagIds("www");
        List<String> tuangouTagIds = store.listTagIds("tuangou");

        Assert.assertArrayEquals(new String[] { "www-4", "www-3", "www-2", "www-1" }, wwwTagIds.toArray(new String[0]));
        Assert.assertArrayEquals(new String[] { "tuangou-3", "tuangou-2", "tuangou-1" },
                tuangouTagIds.toArray(new String[0]));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testGetMultiFolderTag() throws Exception {
        SlbModelTree tagSlbModelTree = DefaultSaxParser.parse(FileUtils
                .readFileToString(new File(tmpDir, "slb_www.xml")));
        for (Pool pool : DefaultSaxParser.parse(FileUtils.readFileToString(new File(tmpDir, "slb_base.xml")))
                .getPools().values()) {
            tagSlbModelTree.addPool(pool);
        }
        FileUtils.writeStringToFile(new File(tmpDir, "tag/www/20120101/slb_www.xml_1"), tagSlbModelTree.toString());
        FileUtils.writeStringToFile(new File(tmpDir, "tag/www/20120101/slb_www.xml_2"), tagSlbModelTree.toString());

        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        Assert.assertEquals(store.findVirtualServer("www").toString(),
                store.getTag("www", "www-1").findVirtualServer("www").toString());
        for (Pool pool : store.listPools()) {
            Assert.assertTrue(EqualsBuilder.reflectionEquals(pool, store.getTag("www", "www-1")
                    .findPool(pool.getName()), true));
        }
        Assert.assertEquals(store.findVirtualServer("www").toString(),
                store.getTag("www", "www-2").findVirtualServer("www").toString());
        for (Pool pool : store.listPools()) {
            Assert.assertTrue(EqualsBuilder.reflectionEquals(pool, store.getTag("www", "www-2")
                    .findPool(pool.getName()), true));
        }

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    @Test
    public void testRemoveTagAndFindLatestTag() throws Exception {
        FileUtils.copyFile(new File(tmpDir, "slb_www.xml"), new File(tmpDir, "tag/www/20120101/slb_www.xml_1"));
        FileUtils.copyFile(new File(tmpDir, "slb_www.xml"), new File(tmpDir, "tag/www/20120101/slb_www.xml_2"));
        FileUtils.copyFile(new File(tmpDir, "slb_www.xml"), new File(tmpDir, "tag/www/20120102/slb_www.xml_3"));

        store = new LocalFileModelStoreImpl();
        store.setBaseDir(tmpDir.getAbsolutePath());
        store.init();

        store.tag("www", 1, store.listPools(), store.listCommonAspects());

        store.removeTag("www", "www-2");
        store.removeTag("www", "www-4");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        Assert.assertFalse(new File(tmpDir, "tag/www/20120101/slb_www.xml_2").exists());
        Assert.assertFalse(new File(tmpDir, "tag/www/" + sdf.format(new Date()) + "/slb_www.xml_4").exists());

        List<String> tagIds = store.listTagIds("www");
        Assert.assertArrayEquals(new String[] { "www-3", "www-1" }, tagIds.toArray());
        Assert.assertEquals("www-3", store.findLatestTagId("www"));

        store.removeTag("www", "www-1");
        store.removeTag("www", "www-3");
        Assert.assertFalse(new File(tmpDir, "tag/www/20120101/slb_www.xml_1").exists());
        Assert.assertFalse(new File(tmpDir, "tag/www/20120101/slb_www.xml_3").exists());

        Assert.assertEquals(0, store.listTagIds("www").size());
        Assert.assertNull(store.findLatestTagId("www"));

        assertRawFileNotChanged("slb_www.xml");
        assertRawFileNotChanged("slb_tuangou.xml");
        assertRawFileNotChanged("slb_base.xml");
    }

    private void assertRawFileNotChanged(String fileName) throws IOException {
        Assert.assertEquals(FileUtils.readFileToString(new File(baseDir, fileName)),
                FileUtils.readFileToString(new File(tmpDir, fileName)));
    }

    private void assertEquals(SlbModelTree slbModelTree, String fileName) throws SAXException, IOException {
        Assert.assertEquals(slbModelTree.toString(),
                DefaultSaxParser.parse(FileUtils.readFileToString(new File(tmpDir, fileName))).toString());
    }

    private <T> void assertEquals(List<T> expectedList, List<T> actualList) {
        Assert.assertEquals(expectedList.size(), actualList.size());
        for (T expected : expectedList) {
            Assert.assertTrue(actualList.contains(expected));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, actualList.get(actualList.indexOf(expected)),
                    true));
        }
    }

}
