package com.dianping.phoenix.lb.action;

import java.util.List;

public class DefinedParam {

    private String       name;

    private String       desc;

    private InputType    inputType;

    private List<String> valueList;

    public static enum InputType {
        RADIO, CHECKBOX, TEXT, SELECT
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    @Override
    public String toString() {
        return "DefinedParam [name=" + name + ", desc=" + desc + ", inputType=" + inputType + ", valueList=" + valueList + "]";
    }

}
