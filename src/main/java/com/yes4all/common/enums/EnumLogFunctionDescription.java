package com.yes4all.common.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumLogFunctionDescription {
    IMS_TO_CONTAINER("sync data IMS to POMS when create, cancel, complete Inbound of IMS"),
    PIMS_PRODUCT_TO_POMS("sync data Product from PIMS to POMS"),

    PIMS_VENDOR_TO_POMS("sync data Vendor from PIMS to POMS");
    private String code;

    EnumLogFunctionDescription(String code) {
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
        static Map<String, EnumLogFunctionDescription> map = new HashMap<>();
    }

    public static EnumLogFunctionDescription find(String code) {
        EnumLogFunctionDescription t = Holder.map.get(code);
        if (t == null) {
            throw new IllegalStateException(String.format("Unsupported type %s.", code));
        }
        return t;
    }
}
