/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Dec 9, 2013
 * 
 */
package com.dianping.phoenix.lb.deploy.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;

/**
 * 
 * @author Leo Liang
 * 
 */
public class AgentClientResult {

    private static final DateFormat DATE_FOMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DeployAgentStatus       status        = DeployAgentStatus.PROCESSING;
    private List<String>            logs          = new ArrayList<String>();
    private String                  currentStep;
    private int                     processPct;

    public void logInfo(String msg) {
        logs.add("[" + DATE_FOMATTER.format(new Date()) + "] [INFO] " + msg);
    }

    public void logError(String msg, Throwable e) {
        logs.add("[" + DATE_FOMATTER.format(new Date()) + "] [ERROR] " + msg);
        if (e != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            try {
                logs.addAll(IOUtils.readLines(new ByteArrayInputStream(baos.toByteArray()), "utf-8"));
            } catch (IOException e2) {
                // ignore
            }
        }
    }

    public void logError(String msg) {
        logError(msg, null);
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public int getProcessPct() {
        return processPct;
    }

    public void setProcessPct(int processPct) {
        this.processPct = processPct;
    }

    public DeployAgentStatus getStatus() {
        return status;
    }

    public void setStatus(DeployAgentStatus status) {
        this.status = status;
    }

    public List<String> getLogs() {
        return logs;
    }
}
