package com.dianping.phoenix.environment;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * jvm添加:<br>
 * -javaagent:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar=script:/home/atell/document/mywork/phoenix/phoenix-environment/src/main/java/com/dianping/phoenix/environment/
 * byteman/phoenix-env.btm,boot:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar
 * 
 * @param args
 */
public class TestBytemanForThreadPoolExecutor {

    private static final String TEST_REQUEST_ID = "test-request-id";
    private static final String TEST_GUID       = "test-guid";

    private ExecutorService     executor        = getExcutor();

    @Before
    public void setup() {
        PhoenixContext.getInstance().setRequestId(TEST_REQUEST_ID);
        PhoenixContext.getInstance().setGuid(TEST_GUID);
    }

    @After
    public void clear() {
        executor.shutdown();
    }

    @Test
    public void testRunnbale() throws Exception {
        Task task = new Task();
        Future<?> feature = executor.submit((Runnable) task);
        feature.get();

        Assert.assertTrue(task.success);
    }

    @Test
    public void testCallable() throws Exception {
        Callable<Boolean> callableTask = new Task();
        Future<Boolean> re = executor.submit(callableTask);

        Assert.assertTrue(re.get());
    }

    @Test
    public void testNPE() throws Exception {
        Runnable task = null;
        PhoenixContext.getInstance().setRequestId(TEST_REQUEST_ID);
        PhoenixContext.getInstance().setGuid(TEST_GUID);

        try {
            executor.execute(task);
        } catch (NullPointerException e) {
            return;
        } finally {
            executor.shutdown();
        }

        Assert.fail();
    }

    /**
     * 子类继承覆盖后，可以换成别的ExecutorService进行测试。
     */
    protected ExecutorService getExcutor() {
        return Executors.newFixedThreadPool(2);
    }

    public static class Task implements Runnable, Callable<Boolean> {

        boolean success = false;

        @Override
        public void run() {
            success = TEST_REQUEST_ID.equals(PhoenixContext.getInstance().getRequestId()) && TEST_GUID.equals(PhoenixContext.getInstance().getGuid());
        }

        @Override
        public Boolean call() throws Exception {
            return TEST_REQUEST_ID.equals(PhoenixContext.getInstance().getRequestId());
        }
    }

}
