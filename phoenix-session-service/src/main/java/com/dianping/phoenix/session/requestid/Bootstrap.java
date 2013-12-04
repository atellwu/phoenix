package com.dianping.phoenix.session.requestid;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.unidal.lookup.annotation.Inject;
import org.unidal.net.Sockets;

public class Bootstrap {

	@Inject
	private EventDelegateManager eventDelegateMgr;

	public void start() {
		Sockets.forServer().listenOn(7377).threads("RequestEvent", 0).start(eventDelegateMgr.getIn());
	}

	public static void main(String[] args) throws Exception {
		PlexusContainer c = new DefaultPlexusContainer();
		c.lookup(EventProcessor.class).start();
		c.lookup(Bootstrap.class).start();
	}

}
