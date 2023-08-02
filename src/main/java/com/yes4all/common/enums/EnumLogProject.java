package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumLogProject {
    IMS("IMS"),
    PIMS("PIMS");


    private String code;

    EnumLogProject(String code) {
        this.code = code;
        Holder.map.put(code, this);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private static class Holder {
        static Map<String, EnumLogProject> map = new HashMap<>();
    }

    public static EnumLogProject find(String code) {
        EnumLogProject t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
