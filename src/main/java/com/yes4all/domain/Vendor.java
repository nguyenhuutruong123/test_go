package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "vendor")
public class Vendor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_address")
    private String factoryAddress;

    @Column(name = "company_owner")
    private String companyOwner;


    @Column(name = "phone_main_contact")
    private String mainContactPhone;

    @Column(name = "number_order_no")
    private Integer numberOrderNo;


    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"vendor"}, allowSetters = true)
    private List<BankInformation> bankInformation = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendor)) return false;
        return id != null && id.equals(((Vendor) o).id);
    }


    public List<BankInformation> getBankInformation() {
        return bankInformation;
    }

    public void setBankInformation(List<BankInformation> bankInformation) {
        if (this.bankInformation != null) {
            this.bankInformation.forEach(i -> i.setVendor(null));
        }
        if (bankInformation != null) {
            bankInformation.forEach(i -> i.vendor(this));
        }
        this.bankInformation = bankInformation;
    }
}
