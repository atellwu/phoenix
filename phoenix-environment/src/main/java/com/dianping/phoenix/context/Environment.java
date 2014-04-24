package com.dianping.phoenix.context;

import java.util.Set;

public interface Environment {
	/**
	 * The environment type of application. Possible values are: production, ppe, sandbox, beta, alpha, dev.
	 * <p>
	 * 
	 * It is configured in server.properties file.
	 */
	public String ENV_TYPE = "env.type";

	/**
	 * A name global unique application name. This is a base name for configuration, logging, tracking and other services.
	 * 
	 * For example: user-web, user-service
	 * <p>
	 * 
	 * It is configured in <code>app.properties</code> file.
	 */
	public String APP_NAME = "app.name";

	/**
	 * Base directory of data files, such as configuration files, data files consumed/produced by applications and frameworks etc.
	 * <p>
	 * 
	 * It is configured in <code>server.properties</code> file.
	 */
	public String DATA_BASE_DIR = "data.base.dir";

	/**
	 * Base directory of log, such as application logs, business logs and frameworks logs.
	 * <p>
	 * 
	 * It is configured in <code>server.properties</code> file.
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