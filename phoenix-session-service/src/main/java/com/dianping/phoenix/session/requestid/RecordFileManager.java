package com.dianping.phoenix.session.requestid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
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

	@Override
   public void enableLogging(Logger logger) {
		m_logger = logger;
   }

	public BlockingQueue<byte[]> getWriteQueue(long timestamp) throws IOException {
		long startTime = timestamp - timestamp % m_config.getRecordFileTimespan();
		if (!m_writeQueueCache.containsKey(startTime)) {
			BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(m_config.getRecordFileWriteQueueSize());
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tsToFile(timestamp), true));
			m_writeQueueCache.putIfAbsent(startTime, new QueueAndOutputStream(queue, out));
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
		
		start();
   }

	// for unit test only
	public void setConfig(ConfigManager config) {
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
		long startTime = timestamp - timestamp % m_config.getRecordFileTimespan();
		String fileName = String.format("%s-%d", m_ip, startTime);
		return new File(m_config.getRecordFileBaseDir(), fileName);
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
						try {
							entry.getValue().out.close();
						} catch (IOException e) {
							m_logger.error(String.format("Error close output stream of %d", startTimestamp), e);
						}
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

	static class QueueAndOutputStream {
		BlockingQueue<byte[]> queue;
		OutputStream out;

		public QueueAndOutputStream(BlockingQueue<byte[]> queue, OutputStream out) {
			this.queue = queue;
			this.out = out;
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
								// TODO remove flush
								entry.getValue().out.flush();
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
