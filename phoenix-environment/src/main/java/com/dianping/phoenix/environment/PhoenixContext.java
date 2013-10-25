package com.dianping.phoenix.environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.phoenix.environment.handler.RequestIdContext;
import com.dianping.phoenix.environment.util.ResourceUtils;

/**
 * 基于ThreadLocal的通用内存容器，用于在各个平台级别的组件中传递变量
 * 
 * <note>该类线程不安全</note>
 * 
 * @author kezhu.wu
 * 
 */
public class PhoenixContext {

    private static final Logger                                  LOG           = LoggerFactory.getLogger(PhoenixContext.class);

    public static final String                                   ENV           = "phoenixEnvironment";

    private static final Pattern                                 PATTERN       = Pattern.compile("phoenix-env.properties");

    /** 已注册的Class */
    private static Set<Class<? extends PhoenixContextInterface>> m_set         = new HashSet<Class<? extends PhoenixContextInterface>>();

    private static ThreadLocal<PhoenixContext>                   s_threadLocal = new ThreadLocal<PhoenixContext>() {
                                                                                   @Override
                                                                                   protected PhoenixContext initialValue() {
                                                                                       return new PhoenixContext();
                                                                                   }
                                                                               };

    private Map<String, PhoenixContextInterface>                 m_map         = new HashMap<String, PhoenixContextInterface>();

    private HttpServletRequest                                   m_hRequest;

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

    //对已注册的类型，进行构建实例，并且初始化
    public void setup() {
        Iterator<Class<? extends PhoenixContextInterface>> it = m_set.iterator();
        while (it.hasNext()) {
            Class<? extends PhoenixContextInterface> contextClazz = it.next();
            PhoenixContextInterface context;
            try {
                context = contextClazz.newInstance();
                context.setup(this);
                m_map.put(contextClazz.getName(), context);
            } catch (InstantiationException e) {
                throw new RuntimeException("Setup Error", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Setup Error", e);
            }
        }
    }

    public void destroy() {
        for (Map.Entry<String, PhoenixContextInterface> entry : m_map.entrySet()) {
            PhoenixContextInterface context = entry.getValue();
            context.destroy();
        }
        m_map.clear();
        s_threadLocal.remove();
    }

    @SuppressWarnings("unchecked")
    public static void init() throws IOException {
        //自动扫描phoenix-env.poperties文件，获取class
        Properties properties = new Properties();
        Map<String, byte[]> map = ResourceUtils.getResources(PATTERN);
        if (map != null) {
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                String file = entry.getKey();
                byte[] bytes = entry.getValue();
                properties.load(new ByteArrayInputStream(bytes));
                LOG.info("Loaded File: " + file);
            }
        }
        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            Object el = names.nextElement();
            String className = (String) el;
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
                if (clazz.isAssignableFrom(PhoenixContextInterface.class)) {
                    m_set.add((Class<? extends PhoenixContextInterface>) clazz);
                } else {
                    LOG.warn("Define ignored because it's not implemented of PhoenixContextInterface: " + className);
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Define ignored because class is not found: " + className);
            }
        }
    }

    //将class类注册进来
    public static void register(Class<? extends PhoenixContextInterface> clazz) {
        m_set.add(clazz);
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

    public static void main(String[] args) throws IOException {
        //第三方业务，例如移动api使用时：
        PhoenixContext.init();
    }

}
