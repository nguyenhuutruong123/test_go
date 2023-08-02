package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "split_purchase_order_result")
public class PurchaseOrdersSplitResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sale_order")
    private String saleOrder;
    @Column(name = "vendor")
    private String vendor;

    @Column(name = "fulfillment_center")
    private String fulfillmentCenter;

    @Column(name = "ship_date")
    private LocalDate shipDate;
    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "country")
    private String country;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "demand")
    private String demand;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplit"}, allowSetters = true)
    @JoinColumn(name = "split_purchase_order_id", referencedColumnName = "id")
    private PurchaseOrdersSplit purchaseOrdersSplit;

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public String getDemand() {
        return demand;
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }

    @OneToMany(mappedBy = "purchaseOrdersSplitResult", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplitResult"}, allowSetters = true)
    private Set<PurchaseOrdersSplitData> purchaseOrdersSplitData = new HashSet<>();

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setPurchaseOrdersSplit(PurchaseOrdersSplit purchaseOrdersSplit) {
        this.purchaseOrdersSplit = purchaseOrdersSplit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSaleOrder(String saleOrder) {
        this.saleOrder = saleOrder;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setFulfillmentCenter(String fulfillmentCenter) {
        this.fulfillmentCenter = fulfillmentCenter;
    }

    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public String getSaleOrder() {
        return saleOrder;
    }

    public String getVendor() {
        return vendor;
    }

    public String getFulfillmentCenter() {
        return fulfillmentCenter;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public PurchaseOrdersSplit getPurchaseOrdersSplit() {
        return purchaseOrdersSplit;
    }

    public PurchaseOrdersSplitResult purchaseOrders(PurchaseOrdersSplit purchaseOrdersSplit) {
        this.setPurchaseOrdersSplit(purchaseOrdersSplit);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    public Set<PurchaseOrdersSplitData> getPurchaseOrdersSplitData() {
        return purchaseOrdersSplitData;
    }
    public void setPurchaseOrdersSplitData(Set<PurchaseOrdersSplitData> purchaseOrdersSplitData) {
        if (this.purchaseOrdersSplitData != null) {
            this.purchaseOrdersSplitData.forEach(i -> i.setPurchaseOrdersSplit(null));
        }
        if (purchaseOrdersSplitData != null) {
            purchaseOrdersSplitData.forEach(i -> i.purchaseOrdersResult(this));
        }
        this.purchaseOrdersSplitData = purchaseOrdersSplitData;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersSplitResult)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersSplitResult) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
