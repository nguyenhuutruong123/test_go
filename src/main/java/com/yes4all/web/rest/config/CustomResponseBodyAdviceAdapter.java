package com.yes4all.web.rest.config;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.web.rest.payload.RestResponse;
import com.yes4all.web.rest.payload.RestResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Date;
import java.util.UUID;

@ControllerAdvice
public class CustomResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(CustomResponseBodyAdviceAdapter.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {

        if (serverHttpRequest instanceof ServletServerHttpRequest
            && serverHttpResponse instanceof ServletServerHttpResponse && (body instanceof RestResponse)) {
                log.info("RestResponse");
            RestResponse<Object> restResponse = (RestResponse<Object>) body;
            if (!CommonDataUtil.isNotNull(restResponse.getHeader())) {
                    RestResponseHeader responseHeader = RestResponseHeader.builder()
                        .respCode("00")
                        .respDesc("Success")
                        .build();
                    restResponse.setHeader(responseHeader);
                    mapCommonResponseHeader(responseHeader);
                }

        }
        return body;
    }

    private void mapCommonResponseHeader(RestResponseHeader responseHeader) {
        responseHeader.setMessageDt(DateUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
        responseHeader.setMessageUid(UUID.randomUUID().toString());
    }
}
