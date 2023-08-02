package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentContainerDetailToIMSDTO {

    private Integer id;

    @JsonProperty("container_id")
    private String containerId ;


    @JsonProperty("quantity")
    private String quantity ;

    @JsonProperty("received_quantity")
    private String receivedQuantity ;

    @JsonProperty("sku")
    private String sku ;

    @JsonProperty("product_name")
    private String productName ;

    @JsonProperty("company_name")
    private String companyName ;

    @JsonProperty("purchase_order_no")
    private String purchaseOrderNo;

}
