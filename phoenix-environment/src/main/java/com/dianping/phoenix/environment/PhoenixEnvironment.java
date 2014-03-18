package com.dianping.phoenix.environment;

/**
 * 通用环境容器，将用于存放诸如：本地ip，domain等信息
 * 
 * @author kezhu.wu
 * 
 */
public class PhoenixEnvironment {

	public static final String ENV = "phoenix";

	private String m_requestId;

	private String m_guid;

	public PhoenixEnvironment(String requestId, String guid) {
		m_requestId = requestId;
		m_guid = guid;
	}

	public String getRequestId() {
		return m_requestId;
	}

	public void setRequestId(String requestId) {
		m_requestId = requestId;
	}

	public String getGuid() {
		return m_guid;
	}

	public void setGuid(String guid) {
		m_guid = guid;
	}

}
