package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;



import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderDetailPageDTO extends PurchaseOrdersMainDTO {
    @JsonProperty("details")
    private List<PurchaseOrderDetailDTO> purchaseOrdersDetail ;

    @JsonProperty("deadlineDateDetails")
    private List<LogUpdateDateDTO> deadlineDateDetails ;

    @JsonProperty("expectedDateDetails")
    private List<LogUpdateDateDTO> expectedDateDetails ;
}
