package com.dianping.phoenix.environment.requestid;

import javax.servlet.http.HttpServletRequest;

import com.dianping.phoenix.environment.RegisterableContext;
import com.dianping.phoenix.environment.PhoenixContext;

public class RequestIdContext implements RegisterableContext {

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

    @Override
    public void destroy() {
    }

    public RequestIdContext clone() throws CloneNotSupportedException {
        return (RequestIdContext) super.clone();
    }

    @Override
    public void setup(PhoenixContext context) {
        HttpServletRequest request = (HttpServletRequest) context.getParam(PhoenixContext.REQUEST);

        if (request != null) {

            m_requestId = request.getHeader(RequestIdContext.MOBILE_REQUEST_ID);

            if (m_requestId != null) {//如果存在requestId，则说明是移动api的web端
                m_referRequestId = request.getHeader(RequestIdContext.MOBILE_REFER_REQUEST_ID);

            } else {//普通web端  TODO 待第二期实现
                //requestId不存在，则生成

                //referRequestId，异步通过pigeon去session服务器获取

                //判断cookie中的guid是否存在，不存在则生成

                //将所有id放入request属性，供页头使用

                //request.setAttribute(PhoenixEnvironment.ENV, new PhoenixEnvironment());
            }

        }

    }

}
