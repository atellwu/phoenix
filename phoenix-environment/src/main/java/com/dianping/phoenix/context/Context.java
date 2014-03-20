package com.dianping.phoenix.context;

import java.util.Set;

/**
 * Configuration context to store the attributes for the current thread only.
 */
public interface Context {
	/**
	 * Gets the value of the given attribute, returns null if not found.
	 * 
	 * @param name
	 *           attribute name
	 * @return value of the attribute, null if not found.
	 */
	public String getAttribute(String name);

	/**
	 * Gets all context attributes.
	 */
	public Set<String> getAttributeNames();

	/**
	 * Sets the value of the given attribute.
	 * 
	 * @param name
	 *           attribute name
	 * @param value
	 *           value to be set to the attribute
	 */
	public void setAttribute(String name, String value);
}
