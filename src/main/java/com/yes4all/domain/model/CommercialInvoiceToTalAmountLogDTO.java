package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommercialInvoiceToTalAmountLogDTO {
    private Integer id;
    private Integer version;
    private String updatedNameBy;
    private String updatedBy;
    private Instant updatedDate;
    private Double amountTotalBefore;
    private Double amountTotalAfter;
    private Double truckingCostLog;
}
