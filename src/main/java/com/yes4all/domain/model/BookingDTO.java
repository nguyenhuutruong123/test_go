package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDTO extends BookingMainDTO {
    private String portOfDischarge;
    private String container;
    private String invoice;
    private String portOfLoading;
    private LocalDateTime cds;
    private String destination;
    private LocalDate originEtd;
    private String freightMode;
    private LocalDate dischargeEta;
    private String fcrNo;
    private LocalDate estimatedDeliveryDate;
    private String poDest;
    private String freightTerms;
    private String shipToLocation;
    private String manufacturer;
    private Instant updatedAt;
    private String updatedBy;
    private String createdBy;
    private Instant createdAt;
    private String vendorCode;
    private String stuffingLocation;
    private Integer status;
    @JsonProperty("products")
    private List<BookingPurchaseOrderDTO> bookingPurchaseOrderDTO;
    @JsonProperty("proformaInvoice")
    private List<BookingProformaInvoiceMainDTO> bookingProformaInvoiceMainDTO;
    @JsonProperty("purchaseOrder")
    private List<BookingPurchaseOrderLocationDTO> bookingPurchaseOrderLocationDTO;

}
