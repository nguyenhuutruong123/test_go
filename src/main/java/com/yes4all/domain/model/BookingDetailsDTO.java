package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDetailsDTO extends BookingMainDTO {
    private String portOfDischarge;
    private String container;
    private String invoice;
    private String portOfLoading;
    private Instant cds;
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
    private Instant createdAt;
    private String vendorCode;
    private String stuffingLocation;
    private String uploadBy;
    private LocalDate shipDate;
    private Set<String> listVendor;
    @JsonProperty("products")
    private List<BookingPurchaseOrderDTO> bookingPurchaseOrder;
    @JsonProperty("proformaInvoice")
    @JsonIgnore
    private Page<BookingProformaInvoiceMainDTO> proformaInvoice;
    @JsonProperty("resource")
    @JsonIgnore
    private Page<ResourceDTO> resource;
    @JsonProperty("purchaseOrder")
    private List<BookingPurchaseOrderLocationDTO> bookingPurchaseOrderLocation;

}
