package com.dianping.phoenix.environment.byteman;

public class PhoenixEnvironmentHelper {

    public Runnable proxyRunnable(Runnable runnable) {

        RunnableProxy proxy = new RunnableProxy();
        return proxy.bind(runnable);
        //        new WrapRunnable(runnable);
        //        return new WrapRunnable(runnable);
    }
}
