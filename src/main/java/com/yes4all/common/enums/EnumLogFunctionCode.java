package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumLogFunctionCode {
    CONTAINER("CONTAINER"),
    PRODUCT("PRODUCT"),
    VENDOR("VENDOR");

    private String code;

    EnumLogFunctionCode(String code) {
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
        static Map<String, EnumLogFunctionCode> map = new HashMap<>();
    }

    public static EnumLogFunctionCode find(String code) {
        EnumLogFunctionCode t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
