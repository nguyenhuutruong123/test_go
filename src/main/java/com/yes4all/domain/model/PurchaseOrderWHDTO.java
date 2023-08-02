package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderWHDTO extends PurchaseOrdersWHMainDTO {
    @JsonProperty("details")
    private Set<PurchaseOrderWHDetailDTO> purchaseOrderWHDetailDTO = new HashSet<>();

}
