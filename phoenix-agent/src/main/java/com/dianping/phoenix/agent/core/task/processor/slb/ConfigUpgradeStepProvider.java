package com.dianping.phoenix.agent.core.task.processor.slb;

import com.dianping.phoenix.agent.core.task.workflow.Context;

public interface ConfigUpgradeStepProvider {

	int init(Context ctx) throws Exception;

	int checkArgument(Context ctx) throws Exception;

	int copyConfig(Context ctx) throws Exception;

	int gitPull(Context ctx) throws Exception;

	int reloadOrDynamicRefreshConfig(Context ctx) throws Exception;

	int commit(Context ctx) throws Exception;

	int rollback(Context ctx) throws Exception;
}
