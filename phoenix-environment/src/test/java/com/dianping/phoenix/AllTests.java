package com.dianping.phoenix;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.phoenix.environment.PhoenixEnvironmentTest;
import com.dianping.phoenix.net.SocketsTest;
import com.dianping.phoenix.session.RequestEventDelegateTest;
import com.dianping.phoenix.session.RequestIdHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({

PhoenixEnvironmentTest.class,

RequestEventDelegateTest.class,

RequestIdHandlerTest.class,

SocketsTest.class

})
public class AllTests {

}
