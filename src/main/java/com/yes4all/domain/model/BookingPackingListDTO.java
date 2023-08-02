package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPackingListDTO {
    private Integer id;
    private String consolidator;
    private String fromCompany;
    private String soldToCompany;
    private String fromAddress;
    private String soldToAddress;
    private String fromFax;
    private String soldToFax;
    private String invoice;
    private LocalDate date;
    private String poNumber;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private Integer status;
    private String soldToTelephone;
    private String fromTelephone;
    private Instant cds;
    private String supplier;
    private Integer bookingId;
    private List<Integer> bookingPackingListIds;
    @JsonProperty("details")
    private Set<BookingPackingListDetailsDTO> bookingPackingListDetailsDTO;

    @JsonProperty("container")
    private Set<BookingPackingListContainerPalletDTO> bookingPackingListContainerPallet;

    @JsonProperty("proformaInvoices")
    private Set<BookingProformaInvoiceMainDTO> bookingProformaInvoiceMainDTO;

    @JsonProperty("commercialInvoice")
    private CommercialInvoiceDTO commercialInvoice;

}
