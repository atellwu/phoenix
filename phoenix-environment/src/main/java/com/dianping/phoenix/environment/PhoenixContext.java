package com.dianping.phoenix.environment;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.phoenix.environment.handler.RequestIdContext;

/**
 * 基于ThreadLocal的通用内存容器，用于在各个平台级别的组件中传递变量
 * 
 * <note>该类线程不安全</note>
 * 
 * @author kezhu.wu
 * 
 */
public class PhoenixContext {

    public static final String                          ENV           = "phoenixEnvironment";

    private static ThreadLocal<PhoenixContext>          s_threadLocal = new ThreadLocal<PhoenixContext>() {
                                                                          @Override
                                                                          protected PhoenixContext initialValue() {
                                                                              return new PhoenixContext();
                                                                          }
                                                                      };

    private static Map<String, PhoenixContextInterface> m_map         = new HashMap<String, PhoenixContextInterface>();

    //    private PhoenixContextContainer() {
    //    }

    //ReuquetIdContext.setup()将做一些初始化，并将自己放到PhoenixContext中。

    //ReuquetIdContext.xxx()从PhoenixContext获取当前的ReuquetIdContext对象(无则返回默认对象)

    //ReuquetIdContext的数据存储在PhoenixContext中，这样才能通过PhoenixContext直接复制所有数据

    //
    //
    public static PhoenixContext get() {
        return s_threadLocal.get();
    }

    private HttpServletRequest m_hRequest;

    //对已注册的类型，进行初始化
    public void init() {
        for (Map.Entry<String, PhoenixContextInterface> entry : m_map.entrySet()) {
            PhoenixContextInterface context = entry.getValue();
            context.setup(this);
        }
    }

    public void clear() {
        for (Map.Entry<String, PhoenixContextInterface> entry : m_map.entrySet()) {
            PhoenixContextInterface handler = entry.getValue();
            handler.destroy();
        }
        //        m_map.remove();
        s_threadLocal.remove();
    }

    //将class类注册进来，这样init时会调用该类型的实例，进行初始化环境变量
    public void register(Class<? extends PhoenixContextInterface> clazz) {
        try {
            m_map.put(clazz.getClass().getName(), clazz.newInstance());
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PhoenixContextInterface> T get(Class<T> clazz) {
        T handler = (T) m_map.get(clazz.getName());
        if (handler == null) {
            try {
                handler = clazz.newInstance();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    public <T extends PhoenixContextInterface> T get(String clazzName) {
        T handler = (T) m_map.get(clazzName);
        if (handler == null) {
            try {
                handler = (T) Class.forName(clazzName).newInstance();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return handler;
    }

    public void setHttpServletRequest(HttpServletRequest hRequest) {
        this.m_hRequest = hRequest;
    }

    public HttpServletRequest getHttpServletRequest() {
        return m_hRequest;
    }

    public static void main(String[] args) {
        //第三方业务，例如移动api使用时：
        RequestIdContext context = PhoenixContext.get().get(RequestIdContext.class);
        if (context == null) {
        }
    }

}
