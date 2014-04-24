package com.dianping.phoenix.session;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.helper.Files;
import org.unidal.test.jetty.JettyServer;

import com.dianping.phoenix.servlet.PhoenixFilter;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;

@RunWith(JUnit4.class)
public class RequestIdHandlerTest extends JettyServer {
	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

	@Override
	protected String getContextPath() {
		return "/session";
	}

	@Override
	protected int getServerPort() {
		return 7377;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addFilter(PhoenixFilter.class, "/*", Handler.ALL);
		context.addServlet(MockServlet.class, "/*");
	}

	@Test
	public void test() throws Exception {
		String cookie = "userParamsCookie=trace=g10g101; sid=mpi33p45nr14odi2lymo1dzq; "
		      + "__utma=169583271.594389490.1356427603.1356427603.1356427603.1; __utmc=169583271; ctu=ab55610dc33e1bde1bc1e3bbc27c4dcfcaf1660c05ee675b7cdea449d507b975; "
		      + "userParamsCookie=trace=g119; _hc.v=\"\\\"407df8d7-8788-45bf-813e-9c907461579b.1328148663\\\"\"; optimizelyEndUserId=oeu1341928727475r0.8494333708658814; "
		      + "optimizelyBuckets=%7B%7D; hackathon=%7C%7C1%7C%7C22%7C%7C2%7C; _tr.u=Bw0SBt5hTJYxWIqB; sqltrace=52521070x; s_ViewType=1; tc=1; tt.tt=621019402.20480.0000; "
		      + "ll=7fd06e815b796be3df069dec7836c3df; ua=qmwu2000%40gmail.com; lln=qmwu2000%40gmail.com; ipbh=1381464000000; abtest=\"34,88\\|32,84\\|33,86\\|25,67\\|29,78\"; "
		      + "__utma=1.1646884542.1372236181.1385620653.1385901273.27; __utmb=1.4.10.1385901273; __utmc=1; __utmz=1.1375576820.12.3.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); "
		      + "ab=; JSESSIONID=DC4CE4779024E94F24802C7C824217F2; lb.dp=453574922.20480.0000; aburl=1; cy=1; cye=shanghai";

		check("/path/to/page1", null, cookie,
		      "407df8d7-8788-45bf-813e-9c907461579b.1328148663:null:42c1e343c593a02ade47e735a10ab194abf44cd1:null");

		check("/path/to/page2", "/path/to/page1", cookie,
		      "407df8d7-8788-45bf-813e-9c907461579b.1328148663:null:42c1e343c593a02ade47e735a10ab194abf44cd1:null");
	}

	private void check(String path, String lastPath, String cookie, String expected) throws IOException,
	      InterruptedException {
		String url = String.format("http://localhost:%s%s%s", getServerPort(), getContextPath(), path);
		String referer = String.format("http://localhost:%s%s%s", getServerPort(), getContextPath(), lastPath);
		URLConnection uc = new URL(url).openConnection();

		uc.setConnectTimeout(1000);
		uc.addRequestProperty("Host", "localhost:" + getServerPort());

		if (lastPath != null) {
			uc.addRequestProperty("Referer", referer);
		}

		if (cookie != null) {
			uc.addRequestProperty("Cookie", cookie);
		}

		InputStream in = uc.getInputStream();
		String content = Files.forIO().readFrom(in, "utf-8");
		String expectedUri = getContextPath() + path;

		Assert.assertEquals(expectedUri, content);

		RequestIdHandler handler = (RequestIdHandler) lookup(PhoenixFilterHandler.class, RequestIdHandler.ID);
		RequestEvent event = handler.take();

		Assert.assertNotNull("No RequestEvent generated!", event);

		String actual = String.format("%s:%s:%s:%s", event.getPhoenixId(), event.getRequestId(), event.getUrlDigest(),
		      event.getRefererUrlDigest());

		Assert.assertEquals(expected, actual);
	}

	public static class MockServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			ServletOutputStream out = res.getOutputStream();

			res.setContentType("text/plain");
			out.print(req.getRequestURI());
			out.flush();
		}
	}
}
