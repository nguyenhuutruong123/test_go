package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;



/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "purchase_orders_wh_detail")
public class PurchaseOrdersWHDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "sku")
    private String sku;

    @Column(name = "a_sin")
    private String asin;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "qty_ordered")
    private Integer qty;

    @Column(name = "make_to_stock")
    private String makeToStock;

    @Column(name = "unit_cost")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "pcs")
    private Integer pcs;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "pallet_quantity")
    private Integer palletQuantity;

    @Column(name = "container_no")
    private String containerNo;

    @Column(name = "container_type")
    private String containerType;

    @Column(name = "note")
    private String note;

    @Column(name = "total_box")
    private Double totalBox;



    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"purchaseOrdersWH"}, allowSetters = true)
    @JoinColumn(name = "purchase_order_wh_id", referencedColumnName = "id")
    private PurchaseOrdersWH purchaseOrdersWH;

    public PurchaseOrdersWHDetail(String sku, String productName, Integer qty) {
        this.sku = sku;
        this.productName = productName;
        this.qty = qty;
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



    public Integer getPalletQuantity() {
        return palletQuantity;
    }

    public void setPalletQuantity(Integer palletQuantity) {
        this.palletQuantity = palletQuantity;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public void setPcs(Integer pcs) {
        this.pcs = pcs;
    }


    public Double getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }


    public Double getAmount() {
        return amount;
    }

    public Integer getPcs() {
        return pcs;
    }


    public Double getTotalVolume() {
        return totalVolume;
    }


    public PurchaseOrdersWHDetail() {
    }

    public void setPurchaseOrdersWH(PurchaseOrdersWH purchaseOrdersWH) {
        this.purchaseOrdersWH = purchaseOrdersWH;
    }

    public PurchaseOrdersWH getPurchaseOrdersWH() {
        return purchaseOrdersWH;
    }
    // jhipster-needle-entity-add-field - JHipster will add fields here



    public Integer getId() {
        return this.id;
    }

    public PurchaseOrdersWHDetail id(Integer id) {
        this.setId(id);
        return this;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return this.sku;
    }

    public PurchaseOrdersWHDetail sku(String sku) {
        this.setSku(sku);
        return this;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setMakeToStock(String makeToStock) {
        this.makeToStock = makeToStock;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getMakeToStock() {
        return makeToStock;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public PurchaseOrdersWHDetail purchaseOrdersWH(PurchaseOrdersWH purchaseOrdersWH) {
        this.setPurchaseOrdersWH(purchaseOrdersWH);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersWHDetail)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersWHDetail) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


}
