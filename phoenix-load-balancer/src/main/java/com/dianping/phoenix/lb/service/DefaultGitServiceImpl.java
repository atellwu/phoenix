/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 25, 2013
 * 
 */
package com.dianping.phoenix.lb.service;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.shell.ScriptExecutor;

/**
 * @author Leo Liang
 * 
 */
public class DefaultGitServiceImpl implements GitService {

    private ScriptExecutor scriptExecutor;

    public void init() throws ComponentLookupException {
        scriptExecutor = PlexusComponentContainer.INSTANCE.lookup(ScriptExecutor.class);
    }

    @Override
    public boolean clone(String gitUrl, String targetDir) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkoutTag(String targetDir, String tag) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean commit(String targetDir, String comment) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean tag(String targetDir, String tag) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean push(String targetDir) {
        // TODO Auto-generated method stub
        return false;
    }

}
