package com.dianping.phoenix.session.core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.session.RequestEvent;

public class FileRecorder implements RequestEventRecorder {

	@Inject
	private RecordFileManager fileMgr;

	@Override
	public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) throws IOException {
		BlockingQueue<byte[]> writeQ = fileMgr.getWriteQueue(curEvent.getTimestamp());
		writeQ.offer(String.format("%s\t%s\n", curEvent.getRequestId(), referToEvent.getRequestId()).getBytes("ascii"));
	}
}
