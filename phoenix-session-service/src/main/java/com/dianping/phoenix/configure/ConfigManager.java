package com.dianping.phoenix.configure;

import java.io.File;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.configure.entity.Config;
import com.dianping.phoenix.configure.transform.DefaultSaxParser;

public class ConfigManager implements Initializable {

	@Inject
	private String m_configFile = "/data/appdatas/phoenix/session-service/config.xml";

	private Config m_config;

	private void check() {
		if (m_config == null) {
			throw new RuntimeException("ConfigManager is not initialized properly!");
		}
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

	public int getEventExpireTime() {
		check();

		return m_config.getEventExpireTime();
	}

	public int getRetryQueueCleanInterval() {
		check();

		return m_config.getRetryQueueCleanInterval();
	}

	public int getRetryQueueSafeLength() {
		check();

		return m_config.getRetryQueueSafeLength();
	}

	public int getMaxL1CacheSize() {
		check();

		return m_config.getMaxL1CacheSize();
	}

	public int getMaxRetryCacheSize() {
		check();

		return m_config.getMaxRetryCacheSize();
	}

	public int getMaxL2CacheSize() {
		check();

		return m_config.getMaxL2CacheSize();
	}

	public int getRecordFileTimespan() {
		check();

		return m_config.getRecordFileTimespan();
	}

	public int getRecordFileWriteQueueSize() {
		check();

		return m_config.getRecordFileWriteQueueSize();
	}

	public File getRecordFileTmpDir() {
		check();

		return new File(m_config.getRecordFileTmpDir());
	}

	public File getRecordFileTargetDir() {
		check();

		return new File(m_config.getRecordFileTargetDir());
	}

	public int getRecordFileWriteQueueScanInterval() {
		check();

		return m_config.getRecordFileWriteQueueScanInterval();
	}

	public int getRecordFileWriteStreamCloseScanInterval() {
		check();

		return m_config.getRecordFileWriteStreamCloseScanInterval();
	}

	public int getRecordFileWriteStreamMultiply() {
		check();

		return m_config.getRecordFileWriteStreamMultiply();
	}

	public int getHandlerTasksThreads() {
		check();

		return m_config.getHandlerTaskThreads();
	}

	public int getHandlerTaskQueueCapacity() {
		check();

		return m_config.getHandlerTaskQueueCapacity();
	}

}
