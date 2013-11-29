package com.dianping.platform.session.core;

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

import org.apache.log4j.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.NetworkInterfaceManager;

public class RecordFileManager {

	private static Logger logger = Logger.getLogger(RecordFileManager.class);

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
		public void run() {
			while (!stop.get()) {
				for (Map.Entry<Long, QueueAndOutputStream> entry : writeQueueCache.entrySet()) {

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
								logger.error(String.format("Error write record %s to file", new String(buf)), e);
							}
						}
					}
				}

				try {
					Thread.sleep(config.getRecordFileWriteQueueScanInterval());
				} catch (InterruptedException e) {
					logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void shutdown() {
		}

	}

	class CleanTask implements Task {

		@Override
		public void run() {
			while (!stop.get()) {

				for (Map.Entry<Long, QueueAndOutputStream> entry : writeQueueCache.entrySet()) {
					long aliveTime = config.getRecordFileTimespan() * config.getRecordFileWriteStreamMultiply();
					long startTimestamp = entry.getKey();
					if (System.currentTimeMillis() > startTimestamp + aliveTime) {
						logger.info(String.format("Closing stream of %d", startTimestamp));
						writeQueueCache.remove(entry.getKey());
						try {
							entry.getValue().out.close();
						} catch (IOException e) {
							logger.error(String.format("Error close output stream of %d", startTimestamp), e);
						}
					}
				}

				try {
					Thread.sleep(config.getRecordFileWriteStreamCloseScanInterval());
				} catch (InterruptedException e) {
					logger.info("Thread Interrupted, will exit");
					return;
				}
			}
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void shutdown() {
		}

	}

	@Inject
	private ConfigManager config;

	private ConcurrentMap<Long, QueueAndOutputStream> writeQueueCache;

	private AtomicBoolean stop = new AtomicBoolean(false);
	
	private String ip;

	public RecordFileManager(ConfigManager config) {
		this.writeQueueCache = new ConcurrentHashMap<Long, QueueAndOutputStream>();
		this.config = config;
		ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	public BlockingQueue<byte[]> getWriteQueue(long timestamp) throws IOException {
		long startTime = timestamp - timestamp % config.getRecordFileTimespan();
		if (!writeQueueCache.containsKey(startTime)) {
			BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(config.getRecordFileWriteQueueSize());
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tsToFile(timestamp), true));
			writeQueueCache.putIfAbsent(startTime, new QueueAndOutputStream(queue, out));
		}
		return writeQueueCache.get(startTime).queue;
	}

	File tsToFile(long timestamp) {
		long startTime = timestamp - timestamp % config.getRecordFileTimespan();
		String fileName = String.format("%s-%d", ip, startTime);
		return new File(config.getRecordFileBaseDir(), fileName);
	}

	public void start() {
		Threads.forGroup("Phoenix").start(new WriteTask());
		Threads.forGroup("Phoenix").start(new CleanTask());
	}

	public void stop() {
		stop.set(true);
		Threads.forGroup("Phoenix").shutdown();
	}

	ConcurrentMap<Long, QueueAndOutputStream> getWriteQueueCache() {
		return writeQueueCache;
	}

}
