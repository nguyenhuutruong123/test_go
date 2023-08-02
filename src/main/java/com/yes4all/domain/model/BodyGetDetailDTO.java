package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BodyGetDetailDTO {
    @NotNull
    private Integer id;
    @NotNull
    private String vendor;
    private Boolean isSupplier;
    private Boolean isViewCI;
}
