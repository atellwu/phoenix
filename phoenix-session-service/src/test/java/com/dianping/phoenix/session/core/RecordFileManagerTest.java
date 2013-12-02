package com.dianping.phoenix.session.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.phoenix.session.core.ConfigManager;
import com.dianping.phoenix.session.core.RecordFileManager;

public class RecordFileManagerTest extends ComponentTestCase {

	private RecordFileManager mgr;

	@Before
	public void before() throws Exception {
		ConfigManager config = new ConfigManager();
		mgr = new RecordFileManager(config);
		File recordBaseDir = config.getRecordFileBaseDir();
		if (recordBaseDir.exists() && recordBaseDir.isDirectory()) {
			FileUtils.deleteDirectory(recordBaseDir);
		}
		recordBaseDir.mkdir();
	}

	@After
	public void after() throws Exception {
		mgr.stop();
	}

	@Test
	public void shouldGetWriteQueue() throws Exception {
		mgr.start();

		BlockingQueue<byte[]> queue = mgr.getWriteQueue(System.currentTimeMillis());

		assertNotNull(queue);
	}

	@Test
	public void shouldWriteToFile() throws Exception {
		mgr = new RecordFileManager(new ConfigManager() {

			@Override
			public int getRecordFileWriteQueueScanInterval() {
				return 100;
			}

		});
		mgr.start();

		long timestamp = System.currentTimeMillis();
		BlockingQueue<byte[]> queue = mgr.getWriteQueue(timestamp);

		String record = "1\t2\n";
		queue.offer(record.getBytes("ascii"));

		long start = timestamp;
		boolean contentEqual = false;
		while (System.currentTimeMillis() - start < 1000) {
			if (IOUtils.toString(new FileInputStream(mgr.tsToFile(timestamp))).equals(record)) {
				contentEqual = true;
				break;
			}
			Thread.sleep(100);
		}

		assertTrue(contentEqual);
	}

	@Test
	public void shouldWriteToSameFile() throws Exception {
		mgr = new RecordFileManager(new ConfigManager() {

			@Override
			public int getRecordFileWriteQueueScanInterval() {
				return 100;
			}

		});
		mgr.start();

		long timestamp = System.currentTimeMillis();
		BlockingQueue<byte[]> queue = mgr.getWriteQueue(timestamp);

		String record1 = "1\t2\n";
		queue.offer(record1.getBytes("ascii"));
		String record2 = "3\t4\n";
		queue.offer(record2.getBytes("ascii"));

		long start = timestamp;
		boolean contentEqual = false;
		while (System.currentTimeMillis() - start < 1000) {
			if (IOUtils.toString(new FileInputStream(mgr.tsToFile(timestamp))).equals(record1 + record2)) {
				contentEqual = true;
				break;
			}
			Thread.sleep(100);
		}

		assertTrue(contentEqual);
	}

	@Test
	public void shouldWriteToDifferentFile() throws Exception {
		mgr = new RecordFileManager(new ConfigManager() {

			@Override
			public int getRecordFileWriteQueueScanInterval() {
				return 100;
			}

			@Override
			public long getRecordFileTimespan() {
				return 1;
			}
			
		});
		mgr.start();

		long timestamp1 = System.currentTimeMillis();
		BlockingQueue<byte[]> queue1 = mgr.getWriteQueue(timestamp1);
		String record1 = "1\t2\n";
		queue1.offer(record1.getBytes("ascii"));
		
		Thread.sleep(10);
		
		long timestamp2 = System.currentTimeMillis();
		BlockingQueue<byte[]> queue2 = mgr.getWriteQueue(timestamp2);
		String record2 = "3\t4\n";
		queue2.offer(record2.getBytes("ascii"));

		long start = timestamp1;
		String actualContent1 = null;
		String actualContent2 = null;
		boolean content1Equal = false;
		boolean content2Equal = false;
		while (System.currentTimeMillis() - start < 2000) {
			actualContent1 = IOUtils.toString(new FileInputStream(mgr.tsToFile(timestamp1)));
			if (actualContent1.equals(record1)) {
				content1Equal = true;
			}
			
			actualContent2 = IOUtils.toString(new FileInputStream(mgr.tsToFile(timestamp2)));
			if (actualContent2.equals(record2)) {
				content2Equal = true;
			}
			if(content1Equal && content2Equal) {
				break;
			}
			Thread.sleep(100);
		}

		assertEquals(record1, actualContent1);
		assertEquals(record2, actualContent2);
	}

	@Test
	public void shouldCloseFile() throws Exception {
		mgr = new RecordFileManager(new ConfigManager() {

			@Override
			public long getRecordFileTimespan() {
				return 1000;
			}

			@Override
			public long getRecordFileWriteStreamMultiply() {
				return 1;
			}

			@Override
			public int getRecordFileWriteStreamCloseScanInterval() {
				return 10;
			}

		});
		mgr.start();

		long timestamp = System.currentTimeMillis();
		BlockingQueue<byte[]> queue = mgr.getWriteQueue(timestamp);

		String record = "1\t2\n";
		queue.offer(record.getBytes("ascii"));

		long start = timestamp;
		boolean fileClosed = false;
		while (System.currentTimeMillis() - start < 1500) {
			if (mgr.getWriteQueueCache().size() == 0) {
				fileClosed = true;
				break;
			}
			Thread.sleep(100);
		}

		assertTrue(fileClosed);
	}

}
