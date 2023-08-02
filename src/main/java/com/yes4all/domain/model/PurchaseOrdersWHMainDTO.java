package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrdersWHMainDTO {
    private Integer id;
    private String poNumber;
    @JsonProperty("vendorName")
    private String vendorId;


    private String shipmentNo;

    private Integer shipmentId;


    private String country;
    private Integer channel;
    private Long totalItem;
    private Double totalAmount;
    private LocalDate expectedShipDate;
    private LocalDate actualShipDate;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate ata;
    private Integer status;
    private String updatedBy;
    private String createdBy;
    private Instant createdDate;
    private Instant updatedDate;
    private Integer proformaInvoiceId;
    private String proformaInvoiceNo;
    private Integer commercialInvoiceId;
    private String commercialInvoiceNo;

    private LocalDate atd;
    private Instant orderedDate;
	private String plOrder;
    private Double totalGrossWeight;
    private Double totalCbm;
    private Integer numberContainer;
    private String portOfLoading;
    private String portOfDeparture;


    private String createdNameBy;
    private String updatedNameBy;


}
