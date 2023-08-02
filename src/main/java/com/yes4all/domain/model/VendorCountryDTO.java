package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorCountryDTO {
    private Long id;

    @JsonAlias("vendor_code")
    private String vendorCode;



    private String country;


}
