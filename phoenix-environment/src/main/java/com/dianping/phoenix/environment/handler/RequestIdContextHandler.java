package com.dianping.phoenix.environment.handler;

import com.dianping.phoenix.environment.PhoenixContextHandler;

public class RequestIdContextHandler implements PhoenixContextHandler {

    public static final String MOBILE_REQUEST_ID       = "pragma-page-id";
    public static final String MOBILE_REFER_REQUEST_ID = "pragma-prev-page-id";
    public static final String METAS                   = "metas";
    public static final String REQUEST_ID              = "request_id";
    public static final String REFER_REQUEST_ID        = "refer_request_id";
    public static final String GUID                    = "guid";

    private String             m_metas;
    private String             m_requestId;
    private String             m_referRequestId;
    private String             m_guid;

    /**
     * 获取metas，metas用于页头插入到html中
     */
    public String getMetas() {
        return m_metas;
    }

    /**
     * 设置metas，metas用于页头插入到html中
     */
    public void setMetas(String metas) {
        this.m_metas = metas;
    }

    /**
     * 从ThreadLocal中获取requestId
     * 
     * @return requestId
     */
    public String getRequestId() {
        return this.m_requestId;
    }

    /**
     * 将requestId存放到ThreadLocal中
     * 
     * @param requestId
     */
    public void setRequestId(String requestId) {
        this.m_requestId = requestId;
    }

    /**
     * 从ThreadLocal中获取referRequestId
     * 
     * @return referRequestId
     */
    public String getReferRequestId() {
        return this.m_referRequestId;
    }

    /**
     * 将referRequestId存放到ThreadLocal中
     * 
     * @param referRequestId
     */
    public void setReferRequestId(String referRequestId) {
        this.m_referRequestId = referRequestId;
    }

    /**
     * 从ThreadLocal中获取guid
     * 
     * @return guid
     */
    public String getGuid() {
        return this.m_guid;
    }

    /**
     * 将guid存放到ThreadLocal中
     * 
     * @param guid
     */
    public void setGuid(String guid) {
        this.m_guid = guid;
    }

}
