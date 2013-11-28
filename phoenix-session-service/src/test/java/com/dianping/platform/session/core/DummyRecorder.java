package com.dianping.platform.session.core;

import com.dianping.phoenix.session.RequestEvent;

public class DummyRecorder implements RequestEventRecorder {

	@Override
	public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
		
	}

}
