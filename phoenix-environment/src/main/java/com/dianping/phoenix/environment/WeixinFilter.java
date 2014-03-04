package com.dianping.phoenix.environment;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

public class WeixinFilter implements Filter {

	private static final Logger m_logger = Logger.getLogger(WeixinFilter.class);

	private static final String REFER_REQUEST_ID = "rid";

	private static final String PARAMETER_NAME = "parameter-name";

	private String m_parameterName = REFER_REQUEST_ID;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String configuredName = filterConfig.getInitParameter(PARAMETER_NAME);
		if (configuredName != null && configuredName.length() > 0) {
			m_parameterName = configuredName;
		}
		m_logger.info(String.format("Will use request parameter (%s) as refer request id from weixin", m_parameterName));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
	      ServletException {

		String referRequestId = req.getParameter(m_parameterName);

		if (referRequestId != null) {
			PhoenixContext.getInstance().setReferRequestId(referRequestId);
		}

		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {
	}

}
