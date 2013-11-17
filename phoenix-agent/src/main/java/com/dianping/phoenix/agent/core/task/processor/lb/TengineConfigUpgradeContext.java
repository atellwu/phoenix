package com.dianping.phoenix.agent.core.task.processor.lb;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.phoenix.agent.core.shell.ScriptExecutor;
import com.dianping.phoenix.agent.core.task.Task;
import com.dianping.phoenix.agent.core.task.processor.kernel.DeployTask;
import com.dianping.phoenix.agent.core.task.workflow.Context;

public class TengineConfigUpgradeContext extends Context {
	@Inject
	private ScriptExecutor scriptExecutor;
	@Inject
	private TengineConfigUpgradeStepProvider stepProvider;

	private com.dianping.cat.message.Transaction c_kernelUpgrade;

	public ScriptExecutor getScriptExecutor() {
		return scriptExecutor;
	}

	public TengineConfigUpgradeStepProvider getStepProvider() {
		return stepProvider;
	}

	public com.dianping.cat.message.Transaction getCatTransaction() {
		return c_kernelUpgrade;
	}

	@Override
	public boolean kill() {
		try {
			setKilled(true);
			scriptExecutor.kill();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void setTask(Task task) {
		super.setTask(task);
		TengineConfigUpgradeTask tsk = (TengineConfigUpgradeTask) task;
		c_kernelUpgrade = Cat.getProducer().newTransaction("Tengine",
				String.format("%s::%s", tsk.getVirtualServerName(), tsk.getVersion()));
		try {
			setMsgId(Cat.getProducer().createMessageId());
		} catch (Exception e) {
			setMsgId("no-cat-id");
		}
	}
}
