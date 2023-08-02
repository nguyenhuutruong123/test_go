package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorMainDetailDTO {
    private Long id;
    private String vendorCode;
    private String vendorName;
    private String vendorAddress;
    private String shippingPort;
    private String capacityUnitPerMonth;
    private String capacityContPerMonth;
    private String createdBy;
    private Instant createdDate;
    private String updatedBy;
    private Instant updatedDate;
    private String paymentTerm;
}
