package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProformaInvoiceWHDTO extends ProformaInvoiceWHMainDTO {
    @JsonProperty("details")
    private Map<String,List<ProformaInvoiceWHDetailDTO>> proformaInvoiceDetail = new HashMap<>();

    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;

    @JsonProperty("listRejectDetail")
    private List<Integer> listRejectDetail;

}
