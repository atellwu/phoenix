package com.dianping.phoenix.environment;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于ThreadLocal的通用内存容器，用于在各个平台级别的组件中传递变量
 * 
 * @author kezhu.wu
 * 
 */
public class PhoenixContext {

    //    public static final String                   MOBILE_REQUEST_ID       = "pragma-page-id";
    //    public static final String                   MOBILE_REFER_REQUEST_ID = "pragma-prev-page-id";
    //    public static final String                   METAS                   = "metas";
    //    public static final String                   REQUEST_ID              = "request_id";
    //    public static final String                   REFER_REQUEST_ID        = "refer_request_id";
    //    public static final String                   GUID                    = "guid";
    //    public static final String                   ENV                     = "phoenixEnvironment";

    //    private static PhoenixContext                instance                = new PhoenixContext();

    private static ThreadLocal<PhoenixContext>   s_threadLocal = new ThreadLocal<PhoenixContext>() {
                                                                   @Override
                                                                   protected PhoenixContext initialValue() {
                                                                       return new PhoenixContext();
                                                                   }
                                                               };

    //    private ThreadLocal<Map<String, Object>>     map                     = new ThreadLocal<Map<String, Object>>() {
    //                                                                             protected synchronized Map<String, Object> initialValue() {
    //                                                                                 return new HashMap<String, Object>();
    //                                                                             }
    //                                                                         };

    private Map<Class<?>, PhoenixContextHandler> m_map         = new HashMap<Class<?>, PhoenixContextHandler>();

    private PhoenixContext() {
    }

    //    public static PhoenixContext getInstance() {
    //        return instance;
    //    }

    public static PhoenixContext get() {
        return s_threadLocal.get();
    }

    public static void remove() {
        s_threadLocal.remove();
    }

    public void set(PhoenixContextHandler handler) {
        m_map.put(handler.getClass(), handler);
    }

    public PhoenixContextHandler get(Class<?> clazz) {
        return m_map.get(clazz);
    }

    public boolean isEmpty() {
        return m_map.isEmpty();
    }

    public void copyTo(PhoenixContext phoenixContext) {
        //TODO 完成复制
    }

    //
    //    /**
    //     * 根据key获取存在ThreadLocal中的对象
    //     * 
    //     * @param key
    //     * @return
    //     */
    //    public Object get(String key) {
    //        return map.get().get(key);
    //    }
    //
    //    /**
    //     * 以key将value存在ThreadLocal中
    //     * 
    //     * @param key
    //     * @return
    //     */
    //    public void set(String key, Object value) {
    //        map.get().put(key, value);
    //    }
    //
    //    /**
    //     * 清除ThreadLocal
    //     */
    //    public void clear() {
    //        map.remove();
    //    }
    //
    //    /**
    //     * 获取metas，metas用于页头插入到html中
    //     */
    //    public String getMetas() {
    //        return (String) get(METAS);
    //    }
    //
    //    /**
    //     * 设置metas，metas用于页头插入到html中
    //     */
    //    public void setMetas(String metas) {
    //        set(METAS, metas);
    //    }
    //
    //    /**
    //     * 从ThreadLocal中获取requestId
    //     * 
    //     * @return requestId
    //     */
    //    public String getRequestId() {
    //        return (String) get(REQUEST_ID);
    //    }
    //
    //    /**
    //     * 将requestId存放到ThreadLocal中
    //     * 
    //     * @param requestId
    //     */
    //    public void setRequestId(String requestId) {
    //        set(REQUEST_ID, requestId);
    //    }
    //
    //    /**
    //     * 从ThreadLocal中获取referRequestId
    //     * 
    //     * @return referRequestId
    //     */
    //    public String getReferRequestId() {
    //        return (String) get(REFER_REQUEST_ID);
    //    }
    //
    //    /**
    //     * 将referRequestId存放到ThreadLocal中
    //     * 
    //     * @param referRequestId
    //     */
    //    public void setReferRequestId(String referRequestId) {
    //        set(REFER_REQUEST_ID, referRequestId);
    //    }
    //
    //    /**
    //     * 从ThreadLocal中获取guid
    //     * 
    //     * @return guid
    //     */
    //    public String getGuid() {
    //        return (String) get(GUID);
    //    }
    //
    //    /**
    //     * 将guid存放到ThreadLocal中
    //     * 
    //     * @param guid
    //     */
    //    public void setGuid(String guid) {
    //        set(GUID, guid);
    //    }
    //
    //    /**
    //     * 返回ThreadLocal中的map，map包含所有已存放的key-value
    //     */
    //    public Map<String, Object> getMap() {
    //        return new HashMap<String, Object>(map.get());
    //    }

}
