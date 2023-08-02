package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.PurchaseOrdersSplitData;
import com.yes4all.domain.PurchaseOrdersSplitResult;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderSplitDTO extends PurchaseOrdersMainDTO {
    @JsonProperty("input")
    private List<PurchaseOrdersSplitData> purchaseOrdersSplitData = new ArrayList<>();
     @JsonProperty("result")
    private List<PurchaseOrdersSplitResult> purchaseOrdersSplitResult = new ArrayList<>();
}
