package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderWHDetailDTO {
    private Integer id;
    private String sku;
    private String asin;
    private String productName;
    private Integer qty;
    private String makeToStock;
    private Double unitPrice;
    private Integer purchaseOrderId;
    private Double amount;
    private Integer pcs;
    private Double totalVolume;
    private Double grossWeight;
    private Double netWeight;
    private Integer palletQuantity;
    private String containerNo;
    private String containerType;
    private String note;
    private Double totalBox;

}
