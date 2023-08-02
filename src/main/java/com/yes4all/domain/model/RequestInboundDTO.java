package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Set;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestInboundDTO {
    @NotNull
    @JsonProperty("container_id")
    private Integer containerId;
    @NotNull
    private String action ;

    @JsonProperty("details")
    private Set<ShipmentContainerDetailToIMSDTO> shipmentContainerDetailToIMSDTOS ;
}
