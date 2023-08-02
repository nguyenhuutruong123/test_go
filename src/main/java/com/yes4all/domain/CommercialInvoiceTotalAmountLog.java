package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "commercial_invoice_total_amount_log")
public class CommercialInvoiceTotalAmountLog implements Serializable {

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


    @Column(name = "amount_total_after")
    private Double amountTotalAfter;



    @Column(name = "amount_total_before")
    private Double amountTotalBefore;

    @Column(name = "updated_date")
    private Instant updatedDate;


    @Column(name = "trucking_cost_log")
    private Double truckingCostLog;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "commercial_invoice_id", referencedColumnName = "id")
    private CommercialInvoice commercialInvoice;


    public Double getTruckingCostLog() {
        return truckingCostLog;
    }

    public void setTruckingCostLog(Double truckingCostLog) {
        this.truckingCostLog = truckingCostLog;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setCommercialInvoice(CommercialInvoice commercialInvoice) {
        this.commercialInvoice = commercialInvoice;
    }

    public CommercialInvoice getCommercialInvoice() {
        return commercialInvoice;
    }

    public CommercialInvoiceTotalAmountLog commercialInvoice(CommercialInvoice commercialInvoice) {
        this.setCommercialInvoice(commercialInvoice);
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

    public Double getAmountTotalAfter() {
        return amountTotalAfter;
    }

    public void setAmountTotalAfter(Double amountTotalAfter) {
        this.amountTotalAfter = amountTotalAfter;
    }

    public Double getAmountTotalBefore() {
        return amountTotalBefore;
    }

    public void setAmountTotalBefore(Double amountTotalBefore) {
        this.amountTotalBefore = amountTotalBefore;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommercialInvoiceTotalAmountLog)) {
            return false;
        }
        return id != null && id.equals(((CommercialInvoiceTotalAmountLog) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
