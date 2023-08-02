package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrdersMainDTO {
    private Integer id;
    private String poNumber;
    private String fromSo;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    @JsonProperty("vendorName")
    private String vendorId;
    private String vendorCode;
    private String reasonCancel;
    private String shipmentId;
    private String country;
    private String fulfillmentCenter;
    private Integer channel;
    private Long totalItem;
    private Double totalCost;
    private LocalDate expectedShipDate;
    private LocalDate actualShipDate;
    private LocalDate etd;
    private LocalDate eta;
    private LocalDate ata;
    private Integer fromPurchaseOrderId;
    private String usBroker;
    private Integer status;
    private String updatedBy;
    private String createdBy;
    private Instant createdDate;
    private Integer proformaInvoiceId;
    private Integer commercialInvoiceId;
    private String proformaInvoiceNo;
    private String commercialInvoiceNo;
    private String bookingNumber;
    private Integer bookingId;
    private LocalDate atd;
    private LocalDate shipWindowStart;
    private LocalDate shipWindowEnd;
    private String portOfLoading;
    private Long cdcVersion;
    private Map<Integer,String> toCI=new HashMap<>();
    private Integer fromId;
    private Instant orderedDate;
    private LocalDate expectedShipDatePrevious;
	private String demand;


}
