package com.dianping.phoenix.agent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

public class AgentTest {
	@Test
	public void testThreadPool() throws Exception {
		AgentMain.attach();

		ObjectHelper.set("test", new StringBuilder());

		ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(new Runnable() {
			@Override
			public void run() {
				StringBuilder sb = ObjectHelper.get("test");

				sb.append("insideJob\n");
			}
		});

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);

		Assert.assertEquals("beforeExecute\ninsideJob\nafterExecute\n", ObjectHelper.get("test").toString());
	}
}
