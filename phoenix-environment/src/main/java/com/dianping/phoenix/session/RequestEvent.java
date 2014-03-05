package com.dianping.phoenix.session;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestEvent implements Cloneable {
	private String m_phoenixId;

	private String m_urlDigest;

	private String m_refererUrlDigest;

	private String m_requestId;

	private long m_timestamp;

	private int m_hop;

	@Override
	public RequestEvent clone() throws CloneNotSupportedException {
		RequestEvent clone = new RequestEvent();

		clone.m_hop = m_hop;
		clone.m_refererUrlDigest = m_refererUrlDigest;
		clone.m_requestId = m_requestId;
		clone.m_timestamp = m_timestamp;
		clone.m_urlDigest = m_urlDigest;
		clone.m_phoenixId = m_phoenixId;

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestEvent other = (RequestEvent) obj;
		if (m_refererUrlDigest == null) {
			if (other.m_refererUrlDigest != null)
				return false;
		} else if (!m_refererUrlDigest.equals(other.m_refererUrlDigest))
			return false;
		if (m_requestId == null) {
			if (other.m_requestId != null)
				return false;
		} else if (!m_requestId.equals(other.m_requestId))
			return false;
		if (m_timestamp != other.m_timestamp)
			return false;
		if (m_urlDigest == null) {
			if (other.m_urlDigest != null)
				return false;
		} else if (!m_urlDigest.equals(other.m_urlDigest))
			return false;
		if (m_phoenixId == null) {
			if (other.m_phoenixId != null)
				return false;
		} else if (!m_phoenixId.equals(other.m_phoenixId))
			return false;
		return true;
	}

	public int getHop() {
		return m_hop;
	}

	public String getPhoenixId() {
		return m_phoenixId;
	}

	public String getRefererUrlDigest() {
		return m_refererUrlDigest;
	}

	public String getRequestId() {
		return m_requestId;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public String getUrlDigest() {
		return m_urlDigest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_refererUrlDigest == null) ? 0 : m_refererUrlDigest.hashCode());
		result = prime * result + ((m_requestId == null) ? 0 : m_requestId.hashCode());
		result = prime * result + (int) (m_timestamp ^ (m_timestamp >>> 32));
		result = prime * result + ((m_urlDigest == null) ? 0 : m_urlDigest.hashCode());
		result = prime * result + ((m_phoenixId == null) ? 0 : m_phoenixId.hashCode());
		return result;
	}

	public void setHop(int hop) {
		m_hop = hop;
	}

	public void setPhoenixId(String phoenixId) {
		m_phoenixId = phoenixId;
	}

	public void setRefererUrlDigest(String refererUrlDigest) {
		m_refererUrlDigest = refererUrlDigest;
	}

	public void setRequestId(String requestId) {
		this.m_requestId = requestId;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	public void setUrlDigest(String urlDigest) {
		m_urlDigest = urlDigest;
	}

	public String toString() {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(m_timestamp));

		return String.format(
		      "%s[phoenixId: %s, requestId: %s, urlDigest: %s, refererUrlDigest: %s, timestamp: %s, hop: %s]", getClass()
		            .getSimpleName(), m_phoenixId, m_requestId, m_urlDigest, m_refererUrlDigest, timestamp, m_hop);
	}

}
