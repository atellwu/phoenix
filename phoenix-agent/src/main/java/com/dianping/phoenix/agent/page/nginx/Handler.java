package com.dianping.phoenix.agent.page.nginx;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.ErrorObject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.phoenix.agent.core.Agent;
import com.dianping.phoenix.agent.core.event.EventTracker;
import com.dianping.phoenix.agent.core.task.Task;
import com.dianping.phoenix.agent.core.task.processor.SubmitResult;
import com.dianping.phoenix.agent.core.task.processor.slb.ConfigUpgradeTask;
import com.dianping.phoenix.agent.core.tx.Transaction;
import com.dianping.phoenix.agent.core.tx.TransactionId;
import com.dianping.phoenix.agent.core.tx.TransactionManager;
import com.dianping.phoenix.agent.response.entity.Response;
import com.dianping.phoenix.agent.util.CharacterReplaceFilterWriter;
import com.dianping.phoenix.agent.util.ThreadUtil;

public class Handler implements PageHandler<Context> {
    private final static Logger logger = Logger.getLogger(Handler.class);
    @Inject
    private JspViewer           m_jspViewer;
    @Inject
    private Agent               agent;
    @Inject
    private TransactionManager  txMgr;

    @Override
    @PayloadMeta(Payload.class)
    @InboundActionMeta(name = "nginx")
    public void handleInbound(Context ctx) throws ServletException, IOException {
        // display only, no action here
    }

    @Override
    @OutboundActionMeta(name = "nginx")
    public void handleOutbound(Context ctx) throws ServletException, IOException {

        Payload payload = ctx.getPayload();
        Model model = new Model(ctx);

        if (ctx.hasErrors()) {
            model.setAction(payload.getAction());
            m_jspViewer.view(ctx, model);
            return;
        } else {
            try {
                dispatch(ctx, payload, model);
            } catch (Exception e) {
                logger.error("error handle request", e);
                ctx.addError("internal.error", e);
            }
        }
    }

    private void dispatch(Context ctx, Payload payload, Model model) throws Exception {
        logger.info(String.format("dispatching request %s", payload));

        TransactionId txId = new TransactionId(payload.getDeployId());
        Task task;

        Response res = new Response();

        switch (payload.getAction()) {
            case STATUS:
                if (txMgr.transactionExists(txId)) {
                    Transaction tx = txMgr.loadTransaction(txId);
                    res.setStatus(tx.getStatus().toString().toLowerCase());
                } else {
                    ctx.addError(new ErrorObject("transaction.exists"));
                }
                break;

            case CANCEL:
                agent.cancel(txId);
                res.setStatus("ok");
                break;
            case DEPLOY:
                task = new ConfigUpgradeTask(payload.getVirtualServerName(), payload.getConfigFileName(),
                        payload.getVersion(), payload.getGirUrl(), payload.isReload(), payload.getDynamicRefreshUrl(),
                        payload.getDynamicRefreshPostData(), payload.getDynamicRefreshMethod());
                submitTask(task, txId, res, ctx);
                break;
            case GETLOG:
                Reader logReader = agent.getLogReader(txId, payload.getOffset());
                while (logReader == null && agent.isTransactionProcessing(txId)) {
                    ThreadUtil.sleepQuiet(1000);
                    logReader = agent.getLogReader(txId, payload.getOffset());
                }

                if (logReader != null) {
                    HttpServletResponse resp = ctx.getHttpServletResponse();
                    resp.setHeader("Content-Type", "text/html; charset=UTF-8");
                    Writer writer = resp.getWriter();
                    if (payload.getBr() > 0) {
                        writer = new CharacterReplaceFilterWriter(writer, '\n', "<br/>");
                    }
                    transferLog(txMgr, txId, logReader, writer);
                    return;
                } else {
                    ctx.addError(new ErrorObject("log.notfound"));
                }
                break;

        }

        model.setResponse(res);
        model.setAction(payload.getAction());
        m_jspViewer.view(ctx, model);
    }

    private void submitTask(Task task, TransactionId txId, Response res, Context ctx) throws Exception {
        Transaction tx = new Transaction(task, txId, EventTracker.DUMMY_TRACKER);
        SubmitResult submitResult = agent.submit(tx);
        if (submitResult.isAccepted()) {
            res.setStatus("ok");
        } else {
            ctx.addError(new ErrorObject(submitResult.getReason().toString().toLowerCase()));
        }
    }

    private void transferLog(TransactionManager txMgr, TransactionId txId, Reader logReader, Writer writer)
            throws IOException {
        int len;
        char[] cbuf = new char[4096];
        if (logReader != null) {
            while (true) {
                len = logReader.read(cbuf);
                if (len > 0) {
                    writer.write(cbuf, 0, len);
                    writer.flush();
                } else {
                    Transaction tx = txMgr.loadTransaction(txId);
                    if (tx.getStatus().isCompleted()) {
                        break;
                    } else {
                        ThreadUtil.sleepQuiet(1000);
                    }
                }
            }
        }
    }
}
