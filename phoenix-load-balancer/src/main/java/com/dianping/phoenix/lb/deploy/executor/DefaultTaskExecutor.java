package com.dianping.phoenix.lb.deploy.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.phoenix.lb.deploy.agent.DefaultAgentClient;
import com.dianping.phoenix.lb.deploy.bo.DeployTaskBo;
import com.dianping.phoenix.lb.deploy.bo.DeployVsBo;
import com.dianping.phoenix.lb.deploy.model.AgentBatch;
import com.dianping.phoenix.lb.deploy.model.DeployAgent;
import com.dianping.phoenix.lb.deploy.model.DeployAgentStatus;
import com.dianping.phoenix.lb.deploy.model.DeployTaskStatus;
import com.dianping.phoenix.lb.deploy.model.DeployVs;
import com.dianping.phoenix.lb.deploy.model.DeployVsStatus;
import com.dianping.phoenix.lb.deploy.model.ErrorPolicy;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

public class DefaultTaskExecutor implements TaskExecutor {

    private final DeployTaskBo         deployTaskBo;

    private final VirtualServerService virtualServerService;

    private final boolean              autoContinue;

    private final int                  deployInterval;

    private final AgentBatch           agentBatch;

    private final ErrorPolicy          errorPolicy;

    /** 正在发布的哪个站点 */
    private String                     currentVsName;
    /** 已经完成到第几台agent */
    private int                        index;

    public DefaultTaskExecutor(DeployTaskBo deployTaskBo, VirtualServerService virtualServerService) {
        this.deployTaskBo = deployTaskBo;
        this.virtualServerService = virtualServerService;

        this.autoContinue = deployTaskBo.getTask().getAutoContinue();
        this.deployInterval = deployTaskBo.getTask().getDeployInterval();
        this.agentBatch = deployTaskBo.getTask().getAgentBatch();
        this.errorPolicy = deployTaskBo.getTask().getErrorPolicy();
        //从deployTaskBo读取进度
        //        DeployTask task = deployTaskBo.getTask();
        //
        //        StatusContainer2.INSTANCE.getDeployTask(deployTaskBo.getTask().getId());

        //
        //        int totalCount = this.deployTaskBo.getDeployVsBos().size();
        //        int intervalCount = this.deployTaskBo.getTask().getDeployPolicy().getIntervalCount(totalCount);
    }

    /**
     * (1)程序启动时，需要扫描数据库，状态为READY就会拿来跑。<br>
     * (2)失败后重试和手动点击启动，都是start方法，一样的流程。（重试前会将状态设置称READY，所以和手动点击start是一样的情况） (3)
     */
    @Override
    public void start() {
        //根据目前的进度，获取当前的状态是否要Task自动执行
        DeployTaskStatus taskStatus = deployTaskBo.getTask().getStatus();
        boolean needContinue = needContinue(taskStatus);
        //如果不需要自动执行不做继续
        if (!needContinue) {
            return;
        }
        //如果需要自动执行，则获取接下来要发布的vs和List<String>

        //        getVsNameToExecute();
        DeployVsBo deployVsBo = null;
        while ((deployVsBo = nextReadyDeployVs(deployTaskBo)) != null) {
            List<DeployAgent> deployAgents = null;
            while ((deployAgents = nextReadyDeployAgents(deployVsBo, agentBatch.getBatchSize())).size() > 0) {

                //创建Agent执行者
                Long deployId = deployTaskBo.getTask().getAgentId();
                String vsName = deployVsBo.getDeployVs().getVsName();
                String vsTag = deployVsBo.getDeployVs().getVsTag();
                DefaultAgentClient agentClient = new DefaultAgentClient(deployId, vsName, vsTag, virtualServerService);

                agentClient.execute();

            }

        }

    }

    private boolean needContinue(DeployTaskStatus taskStatus) {
        return taskStatus == DeployTaskStatus.READY;
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

    private List<DeployAgent> nextReadyDeployAgents(DeployVsBo deployVsBo, int n) {
        List<DeployAgent> re = new ArrayList<DeployAgent>();
        Map<String, DeployAgent> deployAgents = deployVsBo.getDeployAgents();
        Set<String> hostNameSet = deployAgents.keySet();
        //顺序遍历agent,获取到第一个需要执行的agent。
        for (String hostName : hostNameSet) {
            DeployAgent deployAgent = deployAgents.get(hostName);
            if (deployAgent.getStatus() == DeployAgentStatus.INIT) {
                re.add(deployAgent);
                if (re.size() >= n) {
                    break;
                }
            }
        }
        return re;
    }

    private void execute() {
        //多线程执行agent执行者

        //等待agent都执行完

        //更新所有状态，包括持久花到数据库

        //执行完，判断状态，如果都成功
        // 策略是手动，则pause
        // 策略是自动，则睡眠interval后继续

        //执行完，如果有错误
        // 策略是错误跳过，则继续，和上面一样
        // 策略是错误不跳过，则fail，等待处理
    }

    /**
     * 获取上次发布的进度，初始化时需要使用。
     */

    @Override
    public void pause() {

    }

    /**
     * 遇到错误时，手动处理： 将状态变成fail<br>
     * 设置手动一步步执行时：将状态变成pause<br>
     */
    @Override
    public void resume() {
        //与start方法不同，这里是先把pause状态的任务变成ready

        //然后再调用start
    }

    @Override
    public void cancle() {

    }

    @Override
    public TaskStatus getStatus() {
        return null;
    }

}
