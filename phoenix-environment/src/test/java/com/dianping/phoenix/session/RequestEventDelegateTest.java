package com.dianping.phoenix.session;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.net.Sockets;
import org.unidal.net.Sockets.SocketClient;
import org.unidal.net.Sockets.SocketServer;

public class RequestEventDelegateTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		RequestEventDelegate clientDelegate = lookup(RequestEventDelegate.class);
		RequestEventDelegate serverDelegate = lookup(RequestEventDelegate.class);
		SocketServer server = Sockets.forServer().listenOn(1234).threads("TestServer", 3).start(serverDelegate);
		SocketClient client = Sockets.forClient().connectTo(1234, "localhost").threads("TestClient", 2)
		      .start(clientDelegate);
		int toSend = 100;
		int isReceived = 0;

		for (int i = 0; i < toSend; i++) {
			RequestEvent origin = new RequestEvent();

			origin.setUserId("user-id-" + i);
			origin.setRequestId("request-id-" + i);
			origin.setUrlDigest("url-" + i);
			origin.setRefererUrlDigest("referer-url-" + i);
			origin.setTimestamp(System.currentTimeMillis());
			origin.setHop(i);

			clientDelegate.offer(origin);

			RequestEvent target = serverDelegate.take();

			if (target != null) {
				isReceived++;
				Assert.assertEquals(origin.toString(), target.toString());
			}
		}

		client.shutdown();
		server.shutdown();

		Assert.assertEquals("Not all messages are sent or received.", toSend, isReceived);
	}
}
