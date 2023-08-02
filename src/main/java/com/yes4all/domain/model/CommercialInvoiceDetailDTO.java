package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommercialInvoiceDetailDTO {
    private Integer id;
    private String sku;
    private String productTitle;
    private Integer qty;
    private Double unitPrice;
    private Double unitPriceAllocated;
    private Double amount;
    private String fromSo;
    private String aSin;
    private String proformaInvoiceNo;
    private List<CommercialInvoiceDetailLogDTO> commercialInvoiceDetailLog;
    private Integer status;
    private Integer statusY4a;
    private String note;

}
