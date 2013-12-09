package com.dianping.phoenix.session.requestid;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.session.RequestEvent;

public class DefaultEventRecorder implements EventRecorder, LogEnabled {

	@Inject
	private RecordFileManager fileMgr;
	private Logger m_logger;

	@Override
	public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) throws IOException {
		BlockingQueue<byte[]> writeQ = fileMgr.getWriteQueue(curEvent.getTimestamp());
		m_logger.info(String.format("Found requestid %s refer to request id %s", curEvent.getRequestId(),
		      referToEvent.getRequestId()));

		writeQ.offer(String.format("%s\t%s\n", curEvent.getRequestId(), referToEvent.getRequestId()).getBytes("ascii"));
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
