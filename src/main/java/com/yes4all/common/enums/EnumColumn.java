package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumColumn {
    QTY("QTY"),
    UNIT_PRICE("UNIT_PRICE"),
    AMOUNT("AMOUNT"),
    PCS("PCS"),
    CTN("CTN"),
    CBM("CBM"),
    NW("NW"),
    GW("GW");
    private String code;

    EnumColumn(String code) {
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
        static Map<String, EnumColumn> map = new HashMap<>();
    }

    public static EnumColumn find(String code) {
        EnumColumn t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
