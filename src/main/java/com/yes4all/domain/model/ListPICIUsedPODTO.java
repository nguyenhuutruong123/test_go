package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListPICIUsedPODTO {
    private String pI;
    private String cI ;
    private Integer qtyPIUsed ;
    private Long qtyCIUsed ;
    private Integer proformaInvoiceId ;
    private Integer commercialInvoiceId ;

}
