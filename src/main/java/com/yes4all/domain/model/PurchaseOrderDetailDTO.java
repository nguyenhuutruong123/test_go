package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailDTO {
    private Integer id;
    private String sku;
    private String fromSo;
    private String asin;
    private String productName;
    private Long qtyPIUsed;
    private Long qtyCIUsed;
    private Integer qty;
    private Long qtyAvailable;
    private String makeToStock;
    private Double unitPrice;
    private Double totalCost;
    private String bookingNumber;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    List<ListPICIUsedPODTO> listPICIUsedPODTO;
    private Integer purchaseOrderId;
    private Double amount;
    private Integer pcs;
    private Double totalVolume;
    private Double totalBox;
    private Double grossWeight;
    private Double unitPricePrevious;
    private Integer qtyPrevious;
    private Double amountPrevious;
    private Integer pcsPrevious;
    private Double totalVolumePrevious;
    private Double totalBoxPrevious;
    private Double grossWeightPrevious;
    private LocalDate shipDate;
    private Integer onboardQty;
    private Double netWeight;
    private Double  netWeightPrevious;
    private String makeToStockPrevious;

}
