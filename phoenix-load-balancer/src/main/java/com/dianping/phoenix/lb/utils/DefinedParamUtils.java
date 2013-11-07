package com.dianping.phoenix.lb.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dianping.phoenix.lb.action.DefinedParam;
import com.dianping.phoenix.lb.velocity.TemplateManager;

public class DefinedParamUtils {

    private static final String DEFINED_PARAM = "defined-param";

    public static Map<String, DefinedParam> loadDefinedParamMap() {
        Map<String, DefinedParam> map = new HashMap<String, DefinedParam>();

        Set<String> filenames = TemplateManager.INSTANCE.availableFiles(DEFINED_PARAM);

        if (filenames != null) {
            for (String filename : filenames) {
                String template = TemplateManager.INSTANCE.getTemplate(DEFINED_PARAM, filename);
                DefinedParam definedParam = GsonUtils.fromJson(template, DefinedParam.class);
                definedParam.setName(filename);

                map.put(filename, definedParam);
            }
        }
        return map;
    }

    public static void main(String[] args) {
        loadDefinedParamMap();
    }

}
