package com.dianping.phoenix.net;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;

public interface MessageDelegate {
	public ChannelBuffer nextMessage(long timeout, TimeUnit unit) throws InterruptedException;

	public void onMessageReceived(ChannelBuffer buffer);
}
