package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentContainerToIMSDTO {

    private Integer id;

    @JsonProperty("warehouse_code")
    private String warehouseCode ;

    @JsonProperty("title")
    private String title ;

    @JsonProperty("type")
    private String type ;

    @JsonProperty("status")
    private String status ;

}
