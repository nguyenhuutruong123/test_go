package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProformaInvoiceDTO extends ProformaInvoiceMainDTO {
    @JsonProperty("details")
    private Map<String,List<ProformaInvoiceDetailDTO>> proformaInvoiceDetail = new HashMap<>();

    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;

    @JsonProperty("listRejectDetail")
    private List<Integer> listRejectDetail;

}
