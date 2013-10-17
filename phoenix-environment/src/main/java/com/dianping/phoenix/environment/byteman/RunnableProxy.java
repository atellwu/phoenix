package com.dianping.phoenix.environment.byteman;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.dianping.phoenix.environment.PhoenixContext;

public class RunnableProxy implements InvocationHandler {

    /** 被代理的对象 */
    private Runnable            runnable;

    /** 存放PhoenixContext的环境变量（使用map，目前包括requestId,referRequestId,guid） */
    private Map<String, Object> map;

    /**
     * 返回Runnable的动态代理
     */
    public Runnable bind(Runnable task) {
        this.runnable = task;
        this.map = PhoenixContext.getInstance().getMap();

        return (Runnable) Proxy.newProxyInstance(task.getClass().getClassLoader(), task.getClass().getInterfaces(), this);
    }

    /**
     * 方法调用拦截器，拦截run方法
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 拦截Runnable的run方法
        if ("run".equals(method.getName())) {

            //取出map然后放到PhoenixContext
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                PhoenixContext.getInstance().set(entry.getKey(), entry.getValue());
            }

            try {
                Object re = method.invoke(this.runnable, args);
                return re;
            } finally {
                //清理该线程的PhoenixContext
                PhoenixContext.getInstance().clear();
            }

        } else {
            return method.invoke(this.runnable, args);
        }
    }

}