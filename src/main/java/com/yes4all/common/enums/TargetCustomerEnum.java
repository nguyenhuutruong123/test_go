package com.yes4all.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum TargetCustomerEnum {
    MALE(1, "Male"),
    FEMALE(2, "Female"),
    UNISEX(3, "Unisex"),
    JUNIOR(4, "Junior"),
    KIDS(5, "Kids"),
    ALL(6, "All")
    ;

    private Integer key;
    private String value;

    TargetCustomerEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getValueByKey(Integer key) {
        Optional<TargetCustomerEnum> tier = Arrays.stream(TargetCustomerEnum.values())
            .filter(value -> value.getKey().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get().getValue() : "";
    }

    public static Integer getKeyByValue(String value) {
        Optional<TargetCustomerEnum> tier = Arrays.stream(TargetCustomerEnum.values())
            .filter(targetCustomerEnum -> targetCustomerEnum.getValue().equals(value))
            .findFirst();
        return tier.map(TargetCustomerEnum::getKey).orElse(null);
    }
}
