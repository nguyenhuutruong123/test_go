package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumerationType {
    CATEGORY("CT"),
    FAMILY("FM"),
    STYLE("ST"),
    MODEL("MD"),
    LIFECYCLE("LC"),
    SELLING_STATUS("SS"),
    REQUEST_CHANGE_TYPE("RCT"),
    MAIN_MATERIAL("MT"),
    MATERIAL_SUBCATEGORY("MS"),
    PRODUCT_CATEGORY("PC"),
    PRODUCT_GROUP("PG"),
    PRODUCT_SUBCATEGORY("PS"),
    PRODUCT_TYPE("PT"),
    ;

    private String code;

    EnumerationType(String code) {
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
        static Map<String, EnumerationType> map = new HashMap<>();
    }

    public static EnumerationType find(String code) {
        EnumerationType t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
