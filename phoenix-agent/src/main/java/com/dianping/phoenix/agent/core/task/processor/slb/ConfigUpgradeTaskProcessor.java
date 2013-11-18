package com.dianping.phoenix.agent.core.task.processor.slb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.unidal.lookup.annotation.Inject;

import com.dianping.phoenix.agent.core.event.MessageEvent;
import com.dianping.phoenix.agent.core.task.processor.AbstractSerialTaskProcessor;
import com.dianping.phoenix.agent.core.task.workflow.Context;
import com.dianping.phoenix.agent.core.task.workflow.Engine;
import com.dianping.phoenix.agent.core.task.workflow.Step;
import com.dianping.phoenix.agent.core.tx.LogFormatter;
import com.dianping.phoenix.agent.core.tx.Transaction;
import com.dianping.phoenix.agent.core.tx.Transaction.Status;
import com.dianping.phoenix.agent.core.tx.TransactionId;

public class ConfigUpgradeTaskProcessor extends AbstractSerialTaskProcessor<ConfigUpgradeTask> {

    private final static Logger  logger        = Logger.getLogger(ConfigUpgradeTaskProcessor.class);

    @Inject
    Engine                       engine;

    @Inject
    LogFormatter                 logFormatter;

    AtomicReference<Transaction> currentTxRef  = new AtomicReference<Transaction>();
    AtomicReference<Context>     currentCtxRef = new AtomicReference<Context>();

    @Override
    protected Status doTransaction(final Transaction tx) throws IOException {

        currentTxRef.set(tx);
        ConfigUpgradeTask task = (ConfigUpgradeTask) tx.getTask();

        eventTrackerChain.onEvent(new MessageEvent(tx.getTxId(), String.format("updating %s to version %s",
                task.getVirtualServerName(), task.getVersion())));
        OutputStream stdOut = txMgr.getLogOutputStream(tx.getTxId());
        Context ctx = lookup(Context.class, "tengine_ctx");
        ctx.setLogOut(stdOut);
        ctx.setLogFormatter(logFormatter);
        ctx.setTask(task);
        currentCtxRef.set(ctx);

        Status exitStatus = Status.SUCCESS;
        try {
            exitStatus = updateTengine(ctx);
        } catch (Exception e) {
            logger.error("error update tengine", e);
            exitStatus = Status.FAILED;
        } finally {
            IOUtil.close(stdOut);
        }
        return exitStatus;
    }

    private Status updateTengine(Context ctx) throws Exception {
        int exitCode = engine.start(ConfigUpgradeStep.START, ctx);
        if (exitCode == Step.CODE_OK) {
            return Status.SUCCESS;
        } else {
            return Status.FAILED;
        }
    }

    @Override
    public boolean cancel(TransactionId txId) {
        Transaction currentTx = currentTxRef.get();
        if (currentTx != null && currentTx.getTxId().equals(txId)) {
            if (engine.kill(currentCtxRef.get())) {
                currentTx.setStatus(Status.KILLED);
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<ConfigUpgradeTask> handle() {
        return ConfigUpgradeTask.class;
    }

}
