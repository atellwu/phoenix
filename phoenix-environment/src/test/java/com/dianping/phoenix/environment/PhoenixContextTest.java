package com.dianping.phoenix.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.phoenix.environment.requestid.RequestIdContext;

public class PhoenixContextTest {
    private static final String REQUEST_ID_STRING = "request-id";

    boolean                     success           = false;

    @BeforeClass
    public static void initPhoenixContext() throws IOException {
        PhoenixContext.init();
    }

    @Before
    public void setupPhoenixContext() throws IOException {
        HttpServletRequest hRequest = mock(HttpServletRequest.class);
        when(hRequest.getHeader(RequestIdContext.MOBILE_REQUEST_ID)).thenReturn(REQUEST_ID_STRING);

        PhoenixContext context = PhoenixContext.get();
        context.addParam(PhoenixContext.REQUEST, hRequest);
        context.setup();
    }

    @After
    public void clearPhoenixContext() {
        PhoenixContext.get().destroy();
    }

    @Test
    public void test() throws Exception {
        RequestIdContext requestIdContext = PhoenixContext.get().get(RequestIdContext.class);
        Assert.assertEquals(REQUEST_ID_STRING, requestIdContext.getRequestId());

    }

    /**
     * getInstance是兼容0.1.0版本的方法
     */
    @Test
    public void testGetInstance() throws Exception {
        PhoenixContext.getInstance().setRequestId(REQUEST_ID_STRING);

        Thread t = new Thread(new Runnable() {
            public void run() {
                //非继承的ThreadLocal，取值为null
                success = PhoenixContext.getInstance().getRequestId() == null;
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        success = success && REQUEST_ID_STRING.equals(PhoenixContext.getInstance().getRequestId());

        Assert.assertTrue(success);

    }

}
