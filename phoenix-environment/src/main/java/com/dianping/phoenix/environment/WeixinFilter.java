package com.dianping.phoenix.environment;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class WeixinFilter implements Filter {

	private static final String REFER_REQUEST_ID = "rid";

	@Override
   public void init(FilterConfig filterConfig) throws ServletException {
   }

	@Override
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
         ServletException {
		
		String referRequestId = req.getParameter(REFER_REQUEST_ID);
		
		if(referRequestId != null) {
			PhoenixContext.getInstance().setReferRequestId(referRequestId);
		}
		
		chain.doFilter(req, res);
   }

	@Override
   public void destroy() {
   }

}
