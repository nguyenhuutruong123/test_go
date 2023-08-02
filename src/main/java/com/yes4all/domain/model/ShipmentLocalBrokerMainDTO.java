package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentLocalBrokerMainDTO {

    private Integer shipmentId;

    private Integer id;

    private String hbl;

    private String mbl;

    private String shippingType;
    private String userId;
}
