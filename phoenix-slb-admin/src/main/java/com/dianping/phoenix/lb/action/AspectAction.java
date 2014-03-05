package com.dianping.phoenix.lb.action;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Aspect;
import com.dianping.phoenix.lb.service.model.CommonAspectService;
import com.dianping.phoenix.lb.utils.JsonBinder;

/**
 * @author wukezhu
 */
@Component("aspectAction")
@Scope("prototype")
public class AspectAction extends MenuAction {

    private static final Logger LOG              = LoggerFactory.getLogger(AspectAction.class);

    private static final long   serialVersionUID = -1084994778030229218L;

    private static final String MENU             = "aspect";

    protected List<Aspect>      aspects;

    @Autowired
    private CommonAspectService aspectService;

    public List<Aspect> getAspects() {
        return aspects;
    }

    public String list() {
        aspects = aspectService.listCommonAspects();
        return SUCCESS;
    }

    public String index() {
        aspects = aspectService.listCommonAspects();
        return SUCCESS;
    }

    public String show() {
        aspects = aspectService.listCommonAspects();
        editOrShow = "show";
        return SUCCESS;
    }

    public String edit() {
        aspects = aspectService.listCommonAspects();
        editOrShow = "edit";
        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public String save() throws Exception {
        try {
            String aspectsJson = IOUtils.toString(ServletActionContext.getRequest().getInputStream());
            if (StringUtils.isBlank(aspectsJson)) {
                throw new IllegalArgumentException("规则名不能为空！");
            }
            List<Aspect> aspects = JsonBinder.getNonNullBinder().fromJson(aspectsJson, List.class, Aspect.class);

            aspectService.saveCommonAspect(aspects);

            dataMap.put("errorCode", ERRORCODE_SUCCESS);
        } catch (BizException e) {
            dataMap.put("errorCode", e.getMessageId());
            dataMap.put("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            dataMap.put("errorCode", ERRORCODE_PARAM_ERROR);
            dataMap.put("errorMessage", e.getMessage());
        } catch (Exception e) {
            dataMap.put("errorCode", ERRORCODE_INNER_ERROR);
            dataMap.put("errorMessage", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        this.setMenu(MENU);
    }
}
