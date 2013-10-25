package com.dianping.phoenix.environment;

/**
 * 
 * @author kezhu.wu
 * 
 */
public interface PhoenixContextInterface {

    /**
     * 参数是当前的PhoenixContext,将当前拥有的变量给你处理，可包含HttpRequest等
     * 
     * @param phoenixContext
     */
    public void setup(PhoenixContext context);

    public void destroy();

    public PhoenixContextInterface clone();
}
