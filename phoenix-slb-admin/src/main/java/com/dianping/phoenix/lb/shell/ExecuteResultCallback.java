package com.dianping.phoenix.lb.shell;

public interface ExecuteResultCallback {

	void onProcessCompleted(int exitCode);
	
	void onProcessFailed(Exception e);
	
}
