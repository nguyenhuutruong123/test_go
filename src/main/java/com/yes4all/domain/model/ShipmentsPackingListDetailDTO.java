package com.yes4all.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPackingListDetailDTO {
    @NotNull
    private Integer id;
    @NotNull
    private String sku;
    @NotNull
    private String productName;
    @NotNull
    private Integer quantity;
    @NotNull
    private Integer qtyEachCarton;
    @NotNull
    private Double totalCarton;
    @NotNull
    private Double totalVolume;
    @NotNull
    private Double netWeight;
    @NotNull
    private Double grossWeight;
    @NotNull
    private String containerNumber;
    @NotNull
    @NotEmpty
    private String containerType;
    @NotNull
    @NotEmpty
    private String barcode;
    @NotNull
    private Double unitPrice;
    @NotNull
    @NotEmpty
    private String proformaInvoiceNo;

    private Integer proformaInvoiceId;

    private String note;
}
