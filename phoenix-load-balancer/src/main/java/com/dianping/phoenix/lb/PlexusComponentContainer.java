/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Nov 20, 2013
 * 
 */
package com.dianping.phoenix.lb;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.lookup.ContainerLoader;

/**
 * @author Leo Liang
 * 
 */
public enum PlexusComponentContainer {

    INSTANCE;

    private transient PlexusContainer container;

    private PlexusComponentContainer() {
        this.container = ContainerLoader.getDefaultContainer();
    }

    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<Object> type) throws ComponentLookupException {
        return (T) this.container.lookup(type);
    }

}
