package com.dianping.phoenix.configure;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.configure.entity.Config;
import com.dianping.phoenix.configure.entity.Hdfs;
import com.dianping.phoenix.configure.entity.Property;
import com.dianping.phoenix.configure.transform.DefaultSaxParser;

public class ConfigManager implements Initializable {
	@Inject
	private String m_configFile = "/data/appdatas/phoenix/session.xml";

	private Config m_config;

	public int getEventExpireTime() {
		return m_config.getEventExpireTime();
	}

	public int getHandlerTaskQueueCapacity() {
		return m_config.getHandlerTaskQueueCapacity();
	}

	public int getHandlerTasksThreads() {
		return m_config.getHandlerTaskThreads();
	}

	public String getHdfsLocalBaseDir() {
		return m_config.getHdfs().getLocalBaseDir();
	}

	public Map<String, String> getHdfsProperties() {
		Map<String, String> properties = new HashMap<String, String>();

		for (Property property : m_config.getHdfs().getProperties()) {
			properties.put(property.getName(), property.getValue());
		}

		return properties;
	}

	public String getHdfsServerUri() {
		return m_config.getHdfs().getServerUri();
	}

	public int getMaxL1CacheSize() {
		return m_config.getMaxL1CacheSize();
	}

	public int getMaxL2CacheSize() {
		return m_config.getMaxL2CacheSize();
	}

	public int getMaxRetryCacheSize() {
		return m_config.getMaxRetryCacheSize();
	}

	public File getRecordFileTargetDir() {
		return new File(m_config.getRecordFileTargetDir());
	}

	public int getRecordFileTimespan() {
		return m_config.getRecordFileTimespan();
	}

	public File getRecordFileTmpDir() {
		return new File(m_config.getRecordFileTmpDir());
	}

	public int getRecordFileWriteQueueScanInterval() {
		return m_config.getRecordFileWriteQueueScanInterval();
	}

	public int getRecordFileWriteQueueSize() {
		return m_config.getRecordFileWriteQueueSize();
	}

	public int getRecordFileWriteStreamCloseScanInterval() {
		return m_config.getRecordFileWriteStreamCloseScanInterval();
	}

	public int getRecordFileWriteStreamMultiply() {
		return m_config.getRecordFileWriteStreamMultiply();
	}

	public int getRetryQueueCleanInterval() {
		return m_config.getRetryQueueCleanInterval();
	}

	public int getRetryQueueSafeLength() {
		return m_config.getRetryQueueSafeLength();
	}
	
	public String getServerListUpdateUrl() {
		return m_config.getServerListUpdateUrl();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			File file = new File(m_configFile);

			if (file.isFile()) {
				String content = Files.forIO().readFrom(file, "utf-8");

				m_config = DefaultSaxParser.parse(content);
			} else {
				m_config = new Config();
			}

			if (m_config.getHdfs() == null) {
				m_config.setHdfs(new Hdfs());
			}
		} catch (Exception e) {
			throw new InitializationException(String.format("Unable to load configuration file(%s)!", m_configFile), e);
		}

		File recordFileTmpDir = new File(m_config.getRecordFileTmpDir());
		if (!recordFileTmpDir.exists()) {
			recordFileTmpDir.mkdirs();
		}

		File recordFileTargetDir = new File(m_config.getRecordFileTargetDir());
		if (!recordFileTargetDir.exists()) {
			recordFileTargetDir.mkdirs();
		}
	}
}
