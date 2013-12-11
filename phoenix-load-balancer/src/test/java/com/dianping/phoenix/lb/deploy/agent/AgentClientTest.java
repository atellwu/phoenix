/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Dec 11, 2013
 * 
 */
package com.dianping.phoenix.lb.deploy.agent;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.phoenix.lb.PlexusComponentContainer;
import com.dianping.phoenix.lb.configure.ConfigManager;
import com.dianping.phoenix.lb.service.model.StrategyService;
import com.dianping.phoenix.lb.service.model.VirtualServerService;

/**
 * @author Leo Liang
 * 
 */
public class AgentClientTest {
    @Test
    public void test() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");

        AgentClient client = new DefaultAgentClient(4L, "leo", "leo-3", "192.168.22.114",
                context.getBean(VirtualServerService.class), context.getBean(StrategyService.class),
                PlexusComponentContainer.INSTANCE.lookup(ConfigManager.class));
        client.execute();
        
        AgentClientResult result = client.getResult();
        for(String log: result.getLogs()){
            System.out.println(log);
        }
    }
}
