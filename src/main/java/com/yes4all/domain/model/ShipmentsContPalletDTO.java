package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentsContPalletDTO {
    private Integer id;
    private String containerNumber;
    private Integer palletQuantity;
}
