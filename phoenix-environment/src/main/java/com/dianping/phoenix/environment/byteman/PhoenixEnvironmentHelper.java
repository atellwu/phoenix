package com.dianping.phoenix.environment.byteman;

public class PhoenixEnvironmentHelper {

    public Runnable proxyRunnable(Runnable runnable) {

        if (runnable == null) {
            return runnable;//不能抛出NPE异常，否则byteman会报错
        }

        return new WrapRunnable(runnable);
    }

}
