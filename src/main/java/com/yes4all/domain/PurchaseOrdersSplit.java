package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A PurchaseOrders.
 */
@Entity
@Table(name = "split_purchase_order")
public class PurchaseOrdersSplit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;


    @NotNull
    @Column(name = "root_file")
    private String rootFile;

    @Column(name = "status")
    private Integer status;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    @Column(name = "deleted_by")
    private String deletedBy;




    @OneToMany(mappedBy = "purchaseOrdersSplit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplit"}, allowSetters = true)
    private Set<PurchaseOrdersSplitData> purchaseOrdersSplitData = new HashSet<>();

    @OneToMany(mappedBy = "purchaseOrdersSplit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplit"}, allowSetters = true)
    private Set<PurchaseOrdersSplitResult> purchaseOrdersSplitResult = new HashSet<>();

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRootFile(String rootFile) {
        this.rootFile = rootFile;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Integer getId() {
        return id;
    }

    public String getRootFile() {
        return rootFile;
    }

    public Integer getStatus() {
        return status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Set<PurchaseOrdersSplitData> getPurchaseOrdersSplitData() {
        return purchaseOrdersSplitData;
    }

    public Set<PurchaseOrdersSplitResult> getPurchaseOrdersSplitResult() {
        return purchaseOrdersSplitResult;
    }

    public void setPurchaseOrdersSplitData(Set<PurchaseOrdersSplitData> purchaseOrdersSplitData) {
        if (this.purchaseOrdersSplitData != null) {
            this.purchaseOrdersSplitData.forEach(i -> i.setPurchaseOrdersSplit(null));
        }
        if (purchaseOrdersSplitData != null) {
            purchaseOrdersSplitData.forEach(i -> i.purchaseOrders(this));
        }
        this.purchaseOrdersSplitData = purchaseOrdersSplitData;
    }

    public void setPurchaseOrdersSplitResult(Set<PurchaseOrdersSplitResult> purchaseOrdersSplitResult) {
        if (this.purchaseOrdersSplitResult != null) {
            this.purchaseOrdersSplitResult.forEach(i -> i.setPurchaseOrdersSplit(null));
        }
        if (purchaseOrdersSplitResult != null) {
            purchaseOrdersSplitResult.forEach(i -> i.purchaseOrders(this));
        }
        this.purchaseOrdersSplitResult = purchaseOrdersSplitResult;
    }


}
