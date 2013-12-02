package com.dianping.phoenix.session.core;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.core.RequestEventRecorder;

public class DummyRecorder implements RequestEventRecorder {

	@Override
	public void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) {
		
	}

}
