package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.utils.DefinedInputUtils;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
@Component("definedInputAction")
public class DefinedInputAction extends ActionSupport {

    private static final long         serialVersionUID       = 2150069350934991522L;

    private Map<String, DefinedInput> propertiesDefinedInput = new HashMap<String, DefinedInput>();

    @PostConstruct
    public void init() {
        propertiesDefinedInput = DefinedInputUtils.loadPropertiesInput();
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

    public Map<String, DefinedInput> getPropertiesDefinedInput() {
        return propertiesDefinedInput;
    }

    public void setPropertiesDefinedInput(Map<String, DefinedInput> propertiesDefinedInput) {
        this.propertiesDefinedInput = propertiesDefinedInput;
    }

}
