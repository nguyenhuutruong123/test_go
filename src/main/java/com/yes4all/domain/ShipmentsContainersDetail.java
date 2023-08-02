package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "shipment_containers_detail")
public class ShipmentsContainersDetail implements Serializable {

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

    @Column(name = "imported_quantity")
    private Integer importedQuantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "proforma_invoice_no")
    private String proformaInvoiceNo;

    @Column(name = "proforma_invoice_id")
    private Integer proformaInvoiceId;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "import_amount")
    private Double importAmount;

    @Column(name = "note")
    private String note;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "shipment_containers_id", referencedColumnName = "id")
    private ShipmentsContainers shipmentsContainers;

    public void setShipmentsContainers(ShipmentsContainers shipmentsContainers) {
        this.shipmentsContainers = shipmentsContainers;
    }

    public ShipmentsContainers getShipmentsContainers() {
        return shipmentsContainers;
    }

    public ShipmentsContainersDetail shipmentsContainers(ShipmentsContainers shipmentsContainers) {
        this.setShipmentsContainers(shipmentsContainers);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentsContainersDetail)) return false;
        return id != null && id.equals(((ShipmentsContainersDetail) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
