package com.dianping.phoenix.log;

public interface LogConstants {
	public String DEFAULT_VALUE_BIZ_CONVERSION_PATTERN = "%m%n";

	public String DEFAULT_VALUE_APP_CONVERSION_PATTERN = "%d %-5p [%t] (%F:%L) -- %m%n";

	public String DEFAULT_VALUE_DATE_FORMAT = ".yyyy-MM-dd";

	public String KEY_BIZ_CONVERSION_PATTERN = "log[biz].conversionPattern";

	public String KEY_APP_CONVERSION_PATTERN = "log[app].conversionPattern";

	public String KEY_BIZ_REQUIRED_FIELDS_PATTERN = "log[biz].requiredFields.%s.%s"; // appName, name

}
