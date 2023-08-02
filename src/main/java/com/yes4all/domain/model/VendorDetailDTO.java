package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDetailDTO {
    private String website;
    private String postalCode;
    private String companyOwner;
    private String totalEmployees;
    private String supplierDirectorName;
    private String supplierDirectorPhone;
    private String supplierDirectorMail;
    private String mainContactName;
    private String mainContactTitle;
    private String mainContactPhone;
    private String mainContactMail;
    private String coreProducts;
    private String incoterms;
    private String businessLicenseNo;
    private String anotherCustomer;
    private String numberOfFactory;
    private String factoryAddress;
    private String plantArea;
}
