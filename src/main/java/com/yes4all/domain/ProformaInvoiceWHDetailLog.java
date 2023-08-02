package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yes4all.common.enums.EnumColumn;
import com.yes4all.common.enums.EnumUserType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "proforma_invoice_wh_detail_log")
public class ProformaInvoiceWHDetailLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "value_after")
    private Double valueAfter;

    @Column(name = "value_before")
    private Double valueBefore;

    @Column(name = "column_change")
    private EnumColumn columnChange;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "user_type")
    private EnumUserType userType;

    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "proforma_invoice_wh_detail_id", referencedColumnName = "id")
    private ProformaInvoiceWHDetail proformaInvoiceWHDetail;



    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setProformaInvoiceWHDetail(ProformaInvoiceWHDetail proformaInvoiceWHDetail) {
        this.proformaInvoiceWHDetail = proformaInvoiceWHDetail;
    }

    public ProformaInvoiceWHDetail getProformaInvoiceWHDetail() {
        return proformaInvoiceWHDetail;
    }

    public ProformaInvoiceWHDetailLog proformaInvoiceWHDetail(ProformaInvoiceWHDetail proformaInvoiceWHDetail) {
        this.setProformaInvoiceWHDetail(proformaInvoiceWHDetail);
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


    public Double getValueAfter() {
        return valueAfter;
    }

    public void setValueAfter(Double valueAfter) {
        this.valueAfter = valueAfter;
    }

    public Double getValueBefore() {
        return valueBefore;
    }

    public void setValueBefore(Double valueBefore) {
        this.valueBefore = valueBefore;
    }

    public EnumColumn getColumnChange() {
        return columnChange;
    }

    public void setColumnChange(EnumColumn columnChange) {
        this.columnChange = columnChange;
    }

    public EnumUserType getUserType() {
        return userType;
    }

    public void setUserType(EnumUserType userType) {
        this.userType = userType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProformaInvoiceWHDetailLog)) {
            return false;
        }
        return id != null && id.equals(((ProformaInvoiceWHDetailLog) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
