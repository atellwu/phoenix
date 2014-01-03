package com.dianping.phoenix.session;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.environment.PhoenixContext;
import com.dianping.phoenix.servlet.PhoenixFilterContext;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;
import com.dianping.phoenix.session.server.DefaultEventPublisher;
import com.dianping.phoenix.session.server.EventPublisher;

public class RequestIdHandler extends ContainerHolder implements PhoenixFilterHandler, Initializable {
	public static final String ID = "request-id";

	public static final String PHOENIX_ID_COOKIE_NAME = "PHOENIX_ID";

	private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	@Inject
	private RequestEventDelegate m_queue;

	private RequestEvent buildEvent(PhoenixFilterContext ctx) {
		RequestEvent event = new RequestEvent();

		event.setPhoenixId(PhoenixContext.getInstance().getGuid());
		event.setRequestId(PhoenixContext.getInstance().getRequestId());
		event.setUrlDigest(getUrlDigest(ctx));
		event.setRefererUrlDigest(getRefererUrlDigest(ctx));
		event.setTimestamp(System.currentTimeMillis());
		event.setHop(0);

		return event;
	}

	@Override
	public int getOrder() {
		return 10;
	}

	private String getRefererUrlDigest(PhoenixFilterContext ctx) {
		HttpServletRequest req = ctx.getHttpServletRequest();
		String referer = req.getHeader("referer");

		if (referer == null) {
			return null;
		} else {
			return sha1(referer);
		}
	}

	private String getUrlDigest(PhoenixFilterContext ctx) {
		HttpServletRequest req = ctx.getHttpServletRequest();
		String url = req.getRequestURL().toString();
		String queryString = req.getQueryString();
		
		String fullUrl = url;
		if (queryString != null) {
			fullUrl = fullUrl + "?" + queryString;
		}

		return sha1(fullUrl);
	}

	@Override
	public void handle(PhoenixFilterContext ctx) throws IOException, ServletException {
		RequestEvent event = buildEvent(ctx);

		// put in queue in order to transfer it to remote session server asynchronously
		m_queue.offer(event);

		// pass to next Phoenix filter handler or servlet filter
		ctx.doFilter();

	}

	@Override
	public void initialize() throws InitializationException {
		m_queue = lookupById(RequestEventDelegate.class, "out");

		DefaultEventPublisher eventPublisher = (DefaultEventPublisher) lookup(EventPublisher.class);
		eventPublisher.start(m_queue);
	}

	String sha1(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");

			digest.update(value.getBytes("utf-8"));
			byte[] data = digest.digest();

			return bytesToHex(data);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when calculating SHA-1 of %s.", value));
		}
	}

	// for test purpose
	RequestEvent take() throws InterruptedException {
		return m_queue.take();
	}

	private String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_DIGITS[v >>> 4];
			hexChars[j * 2 + 1] = HEX_DIGITS[v & 0x0F];
		}
		return new String(hexChars);
	}

}
