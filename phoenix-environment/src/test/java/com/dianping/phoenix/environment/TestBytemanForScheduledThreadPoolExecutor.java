package com.dianping.phoenix.environment;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

/**
 * jvm添加:<br>
 * -javaagent:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar=script:/home/atell/document/mywork/phoenix/phoenix-environment/src/main/java/com/dianping/phoenix/environment/
 * byteman/phoenix-env.btm,boot:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar
 * 
 * @param args
 */
public class TestBytemanForScheduledThreadPoolExecutor {

    private static final String    TEST_REQUEST_ID = "test-request-id";
    private static final String    TEST_GUID       = "test-guid";
    private static ExecutorService executor;
    static {
        executor = Executors.newScheduledThreadPool(2);
    }

    @Test
    public void test() throws Exception {
        PhoenixContext.getInstance().setRequestId(TEST_REQUEST_ID);
        PhoenixContext.getInstance().setGuid(TEST_GUID);

        Runnable task = new Task();
        executor.execute(task);
        executor.submit(task);

        Callable<Boolean> callableTask = new Task();
        Future<Boolean> re = executor.submit(callableTask);
        Assert.assertTrue(re.get());

        Thread.sleep(1000);
        executor.shutdown();
    }

    @Test
    public void testNPE() throws Exception {
        Runnable task = null;
        PhoenixContext.getInstance().setRequestId(TEST_REQUEST_ID);
        PhoenixContext.getInstance().setGuid(TEST_GUID);

        try {
            executor.execute(task);
        } catch (NullPointerException e) {
            System.out.println(e);
            return;
        } finally {
            executor.shutdown();
        }

        Assert.fail();
    }

    public static class Task implements Runnable, Callable<Boolean> {
        @Override
        public void run() {
            System.out.println(PhoenixContext.getInstance().getRequestId());
            Assert.assertEquals(TEST_REQUEST_ID, PhoenixContext.getInstance().getRequestId());
            Assert.assertEquals(TEST_GUID, PhoenixContext.getInstance().getGuid());
        }

        @Override
        public Boolean call() throws Exception {
            System.out.println(PhoenixContext.getInstance().getRequestId());
            return TEST_REQUEST_ID.equals(PhoenixContext.getInstance().getRequestId());
        }
    }

}
