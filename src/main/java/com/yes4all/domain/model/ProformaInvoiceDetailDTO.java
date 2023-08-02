package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.yes4all.common.annotation.BooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProformaInvoiceDetailDTO {
    private Integer id;
    private String sku;
    private String asin;
    private String productName;
    private String barcode;
    @NotNull
    private Integer qty;
    @NotNull
    private Double unitPrice;
    @NotNull
    private Double amount;
    @NotNull
    private Integer pcs;
    @JsonProperty("isDeleted")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean isDeleted;
    @NotNull
    private Double totalVolume;
    @NotNull
    private Double grossWeight;
    @NotNull
    private Double netWeight;
    @NotNull
    private String fromSo;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate shipDate;
    @NotNull
    private Long cdcVersion;
    private String noteAdjust;
    @NotNull
    private Integer qtyPrevious;
    @NotNull
    private Double unitPricePrevious;
    @NotNull
    private Double amountPrevious;
    @NotNull
    private Integer pcsPrevious;
    @NotNull
    private Double totalVolumePrevious;
    @NotNull
    private Double grossWeightPrevious;
    @NotNull
    private Double netWeightPrevious;
    @NotNull
    private Double totalBox;
    @NotNull
    private Double totalBoxPrevious;
    @NotNull
    private String makeToStock;

    private Boolean isConfirmed;
    private String key;
    private String updatedBy;
    private String updatedNameBy;
    private Instant updatedDate;
    private String noteAdjustSupplier;
    private String noteAdjustSourcing;


    private String containerNo;
    private String containerType;
    @JsonProperty("details_log")
    private List<ProformaInvoiceDetailLogDTO> proformaInvoiceDetailLog;
 }
