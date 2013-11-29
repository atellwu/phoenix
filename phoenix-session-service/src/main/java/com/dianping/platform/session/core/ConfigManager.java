package com.dianping.platform.session.core;

import java.io.File;

public class ConfigManager {

	public int getEventExpireTime() {
		// TODO Auto-generated method stub
		return 5000;
	}

	public int getRetryQueueCleanInterval() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public int getRetryQueueSafeLength() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public long getMaxL1CacheSize() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public long getMaxRetryCacheSize() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public long getMaxL2CacheSize() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public long getRecordFileTimespan() {
		// TODO Auto-generated method stub
		return 1000 * 60 * 5;
	}

	public int getRecordFileWriteQueueSize() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public File getRecordFileBaseDir() {
		return new File("target/record/");
	}

	public int getRecordFileWriteQueueScanInterval() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public int getRecordFileWriteStreamCloseScanInterval() {
		// TODO Auto-generated method stub
		return 1000 * 60;
	}

	public long getRecordFileWriteStreamMultiply() {
		// TODO Auto-generated method stub
		return 3;
	}

}
