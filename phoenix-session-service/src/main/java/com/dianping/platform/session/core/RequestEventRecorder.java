package com.dianping.platform.session.core;

import com.dianping.phoenix.session.RequestEvent;

public interface RequestEventRecorder {

	void recordEvent(RequestEvent curEvent, RequestEvent referToEvent);

}
