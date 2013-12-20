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

import com.dianping.phoenix.servlet.PhoenixFilterContext;
import com.dianping.phoenix.servlet.PhoenixFilterHandler;

public class PhoenixEnvironmentFilter implements PhoenixFilterHandler, Initializable {
	private final Logger m_logger = LoggerFactory.getLogger(PhoenixEnvironmentFilter.class);

	public static final String PHOENIX_ID_COOKIE_NAME = "PHOENIX_ID";

	private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	public static final String ID = "phoenix-env";

	private String m_ip;

	private AtomicInteger m_req_index = new AtomicInteger(0);

	private AtomicInteger m_phoenix_id_index = new AtomicInteger(0);

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

		try {
			// 从request中或去id
			String requestId = req.getHeader(PhoenixContext.MOBILE_REQUEST_ID);
			String referRequestId = null;

			if (requestId != null) {// 如果存在requestId，则说明是移动api的web端
				referRequestId = req.getHeader(PhoenixContext.MOBILE_REFER_REQUEST_ID);
			} else {// 普通web端 TODO 待第二期实现
				// requestId不存在，则生成
				// referRequestId，异步通过pigeon去session服务器获取
				// 判断cookie中的guid是否存在，不存在则生成
				// 将所有id放入request属性，供页头使用
				// request.setAttribute(PhoenixEnvironment.ENV, new PhoenixEnvironment());
				requestId = generateRequestId();
			}

			String phoenixId = getOrCreatePhoenixId(req, res);
			PhoenixContext.getInstance().setGuid(phoenixId);

			// 将id放入ThreadLocal
			if (requestId != null) {
				PhoenixContext.getInstance().setRequestId(requestId);
			}

			if (referRequestId != null) {
				PhoenixContext.getInstance().setReferRequestId(referRequestId);
			}
		} catch (RuntimeException e) {
			m_logger.warn(e.getMessage(), e);
		}

		try {
			ctx.doFilter();
		} finally {
			// 清除ThreadLocal
			PhoenixContext.getInstance().clear();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_ip = bytesToHex(Networks.forIp().getLocalAddress());
	}

	private void setCookie(HttpServletResponse res, String cookieName, String cookieValue) {
		res.addCookie(new Cookie(cookieName, cookieValue));
	}
}
