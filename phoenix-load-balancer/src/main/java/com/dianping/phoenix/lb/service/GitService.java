package com.dianping.phoenix.lb.service;

public interface GitService {
    boolean clone(String gitUrl, String targetDir);

    boolean checkoutTag(String targetDir, String tag);

    boolean commit(String targetDir, String comment);
    
    boolean tag(String targetDir, String tag);
    
    boolean push(String targetDir);
}
