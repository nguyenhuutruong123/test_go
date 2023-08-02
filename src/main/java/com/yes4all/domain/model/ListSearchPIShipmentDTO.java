package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListSearchPIShipmentDTO {
    @JsonProperty("details")
    private List<SearchPIShipmentDTO> search;
    private Integer shipmentId;
    private String userId;
}
