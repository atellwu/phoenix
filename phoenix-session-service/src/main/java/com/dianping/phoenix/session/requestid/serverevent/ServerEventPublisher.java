package com.dianping.phoenix.session.requestid.serverevent;

import com.dianping.phoenix.session.RequestEvent;

public interface ServerEventPublisher {

	public void publish(RequestEvent event);

}