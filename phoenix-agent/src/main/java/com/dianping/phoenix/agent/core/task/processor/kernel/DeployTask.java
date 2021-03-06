package com.dianping.phoenix.agent.core.task.processor.kernel;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.phoenix.agent.core.task.AbstractTask;

public class DeployTask extends AbstractTask {

	private String domain;
	private String kernelVersion;
	private String qaServiceUrlPrefix;
	private int qaServiceTimeout;
	private String kernelGitUrl;

	public DeployTask(String domain, String kernelVersion, String kernelGitUrl, String qaServiceUrlPrefix, int qaServiceTimtout) {
		this.domain = domain;
		this.kernelVersion = kernelVersion;
		this.kernelGitUrl = kernelGitUrl;
		this.qaServiceUrlPrefix = qaServiceUrlPrefix;
		this.qaServiceTimeout = qaServiceTimtout;
	}

	/**
	 * for serialization
	 */
	@SuppressWarnings("unused")
	private DeployTask() {
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getKernelVersion() {
		return kernelVersion;
	}

	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}

	public String getQaServiceUrlPrefix() {
		return qaServiceUrlPrefix;
	}

	public void setQaServiceUrlPrefix(String qaServiceUrlPrefix) {
		this.qaServiceUrlPrefix = qaServiceUrlPrefix;
	}

	public int getQaServiceTimeout() {
		return qaServiceTimeout;
	}

	public void setQaServiceTimeout(int qaServiceTimeout) {
		this.qaServiceTimeout = qaServiceTimeout;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((kernelVersion == null) ? 0 : kernelVersion.hashCode());
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
		DeployTask other = (DeployTask) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (kernelVersion == null) {
			if (other.kernelVersion != null)
				return false;
		} else if (!kernelVersion.equals(other.kernelVersion))
			return false;
		return true;
	}

	public String getKernelGitUrl() {
		return kernelGitUrl;
	}

}
