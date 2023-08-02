package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingProformaInvoiceMainDTO {
    private Integer id;
    private String invoiceNo;
    private String poAmazon;
    private Long quantity;
    private Double ctn;
    private Double cbm;
    private LocalDate shipDate;
    private String proformaInvoiceNo;
    private Integer bookingPackingListId;
    private Integer bookingPackingListStatus;
    private String supplier;
    @JsonProperty("packingList")
    private BookingPackingListDTO bookingPackingListDTO;
    private Integer commercialInvoiceId;
}
