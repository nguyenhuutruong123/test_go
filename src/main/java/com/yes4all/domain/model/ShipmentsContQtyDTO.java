package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentsContQtyDTO {
    private Integer id;
    private String containerType;
    private Integer quantity;
    private Integer shipmentId;
}
