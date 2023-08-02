package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.time.Instant;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentsContainerDetailDTO {
    private String sku;

    private String productName;

    private Integer importedQuantity;

    private Integer quantity;

    private Double unitPrice;

    private Double amount;

    private String proformaInvoiceNo;

    private Integer proformaInvoiceId;

    private Double importAmount;

    private Instant updatedDate;

    private String note;

}
