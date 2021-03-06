package com.dianping.phoenix.lb.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.service.model.CommonAspectService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author wukezhu
 */
public abstract class MenuAction extends ActionSupport {

    public static final int       ERRORCODE_SUCCESS     = 0;

    public static final int       ERRORCODE_PARAM_ERROR = -2;

    public static final int       ERRORCODE_INNER_ERROR = -1;

    private static final long     serialVersionUID      = -1084994778030229218L;

    protected Map<String, Object> dataMap               = new HashMap<String, Object>();

    protected String              contextPath;

    protected String              editOrShow            = "show";

    @Autowired
    protected CommonAspectService commonAspectService;

    protected List<Aspect>        commonAspects;

    /** vs,pool,deploy */
    private String                menu;

    @Override
    public void validate() {
        super.validate();
        if (contextPath == null) {
            contextPath = ServletActionContext.getServletContext().getContextPath();
        }
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getEditOrShow() {
        return editOrShow;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

}
