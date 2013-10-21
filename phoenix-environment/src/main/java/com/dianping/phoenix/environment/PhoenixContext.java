package com.dianping.phoenix.environment;

import java.util.HashMap;
import java.util.Map;

import com.dianping.phoenix.environment.handler.RequestIdContextHandler;

/**
 * 基于ThreadLocal的通用内存容器，用于在各个平台级别的组件中传递变量
 * 
 * <note>该类线程不安全</note>
 * 
 * @author kezhu.wu
 * 
 */
public class PhoenixContext {

    public static final String                                                 ENV           = "phoenixEnvironment";

    private static ThreadLocal<PhoenixContext>                                 s_threadLocal = new ThreadLocal<PhoenixContext>() {
                                                                                                 @Override
                                                                                                 protected PhoenixContext initialValue() {
                                                                                                     return new PhoenixContext();
                                                                                                 }
                                                                                             };

    private Map<Class<? extends PhoenixContextHandler>, PhoenixContextHandler> m_map         = new HashMap<Class<? extends PhoenixContextHandler>, PhoenixContextHandler>();

    private PhoenixContext() {
    }

    public static PhoenixContext get() {
        return s_threadLocal.get();
    }

    public static void remove() {
        s_threadLocal.remove();
    }

    public <T extends PhoenixContextHandler> void set(T handler) {
        m_map.put(handler.getClass(), handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends PhoenixContextHandler> T get(Class<T> clazz) {
        return (T) m_map.get(clazz);
    }

    public boolean isEmpty() {
        return m_map.isEmpty();
    }

    public void copyTo(PhoenixContext phoenixContext) {
        //TODO 完成复制
    }

    /**
     * 清除ThreadLocal
     */
    public void clear() {
        s_threadLocal.remove();
    }

    //*****************以下是兼容方法
    /**
     * @deprecated 改用PhoenixContext.get()
     */
    public static PhoenixContext getInstance() {
        return s_threadLocal.get();
    }

    /**
     * 获取metas，metas用于页头插入到html中
     * 
     * @deprecated 改用RequestIdContextHandler handler = get(RequestIdContextHandler.class); if(handler!=null)metas = handler.getMetas();
     */
    public String getMetas() {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler != null) {
            return handler.getMetas();
        }
        return null;
    }

    /**
     * 设置metas，metas用于页头插入到html中
     * 
     * @deprecated 改用RequestIdContextHandler handler = get(RequestIdContextHandler.class); if(handler!=null) handler.setMetas(metas);
     */
    public void setMetas(String metas) {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler == null) {
            handler = new RequestIdContextHandler();
            this.set(handler);
        }
        handler.setMetas(metas);
    }

    /**
     * 从ThreadLocal中获取requestId
     * 
     * @deprecated 改用RequestIdContextHandler handler = get(RequestIdContextHandler.class);再从handler中获取需要的属性;
     */
    public String getRequestId() {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler != null) {
            return handler.getRequestId();
        }
        return null;
    }

    /**
     * 将requestId存放到ThreadLocal中
     * 
     * @deprecated 改用RequestIdContextHandler handler = get(RequestIdContextHandler.class); if(handler!=null) handler.setRequestId(requestId);
     */
    public void setRequestId(String requestId) {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler == null) {
            handler = new RequestIdContextHandler();
            this.set(handler);
        }
        handler.setRequestId(requestId);
    }

    /**
     * 从ThreadLocal中获取referRequestId
     * 
     * @deprecated
     */
    public String getReferRequestId() {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler != null) {
            return handler.getReferRequestId();
        }
        return null;
    }

    /**
     * 将referRequestId存放到ThreadLocal中
     * 
     * @deprecated
     */
    public void setReferRequestId(String referRequestId) {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler == null) {
            handler = new RequestIdContextHandler();
            this.set(handler);
        }
        handler.setReferRequestId(referRequestId);
    }

    /**
     * 从ThreadLocal中获取guid
     * 
     * @deprecated
     */
    public String getGuid() {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler != null) {
            return handler.getGuid();
        }
        return null;
    }

    /**
     * 将guid存放到ThreadLocal中
     * 
     * @deprecated
     */
    public void setGuid(String guid) {
        RequestIdContextHandler handler = this.get(RequestIdContextHandler.class);
        if (handler == null) {
            handler = new RequestIdContextHandler();
            this.set(handler);
        }
        handler.setGuid(guid);
    }
    //
    //    /**
    //     * 返回ThreadLocal中的map，map包含所有已存放的key-value
    //     */
    //    public Map<String, Object> getMap() {
    //        return new HashMap<String, Object>(map.get());
    //    }

}
