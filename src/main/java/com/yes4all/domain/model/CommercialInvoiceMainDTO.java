package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.yes4all.common.annotation.InstantDeserializer;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommercialInvoiceMainDTO {
    private Integer id;
    private String seller;
    private String fromSo;
    private String numberPO;
    private Integer status;
    private String buyer;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    private String invoiceNo;
    private String term;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;
    private String paymentTerm;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate shipDate;
    private String remark;
    private String companyName;
    private String acNumber;
    private String beneficiaryBank;
    private String swiftCode;
    private String supplier;
    private String updatedBy;
    private String createdBy;
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant createdDate;
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant updatedDate;
    private Double amount;
    private String fulfillmentCenter;
    private Integer proformaInvoiceId;
    private Integer bookingPackingListId;
    private String bookingPackingListNo;
    private String vendorCode;
    private Double truckingCost;
    @NotNull
    private Boolean isSupplier;
    private Double amountLogReject;
    private Integer versionLogReject;
    private Boolean theFirstReject;
    private Boolean supplierUpdatedLatest;
    private Boolean isViewCI;
}
