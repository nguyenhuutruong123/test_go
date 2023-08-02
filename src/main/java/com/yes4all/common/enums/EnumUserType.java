package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumUserType {
    PU("PU"),
    SOURCING("SOURCING"),
    SUPPLIER("SUPPLIER");


    private String code;

    EnumUserType(String code) {
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
        static Map<String, EnumUserType> map = new HashMap<>();
    }

    public static EnumUserType find(String code) {
        EnumUserType t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
