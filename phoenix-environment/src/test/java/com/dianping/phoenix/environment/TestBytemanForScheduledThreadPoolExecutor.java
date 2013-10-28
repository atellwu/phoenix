package com.dianping.phoenix.environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * jvm添加:<br>
 * -javaagent:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar=script:/home/atell/document/mywork/phoenix/phoenix-environment/src/main/java/com/dianping/phoenix/environment/
 * byteman/phoenix-env.btm,boot:/home/atell/document/opensource/byteman-download-2.1.3/lib/byteman.jar
 * 
 * @param args
 */
public class TestBytemanForScheduledThreadPoolExecutor extends TestBytemanForThreadPoolExecutor {

    protected ExecutorService getExcutor() {
        return Executors.newScheduledThreadPool(2);
    }

}
