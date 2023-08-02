package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum CountryEnum {
    JPN("JPN"),
    ARE("ARE"),
    AUS("AUS"),
    CAN("CAN"),
    MEX("LC"),
    GBR("GBR"),
    DEU("DEU"),
    USA("USA"),
    SGP("SGP");

    private String code;

    CountryEnum(String code) {
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
        static Map<String, CountryEnum> map = new HashMap<>();
    }

    public static CountryEnum find(String code) {
        CountryEnum t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
