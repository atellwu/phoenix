package com.dianping.phoenix.lb.deploy.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.deploy.agent.AgentClient;
import com.dianping.phoenix.lb.deploy.agent.AgentClientResult;
import com.dianping.phoenix.lb.deploy.agent.DefaultAgentClient;
import com.dianping.phoenix.lb.deploy.bo.DeployAgentBo;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;
import com.dianping.phoenix.lb.deploy.model.AgentBatch;
import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;
import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
import com.dianping.phoenix.lb.deploy.model.DeployVs;
import com.dianping.phoenix.lb.deploy.model.DeployVsStatus;
import com.dianping.phoenix.lb.deploy.model.ErrorPolicy;
import com.dianping.phoenix.lb.deploy.service.DeployTaskService;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

public class DefaultTaskExecutor implements TaskExecutor {

    private final DeployTaskBo         deployTaskBo;

    private final DeployTaskService    deployTaskService;

    private final VirtualServerService virtualServerService;

    private final StrategyService      strategyService;

    private final ConfigManager        configManager;

    private final boolean              autoContinue;

    private final int                  deployInterval;

    private final AgentBatch           agentBatch;

    private final ErrorPolicy          errorPolicy;

    private Thread                     taskThread;

    private ExecutorService            executor;

    public DefaultTaskExecutor(DeployTaskBo deployTaskBo, DeployTaskService deployTaskService, VirtualServerService virtualServerService, StrategyService strategyService, ConfigManager configManager) {
        this.deployTaskBo = deployTaskBo;

        this.deployTaskService = deployTaskService;
        this.virtualServerService = virtualServerService;
        this.strategyService = strategyService;
        this.configManager = configManager;

        this.autoContinue = deployTaskBo.getTask().getAutoContinue();
        this.deployInterval = deployTaskBo.getTask().getDeployInterval();
        this.agentBatch = deployTaskBo.getTask().getAgentBatch();
        this.errorPolicy = deployTaskBo.getTask().getErrorPolicy();

    }

    /**
     * (1)程序启动时，需要扫描数据库，状态为READY就会拿来跑。<br>
     * (2)失败后重试和手动点击启动，都是start方法，一样的流程。（重试前会将状态设置称READY，所以和手动点击start是一样的情况） (3)
     * 
     */
    @Override
    public synchronized void start() {
        //当前的状态为READY，才会执行
        DeployTaskStatus taskStatus = deployTaskBo.getTask().getStatus();
        if (taskStatus != DeployTaskStatus.READY) {
            return;
        }

        if (taskThread != null) {//make sure
            taskThread.interrupt();
        }
        taskThread = new Thread(new InnerTask(), "TaskExecutor-" + this.deployTaskBo.getTask().getName());
        taskThread.start();
    }

    private class InnerTask implements Runnable {

        @Override
        public void run() {
            executor = Executors.newCachedThreadPool();

            //获取接下来要发布的vs和agent
            DeployVsBo deployVsBo = null;
            while ((deployVsBo = nextReadyDeployVs(deployTaskBo)) != null && !Thread.currentThread().isInterrupted()) {
                Map<String, DeployAgentBo> deployAgentBos = null;
                while ((deployAgentBos = nextReadyDeployAgents(deployVsBo, agentBatch.getBatchSize())).size() > 0 && !Thread.currentThread().isInterrupted()) {
                    try {
                        execute(deployVsBo, deployAgentBos);
                    } catch (InterruptedException e) {
                        // 运行Task的主线程中被中断了(应该是被暂停或取消了，此处不要管，结束该方法即可)
                        Thread.currentThread().interrupt();
                    }
                }
                //执行完一个vs(或者因为暂停/取消而退出)，需要更新一下vs的状态
                updateVsStatus(deployVsBo);
            }

            //执行完所有vs(或者因为暂停/取消而退出)，需要更新一下task的状态
            updateTaskStatus(deployTaskBo);
        }
    }

    private DeployVsBo nextReadyDeployVs(DeployTaskBo deployTaskBo) {
        Map<String, DeployVsBo> deployVsBos = deployTaskBo.getDeployVsBos();
        DeployVsBo vsToBeDeploy = null;
        Set<String> vsNameSet = deployVsBos.keySet();
        for (String vsName : vsNameSet) {
            DeployVsBo deployVsBo = deployVsBos.get(vsName);
            DeployVs deployVs = deployVsBo.getDeployVs();
            //顺序遍历vs,获取到第一个需要执行的vs。
            if (deployVs.getStatus() == DeployVsStatus.READY) {
                vsToBeDeploy = deployVsBo;
                break;
            }
        }
        return vsToBeDeploy;
    }

    private Map<String, DeployAgentBo> nextReadyDeployAgents(DeployVsBo deployVsBo, int n) {
        Map<String, DeployAgentBo> re = new HashMap<String, DeployAgentBo>();
        Map<String, DeployAgentBo> deployAgentBos = deployVsBo.getDeployAgentBos();
        Set<String> hostNameSet = deployAgentBos.keySet();
        //顺序遍历agent,获取到第一个需要执行的agent。
        for (String ip : hostNameSet) {
            DeployAgentBo deployAgentBo = deployAgentBos.get(ip);
            if (deployAgentBo.getDeployAgent().getStatus() == DeployAgentStatus.INIT) {
                re.put(ip, deployAgentBo);
                if (re.size() >= n) {
                    break;
                }
            }
        }
        return re;
    }

    private void execute(DeployVsBo deployVsBo, Map<String, DeployAgentBo> deployAgentBos) throws InterruptedException {
        //创建Agent执行者
        Map<String, AgentClient> agentClients = new HashMap<String, AgentClient>();
        for (DeployAgentBo deployAgentBo : deployAgentBos.values()) {
            long deployId = deployTaskBo.getTask().getAgentId();
            String vsName = deployVsBo.getDeployVs().getVsName();
            String vsTag = deployVsBo.getDeployVs().getVsTag();
            String ip = deployAgentBo.getDeployAgent().getIpAddress();
            DefaultAgentClient agentClient = new DefaultAgentClient(deployId, vsName, vsTag, ip, virtualServerService, strategyService, configManager);
            agentClients.put(ip, agentClient);
        }

        //多线程执行agent执行者
        CountDownLatch doneSignal = new CountDownLatch(agentClients.size());
        for (AgentClient agentClient : agentClients.values()) {
            executor.execute(new AgentTask(agentClient, doneSignal));
        }
        //执行的过程中，所有状态，需要反馈过去，包括持久花到数据库
        while (doneSignal.getCount() > 0) {
            //获取agent的执行状态，设置到deployTaskBo中(deployTaskBo含有持久化的状态和内存状态，此处要不要更新deployTaskBo的状态呢？或者在外面Task的管理者统一定时持久化状态？)。
            updateAgentStatus(deployAgentBos, agentClients);

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //task线程被中断，则认为是cancel，那么不管任务了
                executor.shutdownNow();//不在这里shutdown了，在cancel方法自己shutdown吧
                break;
            }
        }
        //执行完了，再更新一下agent状态
        updateAgentStatus(deployAgentBos, agentClients);

        //一个batch的agent执行完，根据成败看看如何继续
        boolean hasError = hasError(agentClients);
        if (!hasError) {
            doWhenAgentBatchSuccess();
        } else {
            doWhenAgentBatchError();
        }

    }

    /**
     * 执行完，如果有错误<br>
     * 策略是错误跳过，则继续，和上面一样<br>
     * 策略是错误不跳过，则fail，等待处理<br>
     */
    private void doWhenAgentBatchError() throws InterruptedException {
        if (this.errorPolicy == ErrorPolicy.FALL_THROUGH) {
            doWhenAgentBatchSuccess();
        } else {
            //TODO task不设置fail状态，有失败不跳过，则是暂停。
            this.pause();
        }
    }

    /**
     * 一个batch的agent执行完，如果都成功 <br>
     * 策略是手动，则pause <br>
     * 策略是自动，则睡眠interval后继续
     */
    private void doWhenAgentBatchSuccess() throws InterruptedException {
        if (!this.autoContinue) {
            this.pause();
        } else {
            this.pause(this.deployInterval);
        }
    }

    /**
     * 暂停deployInterval秒后继续执行
     */
    private void pause(int deployInterval) throws InterruptedException {
        this.pause();
        TimeUnit.SECONDS.sleep(deployInterval);
        this.start();
    }

    /**
     * 这个batch的agent执行，是否有失败的
     */
    private boolean hasError(Map<String, AgentClient> agentClients) {
        boolean hasError = false;
        for (AgentClient agentClient : agentClients.values()) {
            AgentClientResult result = agentClient.getResult();
            if (result.getStatus().isNotSuccess()) {
                hasError = true;
                break;
            }
        }
        return hasError;
    }

    //根据
    private void updateTaskStatus(DeployTaskBo deployTaskBo) {
        Map<String, DeployVsBo> deployVsBos = deployTaskBo.getDeployVsBos();

        List<DeployVsStatus> list = new ArrayList<DeployVsStatus>();
        for (DeployVsBo deployVsBo : deployVsBos.values()) {
            list.add(deployVsBo.getDeployVs().getStatus());
        }

        DeployTaskStatus status = deployTaskBo.getTask().getStatus();
        deployTaskBo.getTask().setStatus(status.calculate(list));

    }

    /**
     * 根据孩子更新状态
     */
    private void updateVsStatus(DeployVsBo deployVsBo) {
        Map<String, DeployAgentBo> deployAgentBos = deployVsBo.getDeployAgentBos();

        List<DeployAgentStatus> list = new ArrayList<DeployAgentStatus>();
        for (DeployAgentBo deployAgentBo : deployAgentBos.values()) {
            list.add(deployAgentBo.getDeployAgent().getStatus());
        }

        DeployVsStatus status = deployVsBo.getDeployVs().getStatus();
        deployVsBo.getDeployVs().setStatus(status.calculate(list));

        deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
    }

    /**
     * agent执行过程中，更新agent状态
     */
    private void updateAgentStatus(Map<String, DeployAgentBo> deployAgentBos, Map<String, AgentClient> agentClients) {
        for (Map.Entry<String, AgentClient> entry : agentClients.entrySet()) {
            String ip = entry.getKey();
            AgentClient agentClient = entry.getValue();

            AgentClientResult result = agentClient.getResult();
            String currentStep = result.getCurrentStep();
            int processPct = result.getProcessPct();
            List<String> log = result.getLogs();
            DeployAgentStatus status = result.getStatus();

            DeployAgentBo deployAgentBo = deployAgentBos.get(ip);
            deployAgentBo.setCurrentStep(currentStep);
            deployAgentBo.setProcessPct(processPct);
            deployAgentBo.getDeployAgent().setRawLog(convertToRawLog(log));
            deployAgentBo.getDeployAgent().setStatus(status);

            deployTaskService.updateDeployAgentStatus(deployAgentBo.getDeployAgent());
        }
    }

    private String convertToRawLog(List<String> logs) {
        StringBuilder sb = new StringBuilder();
        for (String line : logs) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    @Override
    public synchronized void pause() {
        DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
        if (taskStatus.canPaused()) {
            //将task和vs设置成暂停
            this.deployTaskBo.getTask().setStatus(DeployTaskStatus.PAUSED);
            deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());

            Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
            for (DeployVsBo deployVsBo : deployVsBos.values()) {
                DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
                if (vsStatus.canPaused()) {
                    deployVsBo.getDeployVs().setStatus(DeployVsStatus.PAUSED);
                    deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
                }
            }
            //终止线程
            taskThread.interrupt();

        }
    }

    /**
     * 遇到错误时，手动处理： 将状态变成fail<br>
     * 设置手动一步步执行时：将状态变成pause<br>
     */
    @Override
    public synchronized void resume() {
        //与start方法不同，这里是先把pause状态的任务变成ready
        DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
        if (taskStatus == DeployTaskStatus.PAUSED) {
            //将task和vs的状态设置从暂停设置成ready
            this.deployTaskBo.getTask().setStatus(DeployTaskStatus.READY);
            deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());

            Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
            for (DeployVsBo deployVsBo : deployVsBos.values()) {
                DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
                if (vsStatus == DeployVsStatus.PAUSED) {
                    deployVsBo.getDeployVs().setStatus(DeployVsStatus.READY);
                    deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
                }
            }
            //然后再调用start
            this.start();
        }
    }

    @Override
    public void cancle() {
        DeployTaskStatus taskStatus = this.deployTaskBo.getTask().getStatus();
        if (taskStatus.canCancel()) {
            //将task和vs设置成暂停
            this.deployTaskBo.getTask().setStatus(DeployTaskStatus.CANCELLING);
            deployTaskService.updateDeployTaskStatus(deployTaskBo.getTask());

            Map<String, DeployVsBo> deployVsBos = this.deployTaskBo.getDeployVsBos();
            for (DeployVsBo deployVsBo : deployVsBos.values()) {
                DeployVsStatus vsStatus = deployVsBo.getDeployVs().getStatus();
                if (vsStatus.canCancel()) {
                    deployVsBo.getDeployVs().setStatus(DeployVsStatus.CANCELLING);
                    deployTaskService.updateDeployVsStatus(deployVsBo.getDeployVs());
                }
            }
            //终止线程
            taskThread.interrupt();
        }
    }

    private class AgentTask implements Runnable {
        private AgentClient    agentClient;
        private CountDownLatch doneSignal;

        public AgentTask(AgentClient agentClient, CountDownLatch doneSignal) {
            this.agentClient = agentClient;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            agentClient.execute();
            doneSignal.countDown();
        }

    }

    @Override
    public DeployTaskBo getDeployTaskBo() {
        return deployTaskBo;
    }

}
