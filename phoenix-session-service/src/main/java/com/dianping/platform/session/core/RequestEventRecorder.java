package com.dianping.platform.session.core;

import java.io.IOException;

import com.dianping.phoenix.session.RequestEvent;

public interface RequestEventRecorder {

	void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) throws IOException;

}
