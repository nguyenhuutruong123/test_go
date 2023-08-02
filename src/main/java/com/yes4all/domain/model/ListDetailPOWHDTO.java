package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListDetailPOWHDTO {

    private String purchaserOrderNo;
    private String vendorCode;
    private String fulfillmentCenter;
    private LocalDate shipDate;
    private Integer purchaserOrderId;
    private String seller;
    private String companyName;
    private String acNumber;
    private String beneficiaryBank;
    private String swiftCode;
    private Double totalAmount;
    private Map<String, Set<ProformaInvoiceWHDetailDTO>> details;
}
