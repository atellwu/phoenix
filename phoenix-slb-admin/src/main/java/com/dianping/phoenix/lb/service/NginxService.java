/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 25, 2013
 * 
 */
package com.dianping.phoenix.lb.service;

import com.dianping.phoenix.lb.exception.BizException;

/**
 * @author Leo Liang
 * 
 */
public interface NginxService {
    public static class NginxCheckResult {
        private boolean sucess;
        private String  msg;

        public NginxCheckResult(boolean sucess, String msg) {
            this.sucess = sucess;
            this.msg = msg;
        }

        public boolean isSucess() {
            return sucess;
        }

        public String getMsg() {
            return msg;
        }

    }
    
    NginxCheckResult checkConfig(String configContent) throws BizException;
}
