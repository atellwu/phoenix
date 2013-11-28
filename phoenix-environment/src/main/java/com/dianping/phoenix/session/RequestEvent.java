package com.dianping.phoenix.session;

public class RequestEvent {
	private String m_userId;

	private String m_urlDigest;

	private String m_refererUrlDigest;

	private String requestId;

	private long m_timestamp;

	private int m_hop;

	public int getHop() {
		return m_hop;
	}

	public String getRefererUrlDigest() {
		return m_refererUrlDigest;
	}

	public String getRequestId() {
		return requestId;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public String getUrlDigest() {
		return m_urlDigest;
	}

	public String getUserId() {
		return m_userId;
	}

	public void setHop(int hop) {
		m_hop = hop;
	}

	public void setRefererUrlDigest(String refererUrlDigest) {
		m_refererUrlDigest = refererUrlDigest;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	public void setUrlDigest(String urlDigest) {
		m_urlDigest = urlDigest;
	}

	public void setUserId(String userId) {
		m_userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_hop;
		result = prime * result + ((m_refererUrlDigest == null) ? 0 : m_refererUrlDigest.hashCode());
		result = prime * result + (int) (m_timestamp ^ (m_timestamp >>> 32));
		result = prime * result + ((m_urlDigest == null) ? 0 : m_urlDigest.hashCode());
		result = prime * result + ((m_userId == null) ? 0 : m_userId.hashCode());
		result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
		return result;
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
		if (m_hop != other.m_hop)
			return false;
		if (m_refererUrlDigest == null) {
			if (other.m_refererUrlDigest != null)
				return false;
		} else if (!m_refererUrlDigest.equals(other.m_refererUrlDigest))
			return false;
		if (m_timestamp != other.m_timestamp)
			return false;
		if (m_urlDigest == null) {
			if (other.m_urlDigest != null)
				return false;
		} else if (!m_urlDigest.equals(other.m_urlDigest))
			return false;
		if (m_userId == null) {
			if (other.m_userId != null)
				return false;
		} else if (!m_userId.equals(other.m_userId))
			return false;
		if (requestId == null) {
			if (other.requestId != null)
				return false;
		} else if (!requestId.equals(other.requestId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RequestEvent [m_userId=" + m_userId + ", m_urlDigest=" + m_urlDigest + ", m_refererUrlDigest="
				+ m_refererUrlDigest + ", requestId=" + requestId + ", m_timestamp=" + m_timestamp + ", m_hop=" + m_hop
				+ "]";
	}

}
