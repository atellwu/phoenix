package com.dianping.phoenix.environment.byteman;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.phoenix.environment.PhoenixContext;

public class WrapRunnable implements RunnableScheduledFuture<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(WrapRunnable.class);

    private final Runnable      runnable;

    private PhoenixContext      phoenixContext;

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

        //如果needInitEnv为true，则复制PhoenixContext
        if (needInitEnv) {
            try {
                this.phoenixContext.copyTo(phoenixContext);
            } catch (CloneNotSupportedException e) {
                LOG.error("Error in copying phoenixContext, copy is ingored.", e);
            }
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

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return ((Future<?>) runnable).cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return ((Future<?>) runnable).isCancelled();
    }

    @Override
    public boolean isDone() {
        return ((Future<?>) runnable).isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return ((Future<?>) runnable).get();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return ((Future<?>) runnable).get(timeout, unit);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return ((Delayed) runnable).getDelay(unit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Delayed o) {
        return ((Comparable<Delayed>) runnable).compareTo(o);
    }

    @Override
    public boolean isPeriodic() {
        return ((RunnableScheduledFuture<?>) runnable).isPeriodic();
    }

}
