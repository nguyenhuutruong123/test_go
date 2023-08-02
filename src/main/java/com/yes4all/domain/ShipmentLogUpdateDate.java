package com.yes4all.domain;

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
@Table(name = "shipment_log_update_date")
public class ShipmentLogUpdateDate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "date_before")
    private LocalDate dateBefore;

    @Column(name = "date_after")
    private LocalDate dateAfter;

    @Column(name = "type_date")
    private String typeDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"shipment"}, allowSetters = true)
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date().toInstant();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = updatedDate = new Date().toInstant();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDateBefore() {
        return dateBefore;
    }

    public void setDateBefore(LocalDate dateBefore) {
        this.dateBefore = dateBefore;
    }

    public LocalDate getDateAfter() {
        return dateAfter;
    }

    public void setDateAfter(LocalDate dateAfter) {
        this.dateAfter = dateAfter;
    }

    public String getTypeDate() {
        return typeDate;
    }

    public void setTypeDate(String typeDate) {
        this.typeDate = typeDate;
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

    public ShipmentLogUpdateDate shipment(Shipment shipment) {
        this.setShipment(shipment);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShipmentLogUpdateDate)) {
            return false;
        }
        return id != null && id.equals(((ShipmentLogUpdateDate) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
