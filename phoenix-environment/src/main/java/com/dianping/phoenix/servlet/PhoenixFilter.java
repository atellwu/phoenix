package com.dianping.phoenix.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.lookup.ContainerLoader;

public class PhoenixFilter implements Filter {
	private List<PhoenixFilterHandler> m_handlers;

	private Throwable m_exception;

	@Override
	public void destroy() {
	}

	private void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException,
	      ServletException {
		if (m_exception != null) {
			m_exception.printStackTrace(); // TODO log to CAT

			StringWriter sw = new StringWriter(4096);

			m_exception.printStackTrace(new PrintWriter(sw));

			res.sendError(500, sw.toString());
		} else {
			Context ctx = new Context(req, res, chain, m_handlers);

			ctx.doFilter();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		if (m_exception != null) {
			throw new ServletException("Error when initializating PhoenixFilter!", m_exception);
		} else if (m_handlers == null) {
			synchronized (this) {
				if (m_handlers == null) {
					try {
						List<PhoenixFilterHandler> handlers = ContainerLoader.getDefaultContainer().lookupList(
						      PhoenixFilterHandler.class);

						m_handlers = new ArrayList<PhoenixFilterHandler>(handlers);
						Collections.sort(m_handlers, new Comparator<PhoenixFilterHandler>() {
							@Override
							public int compare(PhoenixFilterHandler h1, PhoenixFilterHandler h2) {
								int o1 = h1.getOrder();
								int o2 = h2.getOrder();

								if (o1 < 0) {
									o1 += Integer.MAX_VALUE;
								}

								if (o2 < 0) {
									o2 += Integer.MAX_VALUE;
								}

								return o1 - o2;
							}
						});
					} catch (Throwable e) {
						m_exception = e;
					}
				}
			}
		}

		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to configure
	}

	static class Context implements PhoenixFilterContext {
		private HttpServletRequest m_request;

		private HttpServletResponse m_response;

		private FilterChain m_chain;

		private List<PhoenixFilterHandler> m_handlers;

		private int m_index;

		private Map<String, Object> m_attributes = new HashMap<String, Object>();

		public Context(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		      List<PhoenixFilterHandler> handlers) {
			m_request = request;
			m_response = response;
			m_chain = chain;
			m_handlers = handlers;
		}

		@Override
		public void doFilter() throws IOException, ServletException {
			if (m_index < m_handlers.size()) {
				PhoenixFilterHandler handler = m_handlers.get(m_index);

				m_index++;
				handler.handle(this);
			} else { // next filter
				m_chain.doFilter(m_request, m_response);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAttribute(String name) {
			return (T) m_attributes.get(name);
		}

		@Override
		public Set<String> getAttributeNames() {
			return m_attributes.keySet();
		}

		@Override
		public HttpServletRequest getHttpServletRequest() {
			return m_request;
		}

		@Override
		public HttpServletResponse getHttpServletResponse() {
			return m_response;
		}

		@Override
		public boolean hasAttribute(String name) {
			return m_attributes.containsKey(name);
		}

		@Override
		public Object setAttribute(String name, Object value) {
			return m_attributes.put(name, value);
		}
	}
}
