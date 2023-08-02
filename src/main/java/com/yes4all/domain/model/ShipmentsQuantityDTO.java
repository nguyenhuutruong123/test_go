package com.yes4all.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsQuantityDTO {

    private String sku;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double amount;

    private Double totalVolume;

    private Double grossWeight;

    private Double netWeight;
}
