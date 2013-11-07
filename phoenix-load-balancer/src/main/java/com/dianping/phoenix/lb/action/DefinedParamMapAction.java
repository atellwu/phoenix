package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.utils.DefinedParamUtils;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("definedParamMapAction")
public class DefinedParamMapAction extends ActionSupport {

    private static final long         serialVersionUID = 2150069350934991522L;

    private Map<String, DefinedParam> definedParamMap  = new HashMap<String, DefinedParam>();

    @PostConstruct
    public void init() {
        // mock virtualServers
        //        VirtualServer vs1 = new VirtualServer();
        //        vs1.setName("dpcomm");
        //        vs1.setDomain("www.dianping.com");
        //        VirtualServer vs2 = new VirtualServer();
        //        vs2.setName("tuangou");
        //        vs2.setDomain("t.dianping.com");
        //
        //        virtualServers.add(vs1);
        //        virtualServers.add(vs2);

        definedParamMap = DefinedParamUtils.loadDefinedParamMap();

        System.out.println(definedParamMap);

    }

    @Override
    public String execute() throws Exception {
        LOG.info("execute");
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
    }

    public Map<String, DefinedParam> getDefinedParamMap() {
        return definedParamMap;
    }

    public void setDefinedParamMap(Map<String, DefinedParam> definedParamMap) {
        this.definedParamMap = definedParamMap;
    }

}
