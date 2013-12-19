package com.dianping.phoenix.session;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.phoenix.session.requestid.RecordFileManagerTest;
import com.dianping.phoenix.session.requestid.RequestEventHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({

RecordFileManagerTest.class,

RequestEventHandlerTest.class

})
public class AllTests {

}
