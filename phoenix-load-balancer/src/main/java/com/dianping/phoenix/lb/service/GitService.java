package com.dianping.phoenix.lb.service;

import com.dianping.phoenix.lb.exception.BizException;

public interface GitService {

    void clone(String gitUrl, String targetDir, String tag) throws BizException;

    void commitAllChanges(String targetDir, String comment) throws BizException;

    void tagAndPush(String gitUrl, String targetDir, String tag, String comment) throws BizException;

    void push(String gitUrl, String targetDir) throws BizException;
}
