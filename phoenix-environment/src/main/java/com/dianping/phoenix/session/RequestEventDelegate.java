package com.dianping.phoenix.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.net.MessageDelegate;

public class RequestEventDelegate implements MessageDelegate, Initializable, LogEnabled {
	@Inject
	private int m_queueSize = 10000;

	private BlockingQueue<RequestEvent> m_queue;

	private long m_overflowed;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public ChannelBuffer nextMessage(long timeout, TimeUnit unit) throws InterruptedException {
		RequestEvent event = m_queue.poll(timeout, unit);

		if (event == null) {
			return null;
		} else {
			ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(200);

			buffer.writeInt(0);
			writeString(buffer, event.getUserId());
			writeString(buffer, event.getRequestId());
			writeString(buffer, event.getUrlDigest());
			writeString(buffer, event.getRefererUrlDigest());
			buffer.writeLong(event.getTimestamp());
			buffer.writeInt(event.getHop());

			buffer.setInt(0, buffer.writerIndex() - 4);
			return buffer;
		}
	}

	public boolean offer(RequestEvent event) {
		if (!m_queue.offer(event)) {
			m_overflowed++;

			if (m_overflowed == 1 || m_overflowed % m_queue.size() == 0) {
				String name = getClass().getSimpleName();

				m_logger.error(String.format("Queue of %s is full! Overflowed: %s.", name, m_overflowed));
			}

			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onMessageReceived(ChannelBuffer buffer) {
		RequestEvent event = new RequestEvent();

		buffer.readInt(); // get rid of length
		event.setUserId(readString(buffer));
		event.setRequestId(readString(buffer));
		event.setUrlDigest(readString(buffer));
		event.setRefererUrlDigest(readString(buffer));
		event.setTimestamp(buffer.readLong());
		event.setHop(buffer.readInt());

		offer(event);
	}

	private String readString(ChannelBuffer buffer) {
		int len = buffer.readInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return "";
		} else {
			byte[] bytes = new byte[len];

			buffer.readBytes(bytes);

			try {
				return new String(bytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				return new String(bytes);
			}
		}
	}

	public void setQueueSize(int queueSize) {
		m_queueSize = queueSize;
	}

	private void writeString(ChannelBuffer buffer, String str) {
		if (str == null) {
			buffer.writeInt(-1);
		} else if (str.length() == 0) {
			buffer.writeInt(0);
		} else {
			try {
				byte[] bytes = str.getBytes("utf-8");

				buffer.writeInt(bytes.length);
				buffer.writeBytes(bytes);
			} catch (IOException e) {
				byte[] bytes = str.getBytes();

				buffer.writeInt(bytes.length);
				buffer.writeBytes(bytes);
			}
		}
	}

	public RequestEvent take() throws InterruptedException {
		return m_queue.take();
	}

	@Override
	public void initialize() throws InitializationException {
		m_queue = new LinkedBlockingQueue<RequestEvent>(m_queueSize);
	}
	
	public int size() {
		return m_queue.size();
	}
}
