package com.dianping.phoenix.environment.byteman;

import com.dianping.phoenix.environment.PhoenixContext;

public class WrapRunnable implements Runnable {

    private final Runnable runnable;

    private PhoenixContext phoenixContext;

    public WrapRunnable(Runnable runnable) {
        super();
        if (runnable == null) {
            throw new NullPointerException("Argument 'runnable' can not be null!");
        }
        this.runnable = runnable;
        this.phoenixContext = PhoenixContext.get();
    }

    @Override
    public int hashCode() {
        return runnable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        //比较的是runnable属性是否相等
        if (obj instanceof WrapRunnable) {
            WrapRunnable other = (WrapRunnable) obj;
            return runnable.equals(other.runnable);
        } else if (obj instanceof Runnable) {
            return runnable.equals(obj);
        }

        return false;
    }

    @Override
    public void run() {
        //是否需要初始化PhoenixContext（如果PhoenixContext的map已经存在，则不需要初始化）
        boolean needInitEnv = true;

        PhoenixContext phoenixContext = PhoenixContext.get();
        if (phoenixContext.isSetuped()) {
            needInitEnv = false;
        }

        //如果needInitEnv为true，取出map然后放到PhoenixContext
        if (needInitEnv) {
            this.phoenixContext.copyTo(phoenixContext);
        }

        try {
            this.runnable.run();
        } finally {
            //清理该线程的ThreadLocal
            if (needInitEnv) {
                phoenixContext.clear();
            }
        }
    }

}
