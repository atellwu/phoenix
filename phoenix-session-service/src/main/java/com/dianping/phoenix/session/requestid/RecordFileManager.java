package com.dianping.phoenix.session.requestid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.phoenix.configure.ConfigManager;

public class RecordFileManager implements Initializable, LogEnabled {
	@Inject
	private ConfigManager m_config;

	private ConcurrentMap<Long, QueueAndOutputStream> m_writeQueueCache;

	private AtomicBoolean m_stop = new AtomicBoolean(false);

	private String m_ip;

	private Logger m_logger;
	
	public static final String PHOENIX_MODE = "PHOENIX_MODE";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public BlockingQueue<byte[]> getWriteQueue(long timestamp) throws IOException {
		long startTime = timestamp - timestamp % m_config.getRecordFileTimespan();

		if (!m_writeQueueCache.containsKey(startTime)) {
			BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(m_config.getRecordFileWriteQueueSize());
			String tmpFile = tsToFileName(timestamp);

			m_writeQueueCache.putIfAbsent(startTime, new QueueAndOutputStream(queue, tmpFile));
		}

		return m_writeQueueCache.get(startTime).queue;
	}

	ConcurrentMap<Long, QueueAndOutputStream> getWriteQueueCache() {
		return m_writeQueueCache;
	}

	@Override
	public void initialize() throws InitializationException {
		this.m_writeQueueCache = new ConcurrentHashMap<Long, QueueAndOutputStream>();
		m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	// for unit test only
	void setConfig(ConfigManager config) {
		m_config = config;
	}

	public void start() {
		Threads.forGroup("Phoenix").start(new WriteTask());
		Threads.forGroup("Phoenix").start(new CleanTask());
	}

	public void stop() {
		m_stop.set(true);
		Threads.forGroup("Phoenix").shutdown();
	}

	File tsToFile(long timestamp) {
		return new File(m_config.getRecordFileTmpDir(), tsToFileName(timestamp));
	}

	String tsToFileName(long timestamp) {
		long startTime = timestamp - timestamp % m_config.getRecordFileTimespan();
		MessageFormat format = new MessageFormat("requestid/{0,date,yyyy/MM/dd/HH}/{1}-{0,date,mmss}");
		String fileName = format.format(new Object[] { new Date(startTime), m_ip });

		return fileName;
	}

	class CleanTask implements Task {

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!m_stop.get()) {

				for (Map.Entry<Long, QueueAndOutputStream> entry : m_writeQueueCache.entrySet()) {
					long aliveTime = m_config.getRecordFileTimespan() * m_config.getRecordFileWriteStreamMultiply();
					long startTimestamp = entry.getKey();
					if (System.currentTimeMillis() > startTimestamp + aliveTime) {
						m_logger.info(String.format("Closing stream of %d", startTimestamp));
						m_writeQueueCache.remove(entry.getKey());
						QueueAndOutputStream queueAndStream = entry.getValue();

						queueAndStream.close();
						queueAndStream.moveTmpFileToTarget();
					}
				}

				try {
					Thread.sleep(m_config.getRecordFileWriteStreamCloseScanInterval());
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	class QueueAndOutputStream {
		private BlockingQueue<byte[]> queue;

		private OutputStream out;

		private File file;

		private String path;

		public QueueAndOutputStream(BlockingQueue<byte[]> queue, String path) throws IOException {
			this.queue = queue;
			this.path = path;
			this.file = new File(m_config.getRecordFileTmpDir(), path);

			if(!this.file.getParentFile().mkdirs()) {
				throw new RuntimeException("Can not create directory " + file.getParentFile().getCanonicalPath());
			}

			this.out = new BufferedOutputStream(new FileOutputStream(this.file, true));
		}

		public void close() {
			try {
				this.out.close();
			} catch (IOException e) {
				m_logger.error(String.format("Error when closing file(%s)!", this.file), e);
			}
		}

		public void moveTmpFileToTarget() {
			File targetFile = new File(m_config.getRecordFileTargetDir(), this.path);

			targetFile.getParentFile().mkdirs();

			if (targetFile.exists()) {
				File newTargetFile = new File(m_config.getRecordFileTargetDir(), this.path + "-"
				      + UUID.randomUUID().toString());
				m_logger.warn(String.format("Target file %s already exists, will rename to %s", targetFile, newTargetFile));
				targetFile = newTargetFile;
			}

			if (!this.file.renameTo(targetFile)) {
				m_logger.error(String.format("Can not move %s to %s", this.file.getAbsolutePath(),
				      targetFile.getAbsolutePath()));
			}
		}
	}

	class WriteTask implements Task {

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void run() {
			while (!m_stop.get()) {
				for (Map.Entry<Long, QueueAndOutputStream> entry : m_writeQueueCache.entrySet()) {

					BlockingQueue<byte[]> queue = entry.getValue().queue;
					while (true) {
						byte[] buf = queue.poll();
						if (buf == null) {
							break;
						} else {
							try {
								entry.getValue().out.write(buf);
								if ("dev".equals(System.getProperty(PHOENIX_MODE))) {
									entry.getValue().out.flush();
								}
							} catch (Exception e) {
								m_logger.error(String.format("Error write record %s to file", new String(buf)), e);
							}
						}
					}
				}

				try {
					Thread.sleep(m_config.getRecordFileWriteQueueScanInterval());
				} catch (InterruptedException e) {
					m_logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}

}
