package com.dianping.phoenix.servlet;

import java.io.IOException;

import javax.servlet.ServletException;

public interface PhoenixFilterHandler {
	/**
	 * Determines where this filter handler should be placed.
	 * 
	 * @return position value.
	 *         <ul>
	 *         <li>positive value means this filter will be prepended to the head of the chain;</li>
	 *         <li>0 means it will be put in the middle of the chain</li>
	 *         <li>negative value means it will be appended to the end of the chain</li>
	 *         </ul>
	 */
	public int getOrder();

	/**
	 * Handle the HTTP request. The behavior is exactly same as Filter class in the servlet container.
	 * 
	 * @param ctx
	 *           phoenix filter context
	 * @throws IOException
	 * @throws ServletException
	 */
	public void handle(PhoenixFilterContext ctx) throws IOException, ServletException;
}
