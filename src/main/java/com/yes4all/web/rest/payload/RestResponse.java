package com.yes4all.web.rest.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class RestResponse<T> {

    @NotNull
    @Valid
    private RestResponseHeader header;

    @Valid @JsonInclude(JsonInclude.Include.NON_NULL)
    private T body;
}
