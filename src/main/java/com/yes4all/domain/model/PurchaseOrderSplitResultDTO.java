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
public class PurchaseOrderSplitResultDTO {
    private Long id;
    private String saleOrder;
    private String vendor;
    private String fulfillmentCenter;
    private LocalDate shipDate;
    private String orderNo;
    private Long totalQuantity;
    private Double totalAmount;
    private String country;
    private String demand;
}
