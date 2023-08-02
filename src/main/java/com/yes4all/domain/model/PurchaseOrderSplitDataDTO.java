package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderSplitDataDTO {
    private Long id;
    private String sku;
    private String saleOrder;
    private String aSin;
    private String productName;
    private Long qtyOrdered;
    private String makeToStock;
    private String vendor;
    private String fulfillmentCenter;
    private LocalDate shipDate;
    private Double unitCost;
    private Double amount;
    private Double  grossWeight;
    private Double  netWeight;
    private Double cbm;
    private Integer pcs;
    private Double totalBox;
    private String country;
    private String vendorCode;
}
