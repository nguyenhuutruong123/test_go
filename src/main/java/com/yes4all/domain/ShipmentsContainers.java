package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "shipment_containers")
public class ShipmentsContainers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "container_number")
    private String containerNumber;

    @Column(name = "container_discharge_date")
    private LocalDateTime containerDischargeDate;

    @Column(name = "gate_out_port")
    private String gateOutPort;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "total_net_weight")
    private Double totalNetWeight;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "status")
    private Integer status;

    @Column(name = "type")
    private String type;

    @Column(name = "wh_location")
    private String whLocation;

    @Column(name = "note")
    private String note;

    @Column(name = "unloading_type")
    private String unloadingType;

    @Column(name = "wh_delivered")
    private LocalDateTime whDelivered;

    @Column(name = "wh_release")
    private LocalDateTime whRelease;

    @Column(name = "wh_gate_out")
    private LocalDate whGateOut;

    @Column(name = "eta_wh")
    private LocalDateTime etaWh;


    @OneToMany(mappedBy = "shipmentsContainers", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"shipmentsContainers"}, allowSetters = true)
    private List<ShipmentsContainersDetail> shipmentsContainersDetail = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment  shipment;

    public List<ShipmentsContainersDetail> getShipmentsContainersDetail() {
        return shipmentsContainersDetail;
    }

    public void setShipmentsContainersDetail(List<ShipmentsContainersDetail> shipmentsContainersDetail) {
        if (this.shipmentsContainersDetail != null) {
            this.shipmentsContainersDetail.forEach(i -> i.setShipmentsContainers(null));
        }
        if (shipmentsContainersDetail != null) {
            shipmentsContainersDetail.forEach(i -> i.shipmentsContainers(this));
        }
        this.shipmentsContainersDetail = shipmentsContainersDetail;
    }


    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentsContainers shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsContainers)) return false;
        return id != null && id.equals(((ShipmentsContainers) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
