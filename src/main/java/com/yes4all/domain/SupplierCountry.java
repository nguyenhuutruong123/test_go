package com.yes4all.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "supplier_country")
public class SupplierCountry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "country")
    private String country;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "order_number_wh")
    private Integer orderNumberWh;

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getOrderNumberWh() {
        return orderNumberWh;
    }

    public void setOrderNumberWh(Integer orderNumberWh) {
        this.orderNumberWh = orderNumberWh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierCountry)) return false;
        return id != null && id.equals(((SupplierCountry) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
