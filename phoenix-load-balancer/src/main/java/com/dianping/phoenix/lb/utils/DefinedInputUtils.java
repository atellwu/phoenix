package com.dianping.phoenix.lb.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dianping.phoenix.lb.action.DefinedInput;
import com.dianping.phoenix.lb.velocity.TemplateManager;

public class DefinedInputUtils {

    private static final String DIR_NAME = "defined-properties";

    public static Map<String, DefinedInput> loadPropertiesInput() {
        Map<String, DefinedInput> map = new HashMap<String, DefinedInput>();

        Set<String> filenames = TemplateManager.INSTANCE.availableFiles(DIR_NAME);

        if (filenames != null) {
            for (String filename : filenames) {
                String template = TemplateManager.INSTANCE.getTemplate(DIR_NAME, filename);
                DefinedInput definedParam = GsonUtils.fromJson(template, DefinedInput.class);
                definedParam.setName(filename);

                map.put(filename, definedParam);
            }
        }
        return map;
    }

    public static void main(String[] args) {
        loadPropertiesInput();
    }

}
