package com.dianping.phoenix.environment;

/**
 * 
 * <note>实现类必须提供默认的构造方法。</note>
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
