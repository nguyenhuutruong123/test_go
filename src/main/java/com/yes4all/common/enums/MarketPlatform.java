package com.yes4all.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public enum MarketPlatform {
    AMAZON(1, "Amazon"),
    WALMART(2, "Walmart"),
    ;

    private final Integer id;
    private final String name;

    MarketPlatform(Integer id, String name) {
        this.id = id;
        this.name = name;
        Holder.map.put(name, this);
    }

    private static class Holder {
        static Map<String, MarketPlatform> map = new HashMap<>();
    }

    public static MarketPlatform find(String id) {
        return Holder.map.get(id);
    }

    public static String getValueByKey(Integer key) {
        Optional<MarketPlatform> tier = Arrays.stream(MarketPlatform.values())
            .filter(value -> value.getId().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get().getName() : "";
    }

    public static Integer getKeyByValue(String value) {
        Optional<MarketPlatform> tier = Arrays.stream(MarketPlatform.values())
            .filter(marketPlatforms -> marketPlatforms.getName().equals(value))
            .findFirst();
        return tier.map(MarketPlatform::getId).orElse(null);
    }
}
