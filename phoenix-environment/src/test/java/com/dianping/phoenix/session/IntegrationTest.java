package com.dianping.phoenix.session;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.lookup.ComponentTestCase;

public class IntegrationTest extends ComponentTestCase {

	public static class ExtryServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("<html><body><a href='yy'>yy</a></body></html>");
		}

	}
	
//	private RequestEventDelegate out;

	@Before
	public void before() throws Exception {
//		out = lookupById(RequestEventDelegate.class, "out");
	}

	@Test
	public void test() throws Exception {
		
//		Sockets.forClient().threads("RequestIDClient", 0).connectTo(7377, "127.0.0.1").start(out);

		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setDescriptor("src/test/webapp/WEB-INF/web.xml");
		webapp.setResourceBase("src/test/webapp/p");
		webapp.setClassLoader(this.getClass().getClassLoader());
		server.setHandler(webapp);

		server.start();
		
		System.in.read();
		
		System.exit(0);
		
	}

}
