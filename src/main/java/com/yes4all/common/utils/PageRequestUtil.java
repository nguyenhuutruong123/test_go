package com.yes4all.common.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Objects;

public class PageRequestUtil {


    public static Pageable genPageRequest(Integer page, Integer limit, Sort.Direction direction, String... sort) {
        page = (Objects.isNull(page) ) ? 0 : page;
        limit =  (Objects.isNull(limit) || limit <= 0) ? 10 : limit;
        if (CommonDataUtil.isEmpty(Arrays.toString(sort)) || sort[0] == null) {
            return genPageRequest(page, limit);
        }
        return PageRequest.of(page, limit, Sort.by(direction, sort));
    }

    public static Pageable genPageRequest(Integer page, Integer limit, Sort sort) {
        page = (Objects.isNull(page) ) ? 0 : page;
        limit =  (Objects.isNull(limit) || limit <= 0) ? 10 : limit;
        return PageRequest.of(page, limit, sort);
    }

    public static Pageable genPageRequest(Integer page, Integer limit) {
        return PageRequest.of(page, limit);
    }
}
