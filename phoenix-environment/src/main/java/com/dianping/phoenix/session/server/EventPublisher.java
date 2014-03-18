package com.dianping.phoenix.session.server;

import com.dianping.phoenix.session.RequestEvent;

public interface EventPublisher {

	public void publish(RequestEvent event);

}