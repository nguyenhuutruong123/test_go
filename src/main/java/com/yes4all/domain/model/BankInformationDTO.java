package com.yes4all.domain.model;


import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class BankInformationDTO  {


    private Long id;

    private String companyName;

    private String acNumber;

    private String beneficiaryBank;

    private String swiftCode;

    private String vendorCode;

}
