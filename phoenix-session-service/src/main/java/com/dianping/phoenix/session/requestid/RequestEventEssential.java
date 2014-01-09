package com.dianping.phoenix.session.requestid;

import com.dianping.phoenix.session.RequestEvent;

public class RequestEventEssential {

	private String m_requestId;

	private long m_timestamp;

	public RequestEventEssential(RequestEvent event) {
		m_requestId = event.getRequestId();
		m_timestamp = event.getTimestamp();
	}

	public String getRequestId() {
		return m_requestId;
	}

	public void setRequestId(String requestId) {
		m_requestId = requestId;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "RequestEventEssential [m_requestId=" + m_requestId + ", m_timestamp=" + m_timestamp + "]";
	}

}
