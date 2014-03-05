//package com.dianping.phoenix.lb.deploy.executor;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.dianping.phoenix.lb.configure.ConfigManager;
//import com.dianping.phoenix.lb.deploy.agent.AgentClient;
//import com.dianping.phoenix.lb.deploy.agent.AgentClientResult;
//import com.dianping.phoenix.lb.deploy.agent.DefaultAgentClient;
//import com.dianping.phoenix.lb.deploy.bo.DeployAgentBo;
//import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
//import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;
//import com.dianping.phoenix.lb.deploy.model.AgentBatch;
//import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;
//import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
//import com.dianping.phoenix.lb.deploy.model.DeployVs;
//import com.dianping.phoenix.lb.deploy.model.DeployVsStatus;
//import com.dianping.phoenix.lb.deploy.model.ErrorPolicy;
//import com.dianping.phoenix.lb.deploy.service.AgentSequenceService;
//import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
//import com.dianping.phoenix.lb.service.model.StrategyService;
//import com.dianping.phoenix.lb.service.model.VirtualServerService;
//
//public class CopyOfDefaultTaskExecutor implements TaskExecutor {
//
//    private static final Logger        LOG        = LoggerFactory.getLogger(CopyOfDefaultTaskExecutor.class);
//
//    private static final String        pattern    = "mm-dd hh:MM:ss";
//
//    private final DeployTaskBo         deployTaskBo;
//
//    private final DeployTaskService    deployTaskService;
//
//    private final VirtualServerService virtualServerService;
//
//    private final StrategyService      strategyService;
//
//    private final AgentSequenceService agentSequenceService;
//
//    private final ConfigManager        configManager;
//
//    private final boolean              autoContinue;
//
//    private final Integer              deployInterval;
//
//    private final AgentBatch           agentBatch;
//
//    private final ErrorPolicy          errorPolicy;
//
//    private final ReentrantLock        actionLock = new ReentrantLock();
//
//    private Thread                     taskThread;
//
//    private ExecutorService            executor;
//
//    public CopyOfDefaultTaskExecutor(DeployTaskBo deployTaskBo, DeployTaskService deployTaskService, VirtualServerService virtualServerService, StrategyService strategyService,
//            AgentSequenceService agentSequenceService, ConfigManager configManager) {
//        this.deployTaskBo = deployTaskBo;
//
//        this.deployTaskService = deployTaskService;
//        this.virtualServerService = virtualServerService;
//        this.strategyService = strategyService;
//        this.agentSequenceService = agentSequenceService;
//        this.configManager = configManager;
//
//        this.autoContinue = deployTaskBo.getTask().getAutoContinue();
//        this.deployInterval = deployTaskBo.getTask().getDeployInterval();
//        this.agentBatch = deployTaskBo.getTask().getAgentBatch();
//        this.errorPolicy = deployTaskBo.getTask().getErrorPolicy();
//
//    }
//
//    private class InnerTask implements Runnable {
//
//        @Override
//        public void run() {
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " running.");
//
//            //task启动了，更新状态为PROCESSING
//            deployTaskBo.getTask().setStatus(DeployTaskStatus.PROCESSING);
//
//            // 每次启动，都使用新的agentId
//            deployTaskBo.setAgentId(agentSequenceService.getAgentId());
//
//            executor = Executors.newCachedThreadPool();
//
//            //获取接下来要发布的vs和agent
//            DeployVsBo deployVsBo = null;
//            while ((deployVsBo = nextReadyDeployVs(deployTaskBo)) != null && !Thread.currentThread().isInterrupted()) {
//                //vs启动了，更新状态为PROCESSING
//                deployVsBo.getDeployVs().setStatus(DeployVsStatus.PROCESSING);
//
//                Map<String, DeployAgentBo> deployAgentBos = null;
//                while ((deployAgentBos = nextReadyDeployAgents(deployVsBo, agentBatch.getBatchSize())).size() > 0 && !Thread.currentThread().isInterrupted()) {
//                    try {
//                        execute(deployVsBo, deployAgentBos);
//                    } catch (InterruptedException e) {
//                        // 运行Task的主线程中被中断了(应该是被暂停或取消了，此处不要管，结束该方法即可)
//                        Thread.currentThread().interrupt();
//                    }
//                }
//                //执行完一个vs(或者因为暂停/取消而退出)，需要更新一下vs的状态
//                autoUpdateVsStatusByChildren(deployVsBo);
//            }
//
//            //执行完所有vs(或者因为暂停/取消而退出)，需要更新一下task的状态
//            autoUpdateTaskStatusByChildren(deployTaskBo);
//
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " done");
//        }
//    }
//
//    private DeployVsBo nextReadyDeployVs(DeployTaskBo deployTaskBo) {
//        Map<String, DeployVsBo> deployVsBos = deployTaskBo.getDeployVsBos();
//        DeployVsBo vsToBeDeploy = null;
//        Set<String> vsNameSet = deployVsBos.keySet();
//        for (String vsName : vsNameSet) {
//            DeployVsBo deployVsBo = deployVsBos.get(vsName);
//            DeployVs deployVs = deployVsBo.getDeployVs();
//            //顺序遍历vs,获取到第一个需要执行的vs。
//            if (deployVs.getStatus() == DeployVsStatus.READY) {
//                vsToBeDeploy = deployVsBo;
//                break;
//            }
//        }
//        return vsToBeDeploy;
//    }
//
//    private Map<String, DeployAgentBo> nextReadyDeployAgents(DeployVsBo deployVsBo, int n) {
//        Map<String, DeployAgentBo> re = new HashMap<String, DeployAgentBo>();
//        Map<String, DeployAgentBo> deployAgentBos = deployVsBo.getDeployAgentBos();
//        Set<String> hostNameSet = deployAgentBos.keySet();
//        //顺序遍历agent,获取到第一个需要执行的agent。
//        int index = 0;
//        for (String ip : hostNameSet) {
//            index++;
//            DeployAgentBo deployAgentBo = deployAgentBos.get(ip);
//            if (deployAgentBo.getDeployAgent().getStatus() == DeployAgentStatus.READY) {
//                re.put(ip, deployAgentBo);
//                if (index == 1) {//如果要执行的是第一个agent，那么只执行这一个agent。因为第一次都是执行第一台的。
//                    break;
//                }
//                if (re.size() >= n) {
//                    break;
//                }
//            }
//        }
//        return re;
//    }
//
//    private void execute(DeployVsBo deployVsBo, Map<String, DeployAgentBo> deployAgentBos) throws InterruptedException {
//        //创建Agent执行者
//        Map<String, AgentClient> agentClients = new HashMap<String, AgentClient>();
//        for (DeployAgentBo deployAgentBo : deployAgentBos.values()) {
//            long agentId = deployTaskBo.getAgentId();
//            String vsName = deployVsBo.getDeployVs().getVsName();
//            String vsTag = deployVsBo.getDeployVs().getVsTag();
//            String ip = deployAgentBo.getDeployAgent().getIpAddress();
//            DefaultAgentClient agentClient = new DefaultAgentClient(agentId, vsName, vsTag, ip, virtualServerService, strategyService, configManager);
//            agentClients.put(ip, agentClient);
//        }
//
//        //多线程执行agent执行者
//        CountDownLatch doneSignal = new CountDownLatch(agentClients.size());
//        for (Map.Entry<String, AgentClient> entry : agentClients.entrySet()) {
//            String ip = entry.getKey();
//            AgentClient agentClient = entry.getValue();
//            executor.execute(new AgentTask(agentClient, doneSignal, deployVsBo.getDeployVs(), ip));
//        }
//        //执行的过程中，所有状态，需要反馈过去，包括持久花到数据库
//        while (doneSignal.getCount() > 0) {
//            //获取agent的执行状态，设置到deployTaskBo中(deployTaskBo含有持久化的状态和内存状态，此处要不要更新deployTaskBo的状态呢？或者在外面Task的管理者统一定时持久化状态？)。
//            updateAgentStatus(deployAgentBos, agentClients);
//
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                //task线程被中断，则认为是cancel或pause，那么不管任务了
//                //executor.shutdownNow();//不在这里shutdown了，在cancel方法自己shutdown吧，因为pause是不能中断executor正在运行的agent的
//                break;
//            }
//        }
//        //执行完了，再更新一下agent状态
//        updateAgentStatus(deployAgentBos, agentClients);
//
//        //一个batch的agent执行完，根据成败看看如何继续
//        boolean hasError = hasError(agentClients);
//        if (!hasError) {
//            doWhenAgentBatchSuccess();
//        } else {
//            doWhenAgentBatchError();
//        }
//    }
//
//    /**
//     * 给vs添加log
//     */
//    private void appendLog(DeployVs deployVs, String line) {
//        String timeStamp = DateFormatUtils.format(System.currentTimeMillis(), pattern);
//
//        String summaryLog = deployVs.getSummaryLog();
//
//        StringBuilder sb = new StringBuilder(summaryLog);
//        sb.append('[').append(timeStamp).append("] ").append(line).append("\n");
//
//        deployVs.setSummaryLog(sb.toString());
//
//        deployTaskService.updateDeployVsSummaryLog(deployVs);
//    }
//
//    /**
//     * 执行完，如果有错误<br>
//     * 策略是错误跳过，则继续，和上面一样<br>
//     * 策略是错误不跳过，则fail，等待处理<br>
//     */
//    private void doWhenAgentBatchError() throws InterruptedException {
//        if (this.errorPolicy == ErrorPolicy.FALL_THROUGH) {
//            doWhenAgentBatchSuccess();
//        } else {
//            //TODO task不设置fail状态，有失败不跳过，则是暂停。
//            this.stop();
//        }
//    }
//
//    /**
//     * 一个batch的agent执行完，如果都成功 <br>
//     * 策略是手动，则pause <br>
//     * 策略是自动，则睡眠interval后继续
//     */
//    private void doWhenAgentBatchSuccess() throws InterruptedException {
//        if (!this.autoContinue) {
//            this.stop();
//        } else {
//            this.innerPause(this.deployInterval);
//        }
//    }
//
//    /**
//     * 程序启动时，需要扫描数据库，状态为READY就会拿来跑。<br>
//     * 
//     */
//    private void innerStart() {
//        //当前的状态为READY，才会执行
//        DeployTaskStatus taskStatus = deployTaskBo.getTask().getStatus();
//        if (taskStatus != DeployTaskStatus.READY) {
//            return;
//        }
//
//        if (taskThread != null) {//make sure
//            taskThread.interrupt();
//        }
//        taskThread = new Thread(new InnerTask(), "TaskExecutor-" + this.deployTaskBo.getTask().getName());
//        taskThread.start();
//    }
//
//    /**
//     * 暂停deployInterval秒后继续执行
//     */
//    private void innerPause(int deployInterval) throws InterruptedException {
//        this.stop();
//        TimeUnit.SECONDS.sleep(deployInterval);
//        this.start();
//    }
//
//    /**
//     * 这个batch的agent执行，是否有失败的
//     */
//    private boolean hasError(Map<String, AgentClient> agentClients) {
//        boolean hasError = false;
//        for (AgentClient agentClient : agentClients.values()) {
//            AgentClientResult result = agentClient.getResult();
//            if (result.getStatus().isNotSuccess()) {
//                hasError = true;
//                break;
//            }
//        }
//        return hasError;
//    }
//
//    /**
//     * 根据孩子(vs)更新task状态
//     */
//    private void autoUpdateTaskStatusByChildren(DeployTaskBo deployTaskBo) {
//        Map<String, DeployVsBo> deployVsBos = deployTaskBo.getDeployVsBos();
//
//        List<DeployVsStatus> list = new ArrayList<DeployVsStatus>();
//        for (DeployVsBo deployVsBo : deployVsBos.values()) {
//            list.add(deployVsBo.getDeployVs().getStatus());
//        }
//
//        DeployTaskStatus status = deployTaskBo.getTask().getStatus();
//        deployTaskBo.getTask().setStatus(status.calculate(list));
//    }
//
//    /**
//     * 根据孩子(agent)更新vs状态
//     */
//    private void autoUpdateVsStatusByChildren(DeployVsBo deployVsBo) {
//        Map<String, DeployAgentBo> deployAgentBos = deployVsBo.getDeployAgentBos();
//
//        List<DeployAgentStatus> list = new ArrayList<DeployAgentStatus>();
//        for (DeployAgentBo deployAgentBo : deployAgentBos.values()) {
//            list.add(deployAgentBo.getDeployAgent().getStatus());
//        }
//
//        DeployVsStatus status = deployVsBo.getDeployVs().getStatus();
//        deployVsBo.getDeployVs().setStatus(status.calculate(list));
//        //        deployVsBo.getDeployVs().setSummaryLog(deployVsBo.getDeployVs().getSummaryLog());
//
//        deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
//    }
//
//    /**
//     * agent执行过程中，更新agent状态
//     */
//    private void updateAgentStatus(Map<String, DeployAgentBo> deployAgentBos, Map<String, AgentClient> agentClients) {
//        for (Map.Entry<String, AgentClient> entry : agentClients.entrySet()) {
//            String ip = entry.getKey();
//            AgentClient agentClient = entry.getValue();
//
//            AgentClientResult result = agentClient.getResult();
//            String currentStep = result.getCurrentStep();
//            int processPct = result.getProcessPct();
//            List<String> log = result.getLogs();
//            DeployAgentStatus status = result.getStatus();
//
//            DeployAgentBo deployAgentBo = deployAgentBos.get(ip);
//            deployAgentBo.setCurrentStep(currentStep);
//            deployAgentBo.setProcessPct(processPct);
//            deployAgentBo.getDeployAgent().setRawLog(convertToRawLog(log));
//            deployAgentBo.getDeployAgent().setStatus(status);
//
//            deployTaskService.updateDeployAgentStatus(deployAgentBo.getDeployAgent());
//        }
//    }
//
//    private void changeAllStatus(DeployTaskStatus taskStatus, DeployVsStatus vsStatus, DeployAgentStatus agentStatus) {
//        DeployTaskStatus taskStatus0 = this.deployTaskBo.getTask().getStatus();
//        if (taskStatus0.canChangeTo(taskStatus)) {
//            deployTaskBo.getTask().setStatus(taskStatus);
//            deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());
//
//            if (vsStatus != null) {
//                Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
//                for (DeployVsBo deployVsBo : deployVsBos.values()) {
//                    DeployVsStatus vsStatus0 = deployVsBo.getDeployVs().getStatus();
//                    if (vsStatus0.canChangeTo(vsStatus)) {
//                        deployVsBo.getDeployVs().setStatus(vsStatus);
//                        deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
//
//                        if (agentStatus != null) {
//                            Map<String, DeployAgentBo> deployAgentBos = deployVsBo.getDeployAgentBos();
//                            for (DeployAgentBo deployAgentBo : deployAgentBos.values()) {
//                                DeployAgentStatus agentStatus0 = deployAgentBo.getDeployAgent().getStatus();
//                                if (agentStatus0.canChangeTo(agentStatus)) {
//                                    deployAgentBo.getDeployAgent().setStatus(agentStatus);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private String convertToRawLog(List<String> logs) {
//        StringBuilder sb = new StringBuilder();
//        for (String line : logs) {
//            sb.append(line).append("\n");
//        }
//        return sb.toString();
//    }
//
//    private class AgentTask implements Runnable {
//        private AgentClient    agentClient;
//        private CountDownLatch doneSignal;
//        private DeployVs       deployVs;
//        private String         ip;
//
//        public AgentTask(AgentClient agentClient, CountDownLatch doneSignal, DeployVs deployVs, String ip) {
//            this.agentClient = agentClient;
//            this.doneSignal = doneSignal;
//            this.ip = ip;
//            this.deployVs = deployVs;
//        }
//
//        @Override
//        public void run() {
//            appendLog(deployVs, "Agent(" + ip + ") executing.");
//            agentClient.execute();
//            doneSignal.countDown();
//            appendLog(deployVs, "Agent(" + ip + ") done. Result is " + agentClient.getResult().getStatus().getDesc());
//        }
//
//    }
//
//    @Override
//    public DeployTaskBo getDeployTaskBo() {
//        return deployTaskBo;
//    }
//
//    @Override
//    public void start() {
//        if (actionLock.tryLock()) {
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " start.");
//
//            try {
//                //先把WAITING状态的任务变成ready
//                DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
//                if (taskStatus == DeployTaskStatus.WAITING) {
//                    //                    //将task,vs和agent的状态，从WAITING，CREATED，CREATED设置成READY
//                    //                    this.deployTaskBo.getTask().setStatus(DeployTaskStatus.READY);
//                    //                    deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());
//                    changeAllStatus(DeployTaskStatus.READY, DeployVsStatus.READY, DeployAgentStatus.READY);
//                }
//
//                innerStart();
//
//            } finally {
//                actionLock.unlock();
//            }
//        }
//    }
//
//    @Override
//    public void stop() {
//
//        if (actionLock.tryLock()) {
//            try {
//                LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " pause.");
//
//                DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
//
//                if (taskStatus.canChangeTo(DeployTaskStatus.PAUSED)) {
//                    //终止主线程
//                    taskThread.interrupt();
//                    while (taskThread.isAlive()) {
//                        try {
//                            taskThread.join();
//                        } catch (InterruptedException e) {
//                        }
//                    }
//                    //                    //将task和vs设置成暂停
//                    //                    this.deployTaskBo.getTask().setStatus(DeployTaskStatus.PAUSED);
//                    //                    deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());
//                    //
//                    //                    Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
//                    //                    for (DeployVsBo deployVsBo : deployVsBos.values()) {
//                    //                        DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
//                    //                        if (vsStatus.canPaused()) {
//                    //                            deployVsBo.getDeployVs().setStatus(DeployVsStatus.PAUSED);
//                    //                            deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
//                    //                        }
//                    //                    }
//                    changeAllStatus(DeployTaskStatus.PAUSED, DeployVsStatus.PAUSED, null);
//
//                }
//
//            } finally {
//                actionLock.unlock();
//            }
//        }
//
//    }
//
//    /**
//     * 遇到错误时，手动处理： 将状态变成fail<br>
//     * 设置手动一步步执行时：将状态变成pause<br>
//     */
//    @Override
//    public void resume() {
//        if (actionLock.tryLock()) {
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " resume");
//
//            try {
//                DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
//
//                if (taskStatus.canChangeTo(DeployTaskStatus.READY)) {
//
//                    //                    //将task和vs的状态设置从暂停设置成ready
//                    //                    this.deployTaskBo.getTask().setStatus(DeployTaskStatus.READY);
//                    //                    deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());
//                    //
//                    //                    Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
//                    //                    for (DeployVsBo deployVsBo : deployVsBos.values()) {
//                    //                        DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
//                    //                        if (vsStatus == DeployVsStatus.PAUSED) {
//                    //                            deployVsBo.getDeployVs().setStatus(DeployVsStatus.READY);
//                    //                            deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
//                    //                        }
//                    //                    }
//
//                    changeAllStatus(DeployTaskStatus.READY, DeployVsStatus.READY, DeployAgentStatus.READY);
//
//                    //然后再调用start
//                    this.innerStart();
//                }
//
//            } finally {
//                actionLock.unlock();
//            }
//        }
//    }
//
//    @Override
//    public void cancel() {
//        if (actionLock.tryLock()) {
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " cancel.");
//
//            try {
//                DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
//                if (taskStatus.canChangeTo(DeployTaskStatus.CANCELLED)) {
//
//                    //终止agent线程
//                    executor.shutdownNow();
//                    try {
//                        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
//                    } catch (InterruptedException e1) {
//                    }
//
//                    //终止主线程
//                    taskThread.interrupt();
//                    while (taskThread.isAlive()) {
//                        try {
//                            taskThread.join();
//                        } catch (InterruptedException e) {
//                        }
//                    }
//
//                    changeAllStatus(DeployTaskStatus.CANCELLED, DeployVsStatus.CANCELLED, DeployAgentStatus.CANCELLED);
//                }
//
//            } finally {
//                actionLock.unlock();
//            }
//        }
//    }
//
//    @Override
//    public void retry() {
//        if (actionLock.tryLock()) {
//            LOG.info("Task " + CopyOfDefaultTaskExecutor.this.deployTaskBo.getTask().getName() + " retry.");
//
//            try {
//                //将fail的改为Ready
//                DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
//                if (taskStatus == DeployTaskStatus.FAILED) {
//                    //将task和vs的状态设置从暂停设置成ready
//                    this.deployTaskBo.getTask().setStatus(DeployTaskStatus.READY);
//                    deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());
//
//                    Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
//                    for (DeployVsBo deployVsBo : deployVsBos.values()) {
//                        DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
//                        if (vsStatus == DeployVsStatus.FAILED) {
//                            deployVsBo.getDeployVs().setStatus(DeployVsStatus.READY);
//                            deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
//                        }
//                    }
//                    //然后再调用start
//                    this.start();
//                }
//
//            } finally {
//                actionLock.unlock();
//            }
//        }
//    }
//
//}
