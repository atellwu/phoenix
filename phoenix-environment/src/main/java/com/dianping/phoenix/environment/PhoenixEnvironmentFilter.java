package com.dianping.phoenix.environment;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.phoenix.environment.requestid.RequestIdContext;

public class PhoenixEnvironmentFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(PhoenixEnvironmentFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            PhoenixContext context = PhoenixContext.get();

            try {
                HttpServletRequest hRequest = (HttpServletRequest) request;

                context.addParam(RequestIdContext.REQUEST, hRequest);
                context.setup();

            } catch (RuntimeException e) {
                LOG.warn(e.getMessage(), e);
            }

            try {
                chain.doFilter(request, response);

            } finally {
                //清除ThreadLocal
                context.clear();
            }

        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            PhoenixContext.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
    }

}
