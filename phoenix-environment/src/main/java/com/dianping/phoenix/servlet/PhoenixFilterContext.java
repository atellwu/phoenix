package com.dianping.phoenix.servlet;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PhoenixFilterContext {
	public void doFilter() throws IOException, ServletException;

	public HttpServletRequest getHttpServletRequest();

	public HttpServletResponse getHttpServletResponse();

	public <T> T getAttribute(String name);

	public Set<String> getAttributeNames();

	public boolean hasAttribute(String name);

	public Object setAttribute(String name, Object value);
}
