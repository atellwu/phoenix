package com.dianping.phoenix.environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

public class TestByteman {

    private static final String    TEST_REQUEST_ID = "test-request-id";
    private static final String    TEST_GUID       = "test-guid";
    private static ExecutorService executor;
    static {
        executor = Executors.newFixedThreadPool(2);
    }

    /**
     * jvm添加:<br>
     * -javaagent:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar=script:/home/atell/document/mywork/phoenix/phoenix-environment/src/main/java/com/dianping/phoenix/environment/
     * byteman/phoenix-env.btm,boot:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar
     * 
     * @param args
     */
    @Test
    public void test() throws Exception {
        Runnable task = new Task();
        PhoenixContext.getInstance().setRequestId(TEST_REQUEST_ID);
        PhoenixContext.getInstance().setGuid(TEST_GUID);

        executor.execute(task);
        executor.execute(task);

        Thread.sleep(1000);
        executor.shutdown();
    }

    public static class Task implements Runnable {
        @Override
        public void run() {
            System.out.println(PhoenixContext.getInstance().getRequestId());
            System.out.println(PhoenixContext.getInstance().getGuid());
            Assert.assertEquals(TEST_REQUEST_ID, PhoenixContext.getInstance().getRequestId());
            Assert.assertEquals(TEST_GUID, PhoenixContext.getInstance().getGuid());
        }
    }

}
