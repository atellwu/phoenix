package com.dianping.phoenix.environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

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

    private static final String                                  DISABLES      = "disables";

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

    private Map<String, Object>                                  m_param       = new HashMap<String, Object>();

    private boolean                                              m_setuped     = false;

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
        m_setuped = true;
    }

    public boolean isSetuped() {
        return m_setuped;
    }

    public void copyTo(PhoenixContext context) {
        for (Map.Entry<String, PhoenixContextInterface> entry : m_map.entrySet()) {
            PhoenixContextInterface c = entry.getValue();
            context.m_map.put(entry.getKey(), c.clone());
        }
        context.m_param.putAll(m_param);
    }

    public void clear() {
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
                LOG.info("Loaded config file: " + file);
            }
        }
        //将class一一注册
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String className = (String) entry.getKey();
            String disables = (String) entry.getValue();
            if (!DISABLES.equalsIgnoreCase(disables)) {
                try {
                    Class<?> clazz = Class.forName(className);
                    register((Class<? extends PhoenixContextInterface>) clazz);
                } catch (ClassNotFoundException e) {
                    LOG.warn("Define ignored because class is not found: " + className);
                }
            }
        }
    }

    //将class类注册进来
    public static void register(Class<? extends PhoenixContextInterface> clazz) {
        if (PhoenixContextInterface.class.isAssignableFrom(clazz)) {
            m_set.add(clazz);
            LOG.info("Loaded define class: " + clazz);
        } else {
            LOG.warn("Define class ignored, because it's not implemented of PhoenixContextInterface: " + clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PhoenixContextInterface> T get(Class<T> clazz) {
        T handler = (T) m_map.get(clazz.getName());
        if (handler == null) {
            try {
                handler = clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return handler;
    }

    public void addParam(String key, Object value) {
        m_param.put(key, value);
    }

    public Object getParam(String key) {
        return m_param.get(key);
    }

    //============以下是兼容方法==========
    public static PhoenixContext getInstance() {
        return get();
    }

    public void setRequestId(String requestId) {
        RequestIdContext context = get(RequestIdContext.class);
        context.setRequestId(requestId);
    }

    public String getRequestId() {
        RequestIdContext context = get(RequestIdContext.class);
        return context.getRequestId();
    }

    public void setReferRequestId(String referRequestId) {
        RequestIdContext context = get(RequestIdContext.class);
        context.setReferRequestId(referRequestId);
    }

    public String getReferRequestId() {
        RequestIdContext context = get(RequestIdContext.class);
        return context.getReferRequestId();
    }

    public void setGuid(String guid) {
        RequestIdContext context = get(RequestIdContext.class);
        context.setGuid(guid);
    }

    public String getGuid() {
        RequestIdContext context = get(RequestIdContext.class);
        return context.getGuid();
    }

    public void setMetas(String metas) {
        RequestIdContext context = get(RequestIdContext.class);
        context.setMetas(metas);
    }

    public String getMetas() {
        RequestIdContext context = get(RequestIdContext.class);
        return context.getMetas();
    }

    public static void main(String[] args) throws IOException {
        PhoenixContext.init();

        PhoenixContext context = PhoenixContext.get();
        //        context.addParam(RequestIdContext.REQUEST, value);
        context.setup();
        RequestIdContext requestIdContext = PhoenixContext.get().get(RequestIdContext.class);
        System.out.println(requestIdContext.getRequestId());
    }

}
