package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListSearchALLPOShipmentDTO {

    private String invoiceNo;
    private String demand;
    private String container;
    private String supplierSearch;
    private String portOfLoading;
    private String portOfDischarge;
    private String etdShipment;
    private String etaShipment;
    private Integer isSearch;
    private Integer id;
}
