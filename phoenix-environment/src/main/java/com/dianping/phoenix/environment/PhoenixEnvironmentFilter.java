package com.dianping.phoenix.environment;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.logger.LoggerFactory;
import org.unidal.net.Networks;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.phoenix.servlet.PhoenixFilterContext;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;

public class PhoenixEnvironmentFilter implements PhoenixFilterHandler, Initializable {
	private final Logger m_logger = LoggerFactory.getLogger(PhoenixEnvironmentFilter.class);

	public static final String PHOENIX_ID_COOKIE_NAME = "PHOENIX_ID";

	public static final String LION_KEY_COOKIE_DOMAIN = "session-service.cookie.domain";

	private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	public static final String ID = "phoenix-env";

	public static final String REQUEST_HEADER_NAME = "send_http_request_id";

	public static final String REFER_REQUEST_HEADER_NAME = "send_http_refer_request_id";

	private String m_ip;

	private AtomicInteger m_req_index = new AtomicInteger(0);

	private AtomicInteger m_phoenix_id_index = new AtomicInteger(0);

	private String m_cookieDomain;

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

	private IdHolder findOrCreateIdFromRequest(HttpServletRequest req, HttpServletResponse res) {
		IdHolder idHolder = new IdHolder();
		String requestId = null;
		String referRequestId = null;

		if (mobileHeaderPresent(req)) {
			requestId = req.getHeader(PhoenixContext.MOBILE_REQUEST_ID);
			referRequestId = req.getHeader(PhoenixContext.MOBILE_REFER_REQUEST_ID);
		} else {
			requestId = generateRequestId();
			referRequestId = PhoenixContext.getInstance().getReferRequestId();
		}

		String phoenixId = getOrCreatePhoenixId(req, res);

		idHolder.setPhoenixId(phoenixId);
		idHolder.setReferRequestId(referRequestId);
		idHolder.setRequestId(requestId);

		return idHolder;
	}

	private String generatePhoenixId() {
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

	private String getCookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	private String getOrCreatePhoenixId(HttpServletRequest req, HttpServletResponse res) {
		String phoenixId = getCookie(req, PHOENIX_ID_COOKIE_NAME);

		if (phoenixId == null) {
			phoenixId = generatePhoenixId();
			setCookie(res, PHOENIX_ID_COOKIE_NAME, phoenixId);
		}

		return phoenixId;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void handle(PhoenixFilterContext ctx) throws IOException, ServletException {
		HttpServletRequest req = ctx.getHttpServletRequest();
		HttpServletResponse res = ctx.getHttpServletResponse();

		IdHolder idHolder = null;
		try {
			idHolder = findOrCreateIdFromRequest(req, res);

			setToPhoenixContext(req, idHolder);

			setToRequestAttribute(req, idHolder);
		} catch (Exception e) {
			m_logger.warn("Error initialize PhoenixContext", e);
		}

		try {
			ctx.doFilter();
		} finally {
			PhoenixContext.getInstance().clear();

			setToResponseHeader(res, idHolder);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_ip = bytesToHex(Networks.forIp().getLocalAddress());
		m_cookieDomain = readCookieDomain();
	}

	private boolean mobileHeaderPresent(HttpServletRequest req) {
		return req.getHeader(PhoenixContext.MOBILE_REQUEST_ID) != null;
	}

	private String readCookieDomain() {
		ConfigCache lion = null;
		String domain = null;

		try {
			lion = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
			domain = lion.getProperty(LION_KEY_COOKIE_DOMAIN);
		} catch (LionException e) {
			m_logger.error("Error read cookie domain from lion", e);
			throw new RuntimeException(e);
		}

		if (domain == null) {
			throw new RuntimeException("Cookie domain from lion is null");
		}

		return domain;

	}

	void setCookie(HttpServletResponse res, String cookieName, String cookieValue) {
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setPath("/");
		cookie.setDomain(m_cookieDomain);
		res.addCookie(cookie);
	}

	private void setToPhoenixContext(HttpServletRequest req, IdHolder idHolder) {
		PhoenixContext.getInstance().setGuid(idHolder.getPhoenixId());
		PhoenixContext.getInstance().setRequestId(idHolder.getRequestId());
		PhoenixContext.getInstance().setReferRequestId(idHolder.getReferRequestId());
	}

	private void setToRequestAttribute(HttpServletRequest req, IdHolder idHolder) {
		PhoenixEnvironment env = new PhoenixEnvironment(idHolder.getRequestId(), idHolder.getPhoenixId(),
		      idHolder.getReferRequestId());
		req.setAttribute(PhoenixEnvironment.ENV, env);
	}

	private void setToResponseHeader(HttpServletResponse res, IdHolder idHolder) {
		if (idHolder != null) {
			if (idHolder.getRequestId() != null) {
				res.setHeader(REQUEST_HEADER_NAME, idHolder.getRequestId());
			}

			if (idHolder.getReferRequestId() != null) {
				res.setHeader(REFER_REQUEST_HEADER_NAME, idHolder.getReferRequestId());
			}
		}
	}

	static class IdHolder {
		private String m_requestId;

		private String m_referRequestId;

		private String m_phoenixId;

		public String getPhoenixId() {
			return m_phoenixId;
		}

		public String getReferRequestId() {
			return m_referRequestId;
		}

		public String getRequestId() {
			return m_requestId;
		}

		public void setPhoenixId(String phoenixId) {
			m_phoenixId = phoenixId;
		}

		public void setReferRequestId(String referRequestId) {
			m_referRequestId = referRequestId;
		}

		public void setRequestId(String requestId) {
			m_requestId = requestId;
		}

	}
}
