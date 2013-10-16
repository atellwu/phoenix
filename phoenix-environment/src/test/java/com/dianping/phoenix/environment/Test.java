package com.dianping.phoenix.environment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.dianping.phoenix.environment.PhoenixContext;

public class Test {

    private static Executor executor;
    static {
        executor = Executors.newFixedThreadPool(1);
    }

    /**
     * jvm添加:<br>
     * -javaagent:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar=script:/home/atell/workspace-64bit/Test/src/phoenix-env.btm,boot:/home/atell/document/opensource/byteman-
     * download-2.1.3/lib/byteman.jar
     * 
     * @param args
     */
    public static void main(String[] args) {
        Runnable task = new Task();
        PhoenixContext.getInstance().setRequestId("das");

        executor.execute(task);
    }

    public static class Task implements Runnable {
        @Override
        public void run() {
            System.out.println("requestId:" + PhoenixContext.getInstance().getRequestId());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
