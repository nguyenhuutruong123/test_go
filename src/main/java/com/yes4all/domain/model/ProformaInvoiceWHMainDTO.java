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
public class ProformaInvoiceWHMainDTO {
    private Integer id;
    private String seller;
    private String numberPO;
    private Integer purchaserOrderId;
    private String purchaserOrderNo;
    private Integer status;
    private Integer totalQuantity;
    private String buyer;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    private String orderNo;
    private String term;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;
    private String paymentTerm;
    private String remark;
    private String companyName;
    private String acNumber;
    private String beneficiaryBank;
    private String swiftCode;
    private String updatedBy;
    private String createdBy;
    private Instant createdDate;
    private Instant updatedDate;
    private Double amount;
    private Double ctn;
    private Double cbmTotal;
    private Double grossWeight;
    private String supplier;
    private String vendorCode;
    private LocalDate shipDate;
    @NotNull
    private Boolean isSupplier;
    private Boolean isNotSend;
    private Integer statusSourcing;
    private Integer userUpdatedLatest;
    private Integer stepActionBy;
    private Integer statusPU;
    private String confirmedByPU;
    private Instant confirmedDatePU;
    private String confirmedBySC;
    private Instant confirmedDateSC;
}
