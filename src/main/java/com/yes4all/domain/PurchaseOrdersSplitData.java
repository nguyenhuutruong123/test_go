package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "split_purchase_order_data")
public class PurchaseOrdersSplitData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;
    @NotNull
    @Column(name = "sale_order")
    private String saleOrder;
    @Column(name = "a_sin")
    private String aSin;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "qty_ordered")
    private Long qtyOrdered;

    @Column(name = "make_to_stock")
    private String makeToStock;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "fulfillment_center")
    private String fulfillmentCenter;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "unit_cost")
    private Double unitCost;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "gross_weight")
    private Double  grossWeight;

    @Column(name = "net_weight")
    private Double  netWeight;

    @Column(name = "cbm")
    private Double cbm;

    @Column(name = "pcs")
    private Integer pcs;

    @Column(name = "total_box")
    private Double totalBox;

    @Column(name = "country")
    private String country;

    @Column(name = "vendor_code")
    private String vendorCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplit"}, allowSetters = true)
    @JoinColumn(name = "split_purchase_order_id", referencedColumnName = "id")
    private PurchaseOrdersSplit purchaseOrdersSplit;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"purchaseOrdersSplitResult"}, allowSetters = true)
    @JoinColumn(name = "split_purchase_order_result_id", referencedColumnName = "id")
    private PurchaseOrdersSplitResult purchaseOrdersSplitResult;

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public Double getCbm() {
        return cbm;
    }

    public void setCbm(Double cbm) {
        this.cbm = cbm;
    }

    public Integer getPcs() {
        return pcs;
    }

    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }

    public Double getTotalBox() {
        return totalBox;
    }

    public void setTotalBox(Double totalBox) {
        this.totalBox = totalBox;
    }

    public void setSaleOrder(String saleOrder) {
        this.saleOrder = saleOrder;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQtyOrdered(Long qtyOrdered) {
        this.qtyOrdered = qtyOrdered;
    }

    public void setMakeToStock(String makeToStock) {
        this.makeToStock = makeToStock;
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

    public void setPurchaseOrdersSplit(PurchaseOrdersSplit purchaseOrdersSplit) {
        this.purchaseOrdersSplit = purchaseOrdersSplit;
    }

    public Integer getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getSaleOrder() {
        return saleOrder;
    }

    public String getaSin() {
        return aSin;
    }

    public String getProductName() {
        return productName;
    }

    public Long getQtyOrdered() {
        return qtyOrdered;
    }

    public String getMakeToStock() {
        return makeToStock;
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

    public PurchaseOrdersSplitData purchaseOrders(PurchaseOrdersSplit purchaseOrdersSplit) {
        this.setPurchaseOrdersSplit(purchaseOrdersSplit);
        return this;
    }

    public PurchaseOrdersSplitResult getPurchaseOrdersSplitResult() {
        return purchaseOrdersSplitResult;
    }

    public PurchaseOrdersSplitData purchaseOrdersResult(PurchaseOrdersSplitResult purchaseOrdersSplitResult) {
        this.setPurchaseOrdersSplitResult(purchaseOrdersSplitResult);
        return this;
    }

    public void setPurchaseOrdersSplitResult(PurchaseOrdersSplitResult purchaseOrdersSplitResult) {
        this.purchaseOrdersSplitResult = purchaseOrdersSplitResult;
    }
// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersSplitData)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersSplitData) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
