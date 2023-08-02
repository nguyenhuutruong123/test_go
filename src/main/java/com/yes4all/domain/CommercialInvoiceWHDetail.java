package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "commercial_invoice_wh_detail")
public class CommercialInvoiceWHDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;


    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "from_so")
    private String fromSo;
    @Column(name = "a_sin")
    private String aSin;

    @Column(name = "status")
    private Integer status;

    @Column(name = "status_y4a")
    private Integer statusY4a;


    @Column(name = "note")
    private String note;

    @Column(name = "unit_price_allocated")
    private Double unitPriceAllocated;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "commercial_invoice_wh_id", referencedColumnName = "id")
    private CommercialInvoiceWH commercialInvoiceWH;


    @OneToMany(mappedBy = "commercialInvoiceWHDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<CommercialInvoiceWHDetailLog> commercialInvoiceWHDetailLog = new HashSet<>();



    public Double getUnitPriceAllocated() {
        return unitPriceAllocated;
    }

    public void setUnitPriceAllocated(Double unitPriceAllocated) {
        this.unitPriceAllocated = unitPriceAllocated;
    }

    public Set<CommercialInvoiceWHDetailLog> getCommercialInvoiceWHDetailLog() {
        return commercialInvoiceWHDetailLog;
    }

    public void setCommercialInvoiceWHDetailLog(Set<CommercialInvoiceWHDetailLog> commercialInvoiceWHDetailLog) {
        if (this.commercialInvoiceWHDetailLog != null) {
            this.commercialInvoiceWHDetailLog.forEach(i -> i.setCommercialInvoiceWHDetail(null));
        }
        if (commercialInvoiceWHDetailLog != null) {
            commercialInvoiceWHDetailLog.forEach(i -> i.commercialInvoiceWHDetail(this));
        }
        this.commercialInvoiceWHDetailLog = commercialInvoiceWHDetailLog;
    }

    public Integer getStatusY4a() {
        return statusY4a;
    }

    public void setStatusY4a(Integer statusY4a) {
        this.statusY4a = statusY4a;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getaSin() {
        return aSin;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public void setFromSo(String fromSo) {
        this.fromSo = fromSo;
    }


    public String getFromSo() {
        return fromSo;
    }


    public void setCommercialInvoiceWH(CommercialInvoiceWH commercialInvoice) {
        this.commercialInvoiceWH = commercialInvoice;
    }

    public CommercialInvoiceWH getCommercialInvoiceWH() {
        return commercialInvoiceWH;
    }

    public Integer getId() {
        return this.id;
    }

    public CommercialInvoiceWHDetail id(Integer id) {
        this.setId(id);
        return this;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public CommercialInvoiceWHDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
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


    public String getProductTitle() {
        return productTitle;
    }


    public Integer getQty() {
        return qty;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public Double getAmount() {
        return amount;
    }

    public CommercialInvoiceWHDetail commercialInvoiceWH(CommercialInvoiceWH commercialInvoice) {
        this.setCommercialInvoiceWH(commercialInvoice);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommercialInvoiceWHDetail)) {
            return false;
        }
        return id != null && id.equals(((CommercialInvoiceWHDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
