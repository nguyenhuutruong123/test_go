package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;


import java.util.Date;


import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "shipment")
public class Shipment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "port_of_loading")
    private String portOfLoading;

    @Column(name = "port_of_discharge")
    private String portOfDischarge;

    @Column(name = "to_warehouse")
    private String toWarehouse;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    private Integer status;

    @Column(name = "etd")
    private LocalDate etd;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "atd")
    private LocalDate atd;

    @Column(name = "ata")
    private LocalDate ata;

    @Column(name = "consolidator")
    private String consolidator;

    @Column(name = "is_request")
    private Boolean isRequest;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "status_us_broker")
    private Integer statusUsBroker;


    @OneToOne(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ShipmentLocalBroker shipmentLocalBroker;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentsPurchaseOrders> shipmentsPurchaseOrders = new HashSet<>();


    @OneToOne(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ShipmentLogisticsInfo shipmentLogisticsInfo;
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentsContQty> shipmentsContQty = new HashSet<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKL = new HashSet<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentsContainers> shipmentsContainers = new HashSet<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentLogUpdateDate> shipmentLogUpdateDates = new HashSet<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    private Set<ShipmentLogUpdateField> shipmentLogUpdateFields = new HashSet<>();



    public Set<ShipmentsPurchaseOrders> getShipmentsPurchaseOrders() {
        return shipmentsPurchaseOrders;
    }

    public void setShipmentsPurchaseOrders(Set<ShipmentsPurchaseOrders> shipmentsPurchaseOrders) {
        if (this.shipmentsPurchaseOrders != null) {
            this.shipmentsPurchaseOrders.forEach(i -> i.setShipment(null));
        }
        if (shipmentsPurchaseOrders != null) {
            shipmentsPurchaseOrders.forEach(i -> i.shipment(this));
        }
        this.shipmentsPurchaseOrders = shipmentsPurchaseOrders;
    }


    public Set<ShipmentLogUpdateField> getShipmentLogUpdateField() {
        return shipmentLogUpdateFields;
    }

    public void setShipmentLogUpdateField(Set<ShipmentLogUpdateField> shipmentLogUpdateFields) {
        if (this.shipmentLogUpdateFields != null) {
            this.shipmentLogUpdateFields.forEach(i -> i.setShipment(null));
        }
        if (shipmentLogUpdateFields != null) {
            shipmentLogUpdateFields.forEach(i -> i.shipment(this));
        }
        this.shipmentLogUpdateFields = shipmentLogUpdateFields;
    }

    public Set<ShipmentsContainers> getShipmentsContainers() {
        return shipmentsContainers;
    }

    public void setShipmentsContainers(Set<ShipmentsContainers> shipmentsContainers) {
        if (this.shipmentsContainers != null) {
            this.shipmentsContainers.forEach(i -> i.setShipment(null));
        }
        if (shipmentsContainers != null) {
            shipmentsContainers.forEach(i -> i.shipment(this));
        }
        this.shipmentsContainers = shipmentsContainers;
    }


    public Set<ShipmentProformaInvoicePKL> getShipmentProformaInvoicePKL() {
        return shipmentProformaInvoicePKL;
    }

    public void setShipmentProformaInvoicePKL(Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKL) {
        if (this.shipmentProformaInvoicePKL != null) {
            this.shipmentProformaInvoicePKL.forEach(i -> i.setShipment(null));
        }
        if (shipmentProformaInvoicePKL != null) {
            shipmentProformaInvoicePKL.forEach(i -> i.shipment(this));
        }
        this.shipmentProformaInvoicePKL = shipmentProformaInvoicePKL;
    }

    public Set<ShipmentsContQty> getShipmentsContQty() {
        return shipmentsContQty;
    }

    public void setShipmentsContQty(Set<ShipmentsContQty> shipmentsContQty) {
        if (this.shipmentsContQty != null) {
            this.shipmentsContQty.forEach(i -> i.setShipment(null));
        }
        if (shipmentsContQty != null) {
            shipmentsContQty.forEach(i -> i.shipment(this));
        }
        this.shipmentsContQty = shipmentsContQty;
    }
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }



    @PrePersist
    protected void onCreate() {
        createdDate   = new Date().toInstant();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date().toInstant();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment)) return false;
        return id != null && id.equals(((Shipment) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
