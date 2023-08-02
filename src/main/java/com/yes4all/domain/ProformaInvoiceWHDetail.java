package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "proforma_invoice_wh_detail")
public class ProformaInvoiceWHDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;


    @Column(name = "product_title")
    private String productName;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;


    @Column(name = "pcs")
    private Integer pcs;


    @Column(name = "cbm_total")
    private Double totalVolume;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    @Column(name = "cdc_version")
    private Long cdcVersion;


    @Column(name = "a_sin")
    private String asin;

    @Column(name = "note_adjust")
    private String noteAdjust;

    @Column(name = "qty_previous")
    private Integer qtyPrevious;

    @Column(name = "unit_price_previous")
    private Double unitPricePrevious;

    @Column(name = "amount_previous")
    private Double amountPrevious;


    @Column(name = "pcs_previous")
    private Integer pcsPrevious;

    @Column(name = "cbm_total_previous")
    private Double totalVolumePrevious;

    @Column(name = "gross_weight_previous")
    private Double grossWeightPrevious;

    @Column(name = "net_weight_previous")
    private Double netWeightPrevious;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "make_to_stock")
    private String makeToStock;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    @Column(name = "note_adjust_supplier")
    private String noteAdjustSupplier;

    @Column(name = "note_adjust_sourcing")
    private String noteAdjustSourcing;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "total_box")
    private Double totalBox;

    @Column(name = "total_box_previous")
    private Double totalBoxPrevious;


    @Column(name = "container_no")
    private String containerNo;

    @Column(name = "container_type")
    private String containerType;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "proforma_invoice_wh_id", referencedColumnName = "id")
    private ProformaInvoiceWH proformaInvoiceWH;

    @OneToMany(mappedBy = "proformaInvoiceWHDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ProformaInvoiceWHDetailLog> proformaInvoiceWHDetailLog = new HashSet<>();

    public Double getNetWeightPrevious() {
        return netWeightPrevious;
    }

    public void setNetWeightPrevious(Double netWeightPrevious) {
        this.netWeightPrevious = netWeightPrevious;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public Double getTotalBox() {
        return totalBox;
    }

    public void setTotalBox(Double totalBox) {
        this.totalBox = totalBox;
    }

    public Double getTotalBoxPrevious() {
        return totalBoxPrevious;
    }

    public void setTotalBoxPrevious(Double totalBoxPrevious) {
        this.totalBoxPrevious = totalBoxPrevious;
    }

    public Set<ProformaInvoiceWHDetailLog> getProformaInvoiceWHDetailLog() {
        return proformaInvoiceWHDetailLog;
    }

    public void setProformaInvoiceWHDetailLog(Set<ProformaInvoiceWHDetailLog> proformaInvoiceWHDetailLog) {
        if (this.proformaInvoiceWHDetailLog != null) {
            this.proformaInvoiceWHDetailLog.forEach(i -> i.setProformaInvoiceWHDetail(null));
        }
        if (proformaInvoiceWHDetailLog != null) {
            proformaInvoiceWHDetailLog.forEach(i -> i.proformaInvoiceWHDetail(this));
        }
        this.proformaInvoiceWHDetailLog = proformaInvoiceWHDetailLog;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getNoteAdjustSupplier() {
        return noteAdjustSupplier;
    }

    public void setNoteAdjustSupplier(String noteAdjustSupplier) {
        this.noteAdjustSupplier = noteAdjustSupplier;
    }

    public String getNoteAdjustSourcing() {
        return noteAdjustSourcing;
    }

    public void setNoteAdjustSourcing(String noteAdjustSourcing) {
        this.noteAdjustSourcing = noteAdjustSourcing;
    }

    public Boolean getIsConfirmed() {
        return isConfirmed;
    }

    public void setIsConfirmed(Boolean confirmed) {
        this.isConfirmed = confirmed;
    }


    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }





    public Integer getQtyPrevious() {
        return qtyPrevious;
    }

    public void setQtyPrevious(Integer qtyPrevious) {
        this.qtyPrevious = qtyPrevious;
    }

    public Double getUnitPricePrevious() {
        return unitPricePrevious;
    }

    public void setUnitPricePrevious(Double unitPricePrevious) {
        this.unitPricePrevious = unitPricePrevious;
    }

    public Double getAmountPrevious() {
        return amountPrevious;
    }

    public void setAmountPrevious(Double amountPrevious) {
        this.amountPrevious = amountPrevious;
    }

    public Integer getPcsPrevious() {
        return pcsPrevious;
    }

    public void setPcsPrevious(Integer pcsPrevious) {
        this.pcsPrevious = pcsPrevious;
    }

    public Double getTotalVolumePrevious() {
        return totalVolumePrevious;
    }

    public void setTotalVolumePrevious(Double totalVolumePrevious) {
        this.totalVolumePrevious = totalVolumePrevious;
    }

    public Double getGrossWeightPrevious() {
        return grossWeightPrevious;
    }

    public void setGrossWeightPrevious(Double grossWeightPrevious) {
        this.grossWeightPrevious = grossWeightPrevious;
    }



    public String getNoteAdjust() {
        return noteAdjust;
    }

    public void setNoteAdjust(String noteAdjust) {
        this.noteAdjust = noteAdjust;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getMakeToStock() {
        return makeToStock;
    }

    public void setMakeToStock(String makeToStock) {
        this.makeToStock = makeToStock;
    }




    public Integer getQty() {
        return qty;
    }


    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void setCdcVersion(Long cdcVersion) {
        this.cdcVersion = cdcVersion;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public Long getCdcVersion() {
        return cdcVersion;
    }

    public void setProformaInvoiceWH(ProformaInvoiceWH proformaInvoiceWH) {
        this.proformaInvoiceWH = proformaInvoiceWH;
    }

    public ProformaInvoiceWH getProformaInvoiceWH() {
        return proformaInvoiceWH;
    }

    public Integer getId() {
        return this.id;
    }

    public ProformaInvoiceWHDetail id(Integer id) {
        this.setId(id);
        return this;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public ProformaInvoiceWHDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }


    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }


    public Double getUnitPrice() {
        return unitPrice;
    }

    public Double getAmount() {
        return amount;
    }


    public Integer getPcs() {
        return pcs;
    }


    public Double getGrossWeight() {
        return grossWeight;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public ProformaInvoiceWHDetail proformaInvoiceWH(ProformaInvoiceWH proformaInvoiceWH) {
        this.setProformaInvoiceWH(proformaInvoiceWH);
        return this;
    }


    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProformaInvoiceWHDetail)) {
            return false;
        }
        return id != null && id.equals(((ProformaInvoiceWHDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
