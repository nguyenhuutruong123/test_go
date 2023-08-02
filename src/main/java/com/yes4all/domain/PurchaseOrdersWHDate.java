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
@Table(name = "purchase_orders_wh_date")
public class PurchaseOrdersWHDate implements Serializable {
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
    @JsonIgnoreProperties(value = {"purchaseOrdersWH"}, allowSetters = true)
    @JoinColumn(name = "purchase_order_wh_id", referencedColumnName = "id")
    private PurchaseOrdersWH purchaseOrdersWH;

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

    public PurchaseOrdersWH getPurchaseOrdersWH() {
        return purchaseOrdersWH;
    }

    public void setPurchaseOrdersWH(PurchaseOrdersWH purchaseOrdersWH) {
        this.purchaseOrdersWH = purchaseOrdersWH;
    }

    public PurchaseOrdersWHDate purchaseOrdersWH(PurchaseOrdersWH purchaseOrdersWH) {
        this.setPurchaseOrdersWH(purchaseOrdersWH);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersWHDate)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersWHDate) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
