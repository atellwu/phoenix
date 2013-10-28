package com.dianping.phoenix.environment;

/**
 * 
 * @author kezhu.wu
 * 
 */
public interface RegisterableContext extends Cloneable {

    /**
     * 参数是当前的PhoenixContext,将当前拥有的变量给你处理，可包含HttpRequest等
     * 
     * @param phoenixContext
     */
    public void setup(PhoenixContext context);

    public void destroy();

    public RegisterableContext clone() throws CloneNotSupportedException;
}
