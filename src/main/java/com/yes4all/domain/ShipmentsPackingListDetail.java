package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "shipment_packing_list_detail")
public class ShipmentsPackingListDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "qty_each_carton")
    private Integer qtyEachCarton;

    @Column(name = "total_carton")
    private Double totalCarton;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "container_number")
    private String containerNumber;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "container_type")
    private String containerType;

    @Column(name = "proforma_invoice_no")
    private String proformaInvoiceNo;

    @Column(name = "proforma_invoice_id")
    private Integer proformaInvoiceId;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipments_packing_list_id", referencedColumnName = "id")
    private ShipmentsPackingList shipmentsPackingList;

    public void setShipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.shipmentsPackingList = shipmentsPackingList;
    }

    public ShipmentsPackingList getShipmentsPackingList() {
        return shipmentsPackingList;
    }

    public ShipmentsPackingListDetail shipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.setShipmentsPackingList(shipmentsPackingList);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsPackingListDetail)) return false;
        return id != null && id.equals(((ShipmentsPackingListDetail) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
