package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "shipment_proforma_invoice_pkl")
public class ShipmentProformaInvoicePKL implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"shipmentsPackingList"}, allowSetters = true)
    @JoinColumn(name = "shipment_packing_list_id", referencedColumnName = "id")
    private ShipmentsPackingList shipmentsPackingList;

    @Column(name = "proforma_invoice_no")
    private String proformaInvoiceNo;

    @Column(name = "proforma_invoice_id")
    private Integer proformaInvoiceId;

    @Column(name = "supplier")
    private String supplier;

    public Integer getProformaInvoiceId() {
        return proformaInvoiceId;
    }

    public void setProformaInvoiceId(Integer proformaInvoiceId) {
        this.proformaInvoiceId = proformaInvoiceId;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }


    public String getProformaInvoiceNo() {
        return proformaInvoiceNo;
    }

    public void setProformaInvoiceNo(String proformaInvoiceNo) {
        this.proformaInvoiceNo = proformaInvoiceNo;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentProformaInvoicePKL shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }

    public ShipmentProformaInvoicePKL shipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.setShipmentsPackingList(shipmentsPackingList);
        return this;
    }
    public ShipmentsPackingList getShipmentsPackingList() {
        return shipmentsPackingList;
    }

    public void setShipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.shipmentsPackingList = shipmentsPackingList;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }





}
