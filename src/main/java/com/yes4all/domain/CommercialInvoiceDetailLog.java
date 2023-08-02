package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "commercial_invoice_detail_log")
public class CommercialInvoiceDetailLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "updated_by")
    private String updatedBy;


    @Column(name = "version")
    private Integer version;

    @Column(name = "unit_price_after")
    private Double unitPriceAfter;

    @Column(name = "unit_price_after_allocate")
    private Double unitPriceAfterAllocate;


    @Column(name = "unit_price_before_allocate")
    private Double unitPriceBeforeAllocate;

    @Column(name = "amount_after")
    private Double amountAfter;

    @Column(name = "unit_price_before")
    private Double unitPriceBefore;

    @Column(name = "amount_before")
    private Double amountBefore;

    @Column(name = "updated_date")
    private Instant updatedDate;




    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "commercial_invoice_detail_id", referencedColumnName = "id")
    private CommercialInvoiceDetail commercialInvoiceDetail;

    public Double getUnitPriceBeforeAllocate() {
        return unitPriceBeforeAllocate;
    }

    public void setUnitPriceBeforeAllocate(Double unitPriceBeforeAllocate) {
        this.unitPriceBeforeAllocate = unitPriceBeforeAllocate;
    }

    public Double getUnitPriceAfterAllocate() {
        return unitPriceAfterAllocate;
    }

    public void setUnitPriceAfterAllocate(Double unitPriceAfterAllocate) {
        this.unitPriceAfterAllocate = unitPriceAfterAllocate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setCommercialInvoiceDetail(CommercialInvoiceDetail commercialInvoiceDetail) {
        this.commercialInvoiceDetail = commercialInvoiceDetail;
    }

    public CommercialInvoiceDetail getCommercialInvoiceDetail() {
        return commercialInvoiceDetail;
    }

    public CommercialInvoiceDetailLog commercialInvoiceDetail(CommercialInvoiceDetail commercialInvoiceDetail) {
        this.setCommercialInvoiceDetail(commercialInvoiceDetail);
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }



    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Double getUnitPriceAfter() {
        return unitPriceAfter;
    }

    public void setUnitPriceAfter(Double unitPriceAfter) {
        this.unitPriceAfter = unitPriceAfter;
    }

    public Double getAmountAfter() {
        return amountAfter;
    }

    public void setAmountAfter(Double amountAfter) {
        this.amountAfter = amountAfter;
    }

    public Double getUnitPriceBefore() {
        return unitPriceBefore;
    }

    public void setUnitPriceBefore(Double unitPriceBefore) {
        this.unitPriceBefore = unitPriceBefore;
    }

    public Double getAmountBefore() {
        return amountBefore;
    }

    public void setAmountBefore(Double amountBefore) {
        this.amountBefore = amountBefore;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommercialInvoiceDetailLog)) {
            return false;
        }
        return id != null && id.equals(((CommercialInvoiceDetailLog) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
