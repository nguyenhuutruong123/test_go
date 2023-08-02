package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommercialInvoiceDTO extends CommercialInvoiceMainDTO {
    @JsonProperty("details")
    private List<CommercialInvoiceDetailDTO> commercialInvoiceDetail;

    @JsonProperty("amount_total_log")
    private List<CommercialInvoiceToTalAmountLogDTO> commercialInvoiceToTalAmountLog;

    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;


}
