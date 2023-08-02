package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumLogFunctionAction {
    CREATE("CREATE"),
    CANCEL("CANCEL"),
    COMPLETE("COMPLETE"),
    SYNC("SYNC");
    private String code;

    EnumLogFunctionAction(String code) {
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
        static Map<String, EnumLogFunctionAction> map = new HashMap<>();
    }

    public static EnumLogFunctionAction find(String code) {
        EnumLogFunctionAction t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
