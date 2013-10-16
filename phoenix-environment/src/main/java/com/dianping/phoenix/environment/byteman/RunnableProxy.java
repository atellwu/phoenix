package com.dianping.phoenix.environment.byteman;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.dianping.phoenix.environment.PhoenixContext;

public class RunnableProxy implements InvocationHandler {

    Runnable                    runnable;

    private Map<String, Object> map;

    /**
     * 将动态代理绑定到指定的Runnable
     * 
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
        // 如果调用的是print方法，则替换掉
        if ("run".equals(method.getName())) {

            //取出map然后放到ThreadLocal
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                PhoenixContext.getInstance().set(entry.getKey(), entry.getValue());
            }

            Object re = method.invoke(this.runnable, args);

            //清理该线程的ThreadLocal
            PhoenixContext.getInstance().clear();

            return re;

        } else {
            return method.invoke(this.runnable, args);
        }
    }

}