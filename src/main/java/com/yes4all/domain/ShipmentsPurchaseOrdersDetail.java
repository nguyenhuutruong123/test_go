package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "shipment_purchase_orders_detail")
public class ShipmentsPurchaseOrdersDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "net_weight")
    private Double netWeight;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipments_purchase_orders_id", referencedColumnName = "id")
    private ShipmentsPurchaseOrders shipmentsPurchaseOrders;

    public void setShipmentsPurchaseOrders(ShipmentsPurchaseOrders shipmentsPurchaseOrders) {
        this.shipmentsPurchaseOrders = shipmentsPurchaseOrders;
    }

    public ShipmentsPurchaseOrders getShipmentsPurchaseOrders() {
        return shipmentsPurchaseOrders;
    }

    public ShipmentsPurchaseOrdersDetail shipmentsPurchaseOrders(ShipmentsPurchaseOrders shipmentsPurchaseOrders) {
        this.setShipmentsPurchaseOrders(shipmentsPurchaseOrders);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsPurchaseOrdersDetail)) return false;
        return id != null && id.equals(((ShipmentsPurchaseOrdersDetail) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
