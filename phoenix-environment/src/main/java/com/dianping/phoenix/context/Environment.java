package com.dianping.phoenix.context;

import java.util.Set;

public interface Environment {
	/**
	 * A name global unique application name. This is a base name for configuration, logging, tracking and other services.
	 */
	public String APP_NAME = "app.name";

	/**
	 * Base directory of data files, such as configuration files, data files consumed/produced by applications and frameworks etc.
	 */
	public String DATA_BASE_DIR = "data.base.dir";

	/**
	 * Base directory of log, such as application logs, business logs and frameworks logs.
	 */
	public String LOG_BASE_DIR = "log.base.dir";

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