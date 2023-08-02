package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPurchaseOrderDTO {
    private Integer id;
    private String sku;
    private String poNumber;
    private String aSin;
    private String title;
    private Long quantity;
    private Double quantityCtns;
    private Double fobPrice;
    private Double grossWeight;
    private Double cbm;
    private String shipToLocation;
    private String supplier;
    private Double usCustomPrice;
    private Integer quantityCtnsPrevious;
    private Integer quantityPrevious;
    private String statusDetail;
}
