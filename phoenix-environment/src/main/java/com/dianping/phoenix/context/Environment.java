package com.dianping.phoenix.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Environment {
	/**
	 * Gets base directory of data files, such as configuration files, data files produced by applications and frameworks etc.
	 * 
	 * @return base directory of data
	 */
	public String getDataBaseDir();

	/**
	 * Gets base directory of log, such as application logs, business logs and frameworks logs.
	 * 
	 * @return base directory of log
	 */
	public String getLogBaseDir();

	/**
	 * Gets property value of given name, return <code>defaultValue</code> if property is not found.
	 * 
	 * @param name
	 *           property name
	 * @param defaultValue
	 *           default value for missing property
	 * @return
	 */
	public String getAttribute(String name, String defaultValue);

	/**
	 * Gets all environment attributes.
	 * 
	 * @return all environment attributes
	 */
	public Map<String, String> getAttributes();

	/**
	 * Loads properties from specified input stream and close the stream. If this method is called multiple times, then some
	 * properties might be overridden by latter calls.
	 * 
	 * @param in
	 *           stream to load properties from
	 */
	public void loadFrom(InputStream in) throws IOException;
	
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