package com.dianping.phoenix.session;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;
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

	private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	@Inject
	private RequestEventDelegate m_queue;

	private AtomicInteger m_index = new AtomicInteger();

	private String m_ip;

	private RequestEvent buildEvent(PhoenixFilterContext ctx) {
		RequestEvent event = new RequestEvent();

		event.setUserId(getUserId(ctx));
		event.setRequestId(getRequestId(ctx));
		event.setUrlDigest(getUrlDigest(ctx));
		event.setRefererUrlDigest(getRefererUrlDigest(ctx));
		event.setTimestamp(System.currentTimeMillis());
		event.setHop(0);

		return event;
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

		// TODO
		return UUID.randomUUID().toString();
	}

	private String getUrlDigest(PhoenixFilterContext ctx) {
		HttpServletRequest req = ctx.getHttpServletRequest();
		String url = req.getRequestURL().toString();

		return sha1(url);
	}

	private String getUserId(PhoenixFilterContext ctx) {
		String userId = getCookie(ctx, "_hc.v");

		if (userId != null) {
			int len = userId.length();

			// remove the quotes if necessary
			if (len > 2 && userId.charAt(0) == '"' && userId.charAt(len - 1) == '"') {
				return userId.substring(1, len - 1);
			} else {
				return userId;
			}
		} else {
			// TODO how about if not exist?
			return null;
		}
	}

	@Override
	public void handle(PhoenixFilterContext ctx) throws IOException, ServletException {
		RequestEvent event = buildEvent(ctx);

		// put in queue in order to transfer it to remote session server asynchronously
		m_queue.offer(event);

		// pass to next Phoenix filter handler or servlet filter
		ctx.doFilter();
	}

	String sha1(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");

			digest.update(value.getBytes("utf-8"));
			byte[] data = digest.digest();

			StringBuilder sb = new StringBuilder(data.length * 2);

			for (byte b : data) {
				sb.append(HEX_DIGITS[(b >> 4) & 0x0F]);
				sb.append(HEX_DIGITS[b & 0x0F]);
			}

			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when calculating SHA-1 of %s.", value));
		}
	}

	// for test purpose
	RequestEvent take() throws InterruptedException {
		return m_queue.take();
	}

	@Override
	public void initialize() throws InitializationException {
		m_ip = Networks.forIp().getLocalHostAddress();
		m_queue = lookupById(RequestEventDelegate.class, "out");
	}
	
}
