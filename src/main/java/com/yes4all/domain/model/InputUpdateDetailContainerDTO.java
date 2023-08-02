package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputUpdateDetailContainerDTO {
    @NotNull
    private Integer id;
    @NotNull
    @JsonProperty("import_quantity")
    private Integer importQuantity ;

}
