package com.dianping.phoenix.session.requestid;

import java.io.IOException;

import com.dianping.phoenix.session.RequestEvent;

public interface EventRecorder {

	boolean recordEvent(RequestEvent curEvent, RequestEventEssential referToEvent) throws IOException;

}
