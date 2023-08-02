package com.yes4all.web.rest.payload;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class RestResponseHeader {
    private String messageUid;
    private String messageDt;
    @NotBlank
    private String respCode;
    @NotBlank
    private String respDesc;
}
