package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WareHouseDTO {

    private Integer id;
    private String warehouseName;
    private String warehouseCode;
    private String address;
}
