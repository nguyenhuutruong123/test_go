package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "shipment_packing_list")
public class ShipmentsPackingList implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "invoice")
    private String invoice;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "status")
    private Integer status;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "commercial_invoice_wh_id", referencedColumnName = "id")
    private CommercialInvoiceWH commercialInvoiceWH;

    @OneToMany(mappedBy = "shipmentsPackingList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipmentsPackingList"}, allowSetters = true)
    private Set<ShipmentsPackingListDetail> shipmentsPackingListDetail = new HashSet<>();

    @OneToMany(mappedBy = "shipmentsPackingList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipmentsPackingList"}, allowSetters = true)
    private Set<ShipmentsContPallet> shipmentsContPallet = new HashSet<>();


    @OneToMany(mappedBy = "shipmentsPackingList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipmentsPackingList"}, allowSetters = true)
    private Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKL;


    public Set<ShipmentProformaInvoicePKL> getShipmentProformaInvoicePKL() {
        return shipmentProformaInvoicePKL;
    }

    public void setShipmentProformaInvoicePKL(Set<ShipmentProformaInvoicePKL>shipmentProformaInvoicePKL) {
        if (this.shipmentProformaInvoicePKL != null) {
            this.shipmentProformaInvoicePKL.forEach(i -> i.setShipment(null));
        }
        if (shipmentProformaInvoicePKL != null) {
            shipmentProformaInvoicePKL.forEach(i -> i.shipmentsPackingList(this));
        }
        this.shipmentProformaInvoicePKL = shipmentProformaInvoicePKL;
    }
    public Set<ShipmentsContPallet> getShipmentsContPallet() {
        return shipmentsContPallet;
    }

    public void setShipmentsContPallet(Set<ShipmentsContPallet> shipmentsContPallet) {
        if (this.shipmentsContPallet != null) {
            this.shipmentsContPallet.forEach(i -> i.setShipmentsPackingList(null));
        }
        if (shipmentsContPallet != null) {
            shipmentsContPallet.forEach(i -> i.shipmentsPackingList(this));
        }
        this.shipmentsContPallet = shipmentsContPallet;
    }





    public Set<ShipmentsPackingListDetail> getShipmentsPackingListDetail() {
        return shipmentsPackingListDetail;
    }

    public void setShipmentsPackingListDetail(Set<ShipmentsPackingListDetail> shipmentsPackingListDetail) {
        if (this.shipmentsPackingListDetail != null) {
            this.shipmentsPackingListDetail.forEach(i -> i.setShipmentsPackingList(null));
        }
        if (shipmentsPackingListDetail != null) {
            shipmentsPackingListDetail.forEach(i -> i.shipmentsPackingList(this));
        }
        this.shipmentsPackingListDetail = shipmentsPackingListDetail;
    }

    public CommercialInvoiceWH getCommercialInvoiceWH() {
        return commercialInvoiceWH;
    }

    public void setCommercialInvoiceWH(CommercialInvoiceWH commercialInvoiceWH) {
        this.commercialInvoiceWH = commercialInvoiceWH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsPackingList)) return false;
        return id != null && id.equals(((ShipmentsPackingList) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
