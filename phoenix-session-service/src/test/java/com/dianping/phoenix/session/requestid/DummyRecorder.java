package com.dianping.phoenix.session.requestid;

import com.dianping.phoenix.session.RequestEvent;
import com.dianping.phoenix.session.requestid.EventRecorder;

public class DummyRecorder implements EventRecorder {

	@Override
	public boolean recordEvent(RequestEvent curEvent, RequestEventEssential referToEvent) {
		return true;
	}

}
