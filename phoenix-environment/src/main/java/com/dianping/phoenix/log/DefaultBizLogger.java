package com.dianping.phoenix.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.phoenix.config.ConfigService;
import com.dianping.phoenix.config.ConfigServiceFactory;
import com.dianping.phoenix.environment.PhoenixContext;
import com.site.helper.Splitters;

public class DefaultBizLogger implements BizLogger {
	private static final String FIELD_REQUEST_TIME = "request_time";

	private static final String FIELD_GUID = "s_guid";

	private static final String FIELD_REFER_REQUEST_ID = "refer_req_id";

	private static final String FIELD_REQUEST_ID = "req_id";

	private static final char DELIMITER_ITEMS = '\2';

	private static final char DELIMITER_PAIRS = '\3';

	private static final char DELIMITER_PAIR = '\4';

	private static final char DELIMITER_ITEM = '\5';

	private Logger m_logger;

	private Set<String> m_requiredFields;

	private Map<String, String> m_map = new LinkedHashMap<String, String>();

	@Override
	public BizLogger add(String key, Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		m_map.put(key, format.format(date));
		return this;
	}

	@Override
	public BizLogger add(String key, double value) {
		m_map.put(key, Double.toString(value));
		return this;
	}

	@Override
	public BizLogger add(String key, int value) {
		m_map.put(key, Integer.toString(value));
		return this;
	}

	@Override
	public BizLogger add(String key, List<?> list) {
		StringBuilder sb = new StringBuilder(1024);
		int index = 0;

		for (Object item : list) {
			if (index++ > 0) {
				sb.append(DELIMITER_ITEMS);
			}

			if (item != null) {
				sb.append(escape(item.toString()));
			}
		}

		m_map.put(key, sb.toString());
		return this;
	}

	@Override
	public BizLogger add(String key, long value) {
		m_map.put(key, Long.toString(value));
		return this;
	}

	@Override
	public BizLogger add(String key, Map<?, ?> map) {
		StringBuilder sb = new StringBuilder(1024);
		int index = 0;

		for (Entry<?, ?> e : map.entrySet()) {
			Object k = e.getKey();
			Object v = e.getValue();

			if (index++ > 0) {
				sb.append(DELIMITER_ITEMS);
			}

			if (k != null) {
				sb.append(escape(k.toString()));
			}

			sb.append(DELIMITER_PAIRS);

			if (v != null) {
				sb.append(escape(v.toString()));
			}
		}

		m_map.put(key, sb.toString());
		return this;
	}

	@Override
	public BizLogger add(String key, String value) {
		m_map.put(key, value);

		return this;
	}

	protected String escape(String value) {
		if (value == null) {
			return "";
		}

		int len = value.length();
		StringBuilder sb = new StringBuilder(len);

		for (int i = 0; i < len; i++) {
			char ch = value.charAt(i);

			switch (ch) {
			case DELIMITER_ITEM:
			case DELIMITER_ITEMS:
			case DELIMITER_PAIR:
			case DELIMITER_PAIRS:
				// just ignore it
				break;
			case '\r':
			case '\n':
				// not allowed
				break;
			default:
				sb.append(ch);
				break;
			}
		}

		return sb.toString();
	}

	@Override
	public void flush() {
		StringBuilder sb = new StringBuilder(1024);
		boolean valid = true;

		for (String field : m_requiredFields) {
			String value = m_map.get(field);

			if (value != null) {
				Cat.logEvent("BizLog.RequiredField", field);
				valid = false;
			}
		}

		if (valid) {
			addSystemFields();

			for (Map.Entry<String, String> e : m_map.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();

				if (value != null) { // ignore null value
					if (sb.length() > 0) {
						sb.append(DELIMITER_ITEM);
					}

					sb.append(escape(key)).append(DELIMITER_PAIR).append(escape(value));
				} else {
					Cat.logEvent("BizLog.NoValue", key);
				}
			}

			m_logger.info(sb.toString());
		}

		m_map.clear();
	}

	protected void addSystemFields() {
		PhoenixContext ctx = PhoenixContext.getInstance();

		add(FIELD_REQUEST_TIME, new Date());
		m_map.put(FIELD_REQUEST_ID, ctx.getRequestId());
		m_map.put(FIELD_REFER_REQUEST_ID, ctx.getReferRequestId());
		m_map.put(FIELD_GUID, ctx.getGuid());
	}

	@Override
	public void initialize(String name) {
		ConfigService config = ConfigServiceFactory.getConfig();
		String appName = config.getAppName();
		String str = config.getString(String.format(LogConstants.KEY_BIZ_REQUIRED_FIELDS_PATTERN, appName, name), "");
		List<String> fields = Splitters.by(',').noEmptyItem().trim().split(str);

		m_requiredFields = new HashSet<String>(fields);
		m_logger = Logger.getLogger(name);
	}
}
