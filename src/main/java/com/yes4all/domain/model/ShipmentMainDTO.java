package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentMainDTO {
    private Integer id;


    private String shipmentId;
    private Set<ListPODTO> purchaseOrder;



    private String purchaseOrderNo;
    private String proformaInvoiceNo;
    private Integer purchaseOrderId;
    private Integer proformaInvoiceId;
    private String portOfLoading;
    private String portOfDischarge;
    private Integer status;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate etd;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate eta;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate atd;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate ata;
    private Instant createdDate;
    private Instant updatedDate;
    private String createdBy;
    private String updatedBy;
    private Double totalAmount;
    private Set<String> consolidatorList;

    private Set<ShipmentsQuantityDTO> shipmentsQuantities;


}
