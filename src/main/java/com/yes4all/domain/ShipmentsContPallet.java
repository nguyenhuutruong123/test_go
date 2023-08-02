package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "shipment_container_pallet")
public class ShipmentsContPallet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "container_number")
    private String containerNumber;

    @Column(name = "pallet_quantity")
    private Integer palletQuantity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_packing_list_id", referencedColumnName = "id")
    private ShipmentsPackingList  shipmentsPackingList;

    public void setShipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.shipmentsPackingList = shipmentsPackingList;
    }

    public ShipmentsPackingList getShipmentsPackingList() {
        return shipmentsPackingList;
    }

    public ShipmentsContPallet shipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.setShipmentsPackingList(shipmentsPackingList);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsContPallet)) return false;
        return id != null && id.equals(((ShipmentsContPallet) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
