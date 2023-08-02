package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "shipment_container_quantity")
public class ShipmentsContQty implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "container_type")
    private String containerType;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentsContQty shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsContQty)) return false;
        return id != null && id.equals(((ShipmentsContQty) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
