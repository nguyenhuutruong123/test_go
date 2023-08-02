package com.yes4all.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SizeTierEnum {
    STANDARD_SIZE(1, "Standard size"),
    OVER_SIZE(2, "Over size")
    ;

    private Integer key;
    private String desc;

    SizeTierEnum(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getValueByKey(Integer key) {
        Optional<SizeTierEnum> tier = Arrays.stream(SizeTierEnum.values())
            .filter(sizeTier -> sizeTier.key.equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get().getDesc() : "";
    }

    public static Integer getKeyByValue(String value) {
        Optional<SizeTierEnum> tier = Arrays.stream(SizeTierEnum.values())
            .filter(sizeTier -> sizeTier.desc.equals(value))
            .findFirst();
        return tier.map(SizeTierEnum::getKey).orElse(null);
    }
}
