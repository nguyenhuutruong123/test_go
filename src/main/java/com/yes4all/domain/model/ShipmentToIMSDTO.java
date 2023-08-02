package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentToIMSDTO {

    private Integer id;

    @JsonProperty("shipment_inv")
    private String shipmentInv ;

    @JsonProperty("shipment_ctrl_id")
    private String shipmentCtrlId ;

    @JsonProperty("status")
    private String status ;

    @JsonProperty("po_title")
    private String poTitle ;

    @JsonProperty("containers")
    List<ShipmentContainerToIMSDTO> shipmentContainerToIMS=new ArrayList<>();

}
