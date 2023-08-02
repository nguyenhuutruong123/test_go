package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderDataPageDTO extends PurchaseOrdersMainSplitDTO {
    @JsonProperty("details")
    private Page<PurchaseOrderSplitDataDTO> purchaseOrderSplitDataDTO ;

}
