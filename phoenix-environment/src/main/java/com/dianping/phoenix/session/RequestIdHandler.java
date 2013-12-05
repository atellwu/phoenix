package com.dianping.phoenix.session;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.net.Networks;

import com.dianping.phoenix.servlet.PhoenixFilterContext;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;

public class RequestIdHandler extends ContainerHolder implements PhoenixFilterHandler, Initializable {
	public static final String ID = "request-id";

	public static final String PHOENIX_ID_COOKIE_NAME = "PHOENIX_ID";

	private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	@Inject
	private RequestEventDelegate m_queue;

	private AtomicInteger m_req_index = new AtomicInteger();

	private AtomicInteger m_phoenix_id_index = new AtomicInteger();

	private String m_ip;

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

	private RequestEvent buildEvent(PhoenixFilterContext ctx) {
		RequestEvent event = new RequestEvent();

		event.setPhoenixId(getPhoenixId(ctx));
		event.setRequestId(getRequestId(ctx));
		event.setUrlDigest(getUrlDigest(ctx));
		event.setRefererUrlDigest(getRefererUrlDigest(ctx));
		event.setTimestamp(System.currentTimeMillis());
		event.setHop(0);

		return event;
	}

	private String generatePhoenixId(PhoenixFilterContext ctx) {
		long ts = System.currentTimeMillis();
		int seq = m_phoenix_id_index.incrementAndGet();

		StringBuilder sb = new StringBuilder();
		sb.append(m_ip);
		sb.append("-");
		sb.append(Long.toHexString(ts));
		sb.append("-");
		sb.append(Integer.toHexString(seq));

		return sb.toString();
	}

	private String generateRequestId() {
		long ts = System.currentTimeMillis();
		int seq = m_req_index.incrementAndGet();

		StringBuilder sb = new StringBuilder();
		sb.append(m_ip);
		sb.append("-");
		sb.append(Long.toHexString(ts));
		sb.append("-");
		sb.append(Integer.toHexString(seq));

		return sb.toString();
	}

	private String getCookie(PhoenixFilterContext ctx, String name) {
		Cookie[] cookies = ctx.getHttpServletRequest().getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	@Override
	public int getOrder() {
		return 10;
	}

	private String getPhoenixId(PhoenixFilterContext ctx) {
		String phoenixId = getCookie(ctx, PHOENIX_ID_COOKIE_NAME);

		if (phoenixId == null) {
			phoenixId = generatePhoenixId(ctx);
			setCookie(ctx, PHOENIX_ID_COOKIE_NAME, phoenixId);
		}

		return phoenixId;
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

	private String getRequestId(PhoenixFilterContext ctx) {

		return generateRequestId();

	}

	private String getUrlDigest(PhoenixFilterContext ctx) {
		HttpServletRequest req = ctx.getHttpServletRequest();
		String url = req.getRequestURL().toString();

		return sha1(url);
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
		m_ip = bytesToHex(Networks.forIp().getLocalAddress());
		m_queue = lookupById(RequestEventDelegate.class, "out");
	}

	private void setCookie(PhoenixFilterContext ctx, String cookieName, String cookieValue) {
		ctx.getHttpServletResponse().addCookie(new Cookie(cookieName, cookieValue));
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

}
