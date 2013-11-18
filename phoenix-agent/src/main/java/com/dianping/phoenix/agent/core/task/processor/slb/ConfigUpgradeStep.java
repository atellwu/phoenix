package com.dianping.phoenix.agent.core.task.processor.slb;

import java.util.Map;

import com.dianping.cat.message.Message;
import com.dianping.phoenix.agent.core.task.workflow.AbstractStep;
import com.dianping.phoenix.agent.core.task.workflow.Context;
import com.dianping.phoenix.agent.core.task.workflow.Step;

public class ConfigUpgradeStep extends AbstractStep {

	protected ConfigUpgradeStep(AbstractStep nextStepWhenSuccess, AbstractStep nextStepWhenFail, int stepSeq) {
		super(nextStepWhenSuccess, nextStepWhenFail, stepSeq);
	}

	private static ConfigUpgradeStep FAILED = new ConfigUpgradeStep(null, null, 8) {
		@Override
		public int doStep(Context ctx) throws Exception {
			ConfigUpgradeContext myCtx = (ConfigUpgradeContext) ctx;
			myCtx.setEndStep(FAILED);
			myCtx.setExitCode(Step.CODE_ERROR);
			com.dianping.cat.message.Transaction trans = myCtx.getCatTransaction();
			if (trans != null) {
				trans.setStatus(STATUS_FAIL);
				trans.complete();
			}
			return Step.CODE_ERROR;
		}

		@Override
		public Map<String, String> getLogChunkHeader() {
			Map<String, String> header = super.getLogChunkHeader();
			header.put(HEADER_STATUS, STATUS_FAIL);
			return header;
		}

		@Override
		public String toString() {
			return "FAILED";
		}

	};

	private static ConfigUpgradeStep SUCCESS = new ConfigUpgradeStep(null, null, 8) {
		@Override
		public int doStep(Context ctx) throws Exception {
			ConfigUpgradeContext myCtx = (ConfigUpgradeContext) ctx;
			myCtx.setEndStep(SUCCESS);
			myCtx.setExitCode(Step.CODE_OK);
			com.dianping.cat.message.Transaction trans = myCtx.getCatTransaction();
			if (trans != null) {
				trans.setStatus(Message.SUCCESS);
				trans.complete();
			}
			return Step.CODE_OK;
		}

		@Override
		public Map<String, String> getLogChunkHeader() {
			Map<String, String> header = super.getLogChunkHeader();
			header.put(HEADER_STATUS, STATUS_SUCCESS);
			return header;
		}

		@Override
		public String toString() {
			return "SUCCESS";
		}
	};

	private static ConfigUpgradeStep ROLLBACK = new ConfigUpgradeStep(FAILED, FAILED, 7) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).rollback(ctx);
		}

		@Override
		public String toString() {
			return "ROLLBACK";
		}
	};

	private static ConfigUpgradeStep COMMIT = new ConfigUpgradeStep(SUCCESS, FAILED, 6) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).commit(ctx);
		}

		@Override
		public String toString() {
			return "COMMIT";
		}
	};

	private static ConfigUpgradeStep RELOAD_OR_DYNAMIC_REFRESH_CONFIG = new ConfigUpgradeStep(COMMIT, ROLLBACK, 5) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).reloadOrDynamicRefreshConfig(ctx);
		}

		@Override
		public String toString() {
			return "RELOAD_OR_DYNAMIC_REFRESH_CONFIG";
		}
	};

	private static ConfigUpgradeStep COPY_CONFIG = new ConfigUpgradeStep(RELOAD_OR_DYNAMIC_REFRESH_CONFIG, ROLLBACK, 4) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).copyConfig(ctx);
		}

		@Override
		public String toString() {
			return "COPY_CONFIG";
		}
	};
	
	private static ConfigUpgradeStep GIT_PULL = new ConfigUpgradeStep(COPY_CONFIG, FAILED, 3) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).gitPull(ctx);
		}

		@Override
		public String toString() {
			return "GIT_PULL";
		}
	};

	private static ConfigUpgradeStep CHECK_ARGUMENT = new ConfigUpgradeStep(GIT_PULL, FAILED, 2) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).checkArgument(ctx);
		}

		@Override
		public String toString() {
			return "CHECK_ARGUMENT";
		}
	};

	private static ConfigUpgradeStep INIT = new ConfigUpgradeStep(CHECK_ARGUMENT, FAILED, 1) {

		@Override
		protected int doActivity(Context ctx) throws Exception {
			return getStepProvider(ctx).init(ctx);
		}

		@Override
		public String toString() {
			return "INIT";
		}
	};

	public static ConfigUpgradeStep START = new ConfigUpgradeStep(INIT, FAILED, 0) {
		@Override
		public int doStep(Context ctx) throws Exception {
			return doActivity(ctx);
		}

		@Override
		public String toString() {
			return "START";
		}
	};

	private static ConfigUpgradeStepProvider getStepProvider(Context ctx) {
		return ((ConfigUpgradeContext) ctx).getStepProvider();
	}

	@Override
	public int doStep(Context ctx) throws Exception {
		return doStepWithCat(ctx, "TengineConfigUpgrade", toString());
	}

	@Override
	protected int getTotalStep() {
		return 8;
	}

	@Override
	protected int doActivity(Context ctx) throws Exception {
		return Step.CODE_OK;
	}
}
