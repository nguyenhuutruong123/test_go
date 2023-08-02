package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "shipment_purchase_orders")
public class ShipmentsPurchaseOrders implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "purchase_order_no")
    private String purchaseOrderNo;


    @Column(name = "purchase_order_id")
    private Integer purchaseOrderId;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    @OneToMany(mappedBy = "shipmentsPurchaseOrders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipmentsPurchaseOrders"}, allowSetters = true)
    private Set<ShipmentsPurchaseOrdersDetail> shipmentsPurchaseOrdersDetail = new HashSet<>();

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentsPurchaseOrders shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }

    public Set<ShipmentsPurchaseOrdersDetail> getShipmentsPurchaseOrdersDetail() {
        return shipmentsPurchaseOrdersDetail;
    }

    public void setShipmentsPurchaseOrdersDetail(Set<ShipmentsPurchaseOrdersDetail> shipmentsPurchaseOrdersDetail) {
        if (this.shipmentsPurchaseOrdersDetail != null) {
            this.shipmentsPurchaseOrdersDetail.forEach(i -> i.setShipmentsPurchaseOrders(null));
        }
        if (shipmentsPurchaseOrdersDetail != null) {
            shipmentsPurchaseOrdersDetail.forEach(i -> i.shipmentsPurchaseOrders(this));
        }
        this.shipmentsPurchaseOrdersDetail = shipmentsPurchaseOrdersDetail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsPurchaseOrders)) return false;
        return id != null && id.equals(((ShipmentsPurchaseOrders) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
