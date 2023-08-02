package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A PurchaseOrders.
 */
@Entity
@Table(name = "purchase_orders_wh")
public class PurchaseOrdersWH implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @NotNull
    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "vendor_id")
    private String vendorId;

    @Column(name = "country")
    private String country;

    @Column(name = "shipment_id")
    private String shipmentId;




    @Column(name = "channel")
    private Integer channel;

    @Column(name = "total_item")
    private Integer totalItem;


    @Column(name = "expected_ship_date")
    private LocalDate expectedShipDate;

    @Column(name = "actual_ship_date")
    private LocalDate actualShipDate;

    @Column(name = "etd")
    private LocalDate etd;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "port_of_loading")
    private String portOfLoading;

    @Column(name = "atd")
    private LocalDate atd;

    @Column(name = "ata")
    private LocalDate ata;

    @Column(name = "status")
    private Integer status;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_date")
    private Instant updatedDate;


    @Column(name = "ordered_date")
    private Instant orderedDate;



    @Column(name = "is_sendmail")
    private Boolean isSendmail;

    @Column(name = "user_send")
    private String userSend;

    @Column(name = "number_container")
    private Integer numberContainer;

    @Column(name = "pl_order")
    private String plOrder;

    @Column(name = "total_cbm")
    private Double totalCbm;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "total_gross_weight")
    private Double totalGrossWeight;

    @Column(name = "port_of_departure")
    private String portOfDeparture;

    @Column(name = "etd_original")
    private LocalDate etdOriginal;
    @OneToMany(mappedBy = "purchaseOrdersWH", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrdersWH"}, allowSetters = true)
    private Set<PurchaseOrdersWHDetail> purchaseOrdersWHDetail = new HashSet<>();

    @OneToMany(mappedBy = "purchaseOrdersWH", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"purchaseOrdersWH"}, allowSetters = true)
    private Set<PurchaseOrdersWHDate> purchaseOrdersWHDate = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "proforma_invoice_wh_id", referencedColumnName = "id")
    private ProformaInvoiceWH proformaInvoiceWH ;

    public LocalDate getEtdOriginal() {
        return etdOriginal;
    }

    public void setEtdOriginal(LocalDate etdOriginal) {
        this.etdOriginal = etdOriginal;
    }

    public String getPortOfDeparture() {
        return portOfDeparture;
    }

    public void setPortOfDeparture(String portOfDeparture) {
        this.portOfDeparture = portOfDeparture;
    }


    public void setSendmail(Boolean sendmail) {
        isSendmail = sendmail;
    }

    public Integer getNumberContainer() {
        return numberContainer;
    }

    public void setNumberContainer(Integer numberContainer) {
        this.numberContainer = numberContainer;
    }

    public String getPlOrder() {
        return plOrder;
    }

    public void setPlOrder(String plOrder) {
        this.plOrder = plOrder;
    }

    public Double getTotalCbm() {
        return totalCbm;
    }

    public void setTotalCbm(Double totalCbm) {
        this.totalCbm = totalCbm;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTotalGrossWeight() {
        return totalGrossWeight;
    }

    public void setTotalGrossWeight(Double totalGrossWeight) {
        this.totalGrossWeight = totalGrossWeight;
    }

    public String getUserSend() {
        return userSend;
    }

    public void setUserSend(String userSend) {
        this.userSend = userSend;
    }

    public LocalDate getAta() {
        return ata;
    }

    public void setAta(LocalDate ata) {
        this.ata = ata;
    }



    public Instant getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(Instant orderedDate) {
        this.orderedDate = orderedDate;
    }


    public Set<PurchaseOrdersWHDate> getPurchaseOrdersWHDate() {
        return purchaseOrdersWHDate;
    }

    public void setPurchaseOrdersWHDate(Set<PurchaseOrdersWHDate> purchaseOrdersWHDate) {
        if (this.purchaseOrdersWHDate != null) {
            this.purchaseOrdersWHDate.forEach(i -> i.setPurchaseOrdersWH(null));
        }
        if (purchaseOrdersWHDate != null) {
            purchaseOrdersWHDate.forEach(i -> i.purchaseOrdersWH(this));
        }
        this.purchaseOrdersWHDate = purchaseOrdersWHDate;
    }


    public Integer getId() {
        return this.id;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public String getCreatedBy() {
        return createdBy;
    }


    public PurchaseOrdersWH id(Integer id) {
        this.setId(id);
        return this;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public void setAtd(LocalDate atd) {
        this.atd = atd;
    }



    public String getPortOfLoading() {
        return portOfLoading;
    }

    public LocalDate getAtd() {
        return atd;
    }




    public Set<PurchaseOrdersWHDetail> getPurchaseOrdersWHDetail() {
        return purchaseOrdersWHDetail;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = updatedDate = new Date().toInstant();
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PurchaseOrdersWH)) {
            return false;
        }
        return id != null && id.equals(((PurchaseOrdersWH) o).id);
    }


    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setCountry(String country) {
        this.country = country;
    }




    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }


    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getTotalItem() {
        return totalItem;
    }

    public void setTotalItem(Integer totalItem) {
        this.totalItem = totalItem;
    }


    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDate getExpectedShipDate() {
        return expectedShipDate;
    }

    public void setExpectedShipDate(LocalDate expectedShipDate) {
        this.expectedShipDate = expectedShipDate;
    }

    public void setActualShipDate(LocalDate actualShipDate) {
        this.actualShipDate = actualShipDate;
    }

    public void setEtd(LocalDate etd) {
        this.etd = etd;
    }

    public void setEta(LocalDate eta) {
        this.eta = eta;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }



    public LocalDate getActualShipDate() {
        return actualShipDate;
    }

    public LocalDate getEtd() {
        return etd;
    }

    public LocalDate getEta() {
        return eta;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }




    public String getPoNumber() {
        return poNumber;
    }


    public String getVendorId() {
        return vendorId;
    }

    public String getCountry() {
        return country;
    }


    public String getShipmentId() {
        return shipmentId;
    }



    public Integer getChannel() {
        return channel;
    }



    public Integer getStatus() {
        return status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }




    public void setPurchaseOrdersWHDetail(Set<PurchaseOrdersWHDetail> purchaseOrdersWHDetail) {
        if (this.purchaseOrdersWHDetail != null) {
            this.purchaseOrdersWHDetail.forEach(i -> i.setPurchaseOrdersWH(null));
        }
        if (purchaseOrdersWHDetail != null) {
            purchaseOrdersWHDetail.forEach(i -> i.purchaseOrdersWH(this));
        }
        this.purchaseOrdersWHDetail = purchaseOrdersWHDetail;
    }

    public ProformaInvoiceWH getProformaInvoiceWH() {
        return proformaInvoiceWH;
    }

    public void setProformaInvoiceWH(ProformaInvoiceWH proformaInvoiceWH) {
        this.proformaInvoiceWH = proformaInvoiceWH;
    }



    public void setIsSendmail(Boolean isSendmail) {
        this.isSendmail = isSendmail;
    }

}
