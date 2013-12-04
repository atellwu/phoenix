package com.dianping.phoenix.session.requestid;

import java.io.IOException;

import com.dianping.phoenix.session.RequestEvent;

public interface EventRecorder {

	void recordEvent(RequestEvent curEvent, RequestEvent referToEvent) throws IOException;

}
