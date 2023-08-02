package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDetailSyncDTO {
    private String companyOwner;
    private String mainContactPhone;
    private String vendorCode;
    private String factoryAddress;
    private String vendorName;
    private Long id;
}
