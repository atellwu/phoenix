/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Dec 11, 2013
 * 
 */
package com.dianping.phoenix.lb.utils;

/**
 * @author Leo Liang
 * 
 */
public class PoolNameUtils {
    public static String getPoolNamePrefix(String poolName) {
        int prefixPos = poolName.indexOf("@");
        if (prefixPos >= 0) {
            return poolName.substring(0, prefixPos);
        } else {
            return poolName;
        }
    }

    public static String rewriteToPoolNamePrefix(String vsName, String poolName) {
        return vsName + "." + poolName;
    }
}
