package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

/**
 * A PurchaseOrdersDate.
 */
@Entity
@Table(name = "shipment_logistics_info")
public class ShipmentLogisticsInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cut_off_si")
    private LocalDate cutOffSi;

    @Column(name = "cut_off_sy")
    private LocalDate cutOffSy;

    @Column(name = "release_so")
    private LocalDate releaseSo;

    @Column(name = "name_of_ship")
    private String nameOfShip;

    @Column(name = "ocean_freight")
    private String oceanFreight;

    @Column(name = "local_broker")
    private String localBroker;

    @Column(name = "us_broker")
    private String usBroker;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment ;

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date().toInstant();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = updatedDate = new Date().toInstant();
    }

    public void setCutOffSy(LocalDate cutOffSy) {
        this.cutOffSy = cutOffSy;
    }

    public LocalDate getCutOffSy() {
        return cutOffSy;
    }

    public LocalDate getReleaseSo() {
        return releaseSo;
    }

    public void setReleaseSo(LocalDate releaseSo) {
        this.releaseSo = releaseSo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getCutOffSi() {
        return cutOffSi;
    }

    public void setCutOffSi(LocalDate cutOffSi) {
        this.cutOffSi = cutOffSi;
    }

    public String getNameOfShip() {
        return nameOfShip;
    }

    public void setNameOfShip(String nameOfShip) {
        this.nameOfShip = nameOfShip;
    }

    public String getOceanFreight() {
        return oceanFreight;
    }

    public void setOceanFreight(String oceanFreight) {
        this.oceanFreight = oceanFreight;
    }

    public String getLocalBroker() {
        return localBroker;
    }

    public void setLocalBroker(String localBroker) {
        this.localBroker = localBroker;
    }

    public String getUsBroker() {
        return usBroker;
    }

    public void setUsBroker(String usBroker) {
        this.usBroker = usBroker;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public ShipmentLogisticsInfo shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShipmentLogisticsInfo)) {
            return false;
        }
        return id != null && id.equals(((ShipmentLogisticsInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
