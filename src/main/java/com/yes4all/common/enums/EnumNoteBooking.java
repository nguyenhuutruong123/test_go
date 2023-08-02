package com.yes4all.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public enum EnumNoteBooking {

    REASON_0("0", "Select reason"),
    REASON_1("1", "Compliance"),
    REASON_2("2", "AMZ close PO"),
    REASON_3("3", "B2R Test"),
    REASON_4("4", "Product quality"),
    REASON_5("5", "Oversize/Overweight"),
    REASON_6("6", "Vendor cancel"),
    REASON_7("7", "Change vendor"),
    REASON_8("8", "FBA Test"),
    REASON_9("9", "Short leadtime"),
    REASON_10("10", "Order below MOQ"),
    REASON_11("11", "Vendor shortship"),
    REASON_12("12", "Change business strategy"),
    REASON_13("13", "Patent"),
    REASON_14("14", "Permanently Unavailable"),
    REASON_15("15", "Change box"),
    ;

    private final String id;
    private final String name;

    EnumNoteBooking(String id, String name) {
        this.id = id;
        this.name = name;
        Holder.map.put(name, this);
    }

    private static class Holder {
        static Map<String, EnumNoteBooking> map = new HashMap<>();
    }

    public static EnumNoteBooking find(String id) {
        return Holder.map.get(id);
    }

    public static String getValueByKey(String key) {
        Optional<EnumNoteBooking> tier = Arrays.stream(EnumNoteBooking.values())
            .filter(value -> value.getId().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get().getName() : "";
    }

    public static String getKeyByValue(String value) {
        Optional<EnumNoteBooking> tier = Arrays.stream(EnumNoteBooking.values())
            .filter(marketPlatforms -> marketPlatforms.getName().equals(value))
            .findFirst();
        return tier.map(EnumNoteBooking::getId).orElse(null);
    }
}
