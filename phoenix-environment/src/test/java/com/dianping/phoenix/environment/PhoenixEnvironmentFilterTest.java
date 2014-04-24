package com.dianping.phoenix.environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dianping.phoenix.servlet.PhoenixFilter.Context;
import com.dianping.phoenix.servlet.PhoenixFilterContext;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;

public class PhoenixEnvironmentFilterTest {

	interface Action {
		public void doCheck(PhoenixEnvironment env);
	}

	@Test
	public void testNormalSite() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);

		check(req, new Action() {

			@Override
			public void doCheck(PhoenixEnvironment env) {
				assertNotNull(PhoenixContext.getInstance().getRequestId());
				assertNull(PhoenixContext.getInstance().getReferRequestId());
			}

		});
	}

	@Test
	public void testMobileSite() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		final String reqId = "mobile request id";
		final String referId = "mobile  refer id";
		when(req.getHeader(PhoenixContext.MOBILE_REQUEST_ID)).thenReturn(reqId);
		when(req.getHeader(PhoenixContext.MOBILE_REFER_REQUEST_ID)).thenReturn(referId);

		check(req, new Action() {

			@Override
			public void doCheck(PhoenixEnvironment env) {
				assertEquals(reqId, PhoenixContext.getInstance().getRequestId());
				assertEquals(referId, PhoenixContext.getInstance().getReferRequestId());
			}

		});
	}

	@Test
	public void testWeixinSite() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);

		final String referId = "weixin refer id";
		PhoenixContext.getInstance().setReferRequestId(referId);

		check(req, new Action() {

			@Override
			public void doCheck(PhoenixEnvironment env) {
				assertNotNull(PhoenixContext.getInstance().getRequestId());
				assertEquals(referId, PhoenixContext.getInstance().getReferRequestId());
			}

		});
	}
	
	@Test
	public void testWeixinOnMobileSite() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		final String reqId = "mobile request id";
		final String mobileReferId = "mobile  refer id";
		when(req.getHeader(PhoenixContext.MOBILE_REQUEST_ID)).thenReturn(reqId);
		when(req.getHeader(PhoenixContext.MOBILE_REFER_REQUEST_ID)).thenReturn(mobileReferId);

		final String weixinReferId = "weixin refer id";
		PhoenixContext.getInstance().setReferRequestId(weixinReferId);

		check(req, new Action() {

			@Override
			public void doCheck(PhoenixEnvironment env) {
				assertNotNull(PhoenixContext.getInstance().getRequestId());
				assertEquals(mobileReferId, PhoenixContext.getInstance().getReferRequestId());
			}

		});
	}

	private void check(HttpServletRequest req, final Action action) throws Exception {
		PhoenixEnvironmentFilter filter = new PhoenixEnvironmentFilter() {
			@Override
			void setCookie(HttpServletResponse res, String cookieName, String cookieValue) {
			}
		};

		HttpServletResponse res = mock(HttpServletResponse.class);

		final AtomicReference<PhoenixEnvironment> phoenixEnv = new AtomicReference<PhoenixEnvironment>();
		Answer<Object> answer = new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				phoenixEnv.set((PhoenixEnvironment) invocation.getArguments()[1]);
				return null;
			}
		};
		doAnswer(answer).when(req).setAttribute(any(String.class), any(PhoenixEnvironment.class));

		FilterChain chain = mock(FilterChain.class);
		List<PhoenixFilterHandler> handlers = Collections.emptyList();
		PhoenixFilterContext ctx = new Context(req, res, chain, handlers) {

			@Override
			public void doFilter() throws IOException, ServletException {
				action.doCheck(phoenixEnv.get());

				assertEquals(PhoenixContext.getInstance().getRequestId(), phoenixEnv.get().getRequestId());
				assertEquals(PhoenixContext.getInstance().getReferRequestId(), phoenixEnv.get().getReferRequestId());
			}

		};
		filter.handle(ctx);
	}

}
