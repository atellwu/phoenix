package com.dianping.phoenix;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.phoenix.config.ConfigTest;
import com.dianping.phoenix.context.ThreadLifecycleRemedyTest;
import com.dianping.phoenix.context.ThreadPoolExecutorTest;
import com.dianping.phoenix.environment.PhoenixEnvironmentTest;
import com.dianping.phoenix.log.LogTest;
import com.dianping.phoenix.session.RequestEventDelegateTest;
import com.dianping.phoenix.session.RequestIdHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({

ConfigTest.class,

LogTest.class,

ThreadLifecycleRemedyTest.class,

ThreadPoolExecutorTest.class,

/***/

PhoenixEnvironmentTest.class,

RequestEventDelegateTest.class,

RequestIdHandlerTest.class

})
public class AllTests {

}
