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

/**
 * A PurchaseOrdersDate.
 */
@Getter
@Setter
@Entity
@Table(name = "shipment_local_broker")
public class ShipmentLocalBroker implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "hbl")
    private String hbl;

    @Column(name = "mbl")
    private String mbl;

    @Column(name = "shipping_type")
    private String shippingType;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment ;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShipmentLocalBroker)) {
            return false;
        }
        return id != null && id.equals(((ShipmentLocalBroker) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
