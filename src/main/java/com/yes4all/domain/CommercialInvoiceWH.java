package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Where;

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
@Table(name = "commercial_invoice_wh")
public class CommercialInvoiceWH implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "seller")
    private String seller;

    @Column(name = "buyer")
    private String buyer;


    @NotNull
    @Column(name = "invoice_no")
    private String invoiceNo;


    @Column(name = "term")
    private String term;



    @Column(name = "date")
    private LocalDate date;

    @Column(name = "payment_term")
    private String paymentTerm;


    @Column(name = "status")
    private Integer status;


    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "remark")
    private String remark;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "ac_number")
    private String acNumber;

    @Column(name = "beneficiary_bank")
    private String beneficiaryBank;

    @Column(name = "swift_code")
    private String swiftCode;


    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "updated_date")
    private Instant updatedDate;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_date")
    private Instant deletedDate;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "fulfillment_center")
    private String fulfillmentCenter;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "number_po")
    private String numberPO;

    @Column(name = "trucking_cost")
    private Double truckingCost;

    @Column(name = "amount_log_reject")
    private Double amountLogReject;

    @Column(name = "version_log_reject")
    private Integer versionLogReject;

    @Column(name = "the_first_reject")
    private Boolean theFirstReject;

    @Column(name = "supplier_updated_latest")
    private Boolean supplierUpdatedLatest;

    @OneToMany(mappedBy = "commercialInvoiceWH", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<CommercialInvoiceWHDetail> commercialInvoiceWHDetail  = new HashSet<>();

    @OneToMany(mappedBy = "commercialInvoiceWH", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<CommercialInvoiceWHTotalAmountLog> commercialInvoiceWHTotalAmountLog = new HashSet<>();

    @OneToOne(mappedBy = "commercialInvoiceWH", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    private ShipmentsPackingList shipmentsPackingList;


    public Boolean getSupplierUpdatedLatest() {
        return supplierUpdatedLatest;
    }

    public void setSupplierUpdatedLatest(Boolean supplierUpdatedLatest) {
        this.supplierUpdatedLatest = supplierUpdatedLatest;
    }

    public Boolean getTheFirstReject() {
        return theFirstReject;
    }

    public void setTheFirstReject(Boolean theFirstReject) {
        this.theFirstReject = theFirstReject;
    }

    public Double getAmountLogReject() {
        return amountLogReject;
    }

    public void setAmountLogReject(Double amountLogReject) {
        this.amountLogReject = amountLogReject;
    }

    public Integer getVersionLogReject() {
        return versionLogReject;
    }

    public void setVersionLogReject(Integer versionLogReject) {
        this.versionLogReject = versionLogReject;
    }

    public String getNumberPO() {
        return numberPO;
    }

    public void setNumberPO(String numberPO) {
        this.numberPO = numberPO;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getFulfillmentCenter() {
        return fulfillmentCenter;
    }

    public void setFulfillmentCenter(String fulfillmentCenter) {
        this.fulfillmentCenter = fulfillmentCenter;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Double getTruckingCost() {
        return truckingCost;
    }

    public void setTruckingCost(Double truckingCost) {
        this.truckingCost = truckingCost;
    }

    public ShipmentsPackingList getShipmentsPackingList() {
        return shipmentsPackingList;
    }

    public void setShipmentsPackingList(ShipmentsPackingList shipmentsPackingList) {
        this.shipmentsPackingList = shipmentsPackingList;
    }

    public Integer getId() {
        return this.id;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }


    public String getInvoiceNo() {
        return invoiceNo;
    }


    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public String getCreatedBy() {
        return createdBy;
    }


    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = updatedDate = new Date().toInstant();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new Date().toInstant();
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }


    public String getUpdatedBy() {
        return updatedBy;
    }


    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }


    public void setTerm(String term) {
        this.term = term;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }


    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setAcNumber(String acNumber) {
        this.acNumber = acNumber;
    }

    public void setBeneficiaryBank(String beneficiaryBank) {
        this.beneficiaryBank = beneficiaryBank;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getSeller() {
        return seller;
    }

    public String getBuyer() {
        return buyer;
    }


    public String getTerm() {
        return term;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPaymentTerm() {
        return paymentTerm;
    }


    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public String getRemark() {
        return remark;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAcNumber() {
        return acNumber;
    }

    public String getBeneficiaryBank() {
        return beneficiaryBank;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public Set<CommercialInvoiceWHDetail> getCommercialInvoiceWHDetail() {
        return commercialInvoiceWHDetail;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setCommercialInvoiceWHDetail(Set<CommercialInvoiceWHDetail> commercialInvoiceWHDetail) {
        if (this.commercialInvoiceWHDetail != null) {
            this.commercialInvoiceWHDetail.forEach(i -> i.setCommercialInvoiceWH(null));
        }
        if (commercialInvoiceWHDetail != null) {
            commercialInvoiceWHDetail.forEach(i -> i.commercialInvoiceWH(this));
        }
        this.commercialInvoiceWHDetail = commercialInvoiceWHDetail;
    }

    public Set<CommercialInvoiceWHTotalAmountLog> getCommercialInvoiceWHTotalAmountLog() {
        return commercialInvoiceWHTotalAmountLog;
    }
    public void setCommercialInvoiceWHTotalAmountLog(Set<CommercialInvoiceWHTotalAmountLog> commercialInvoiceWHTotalAmountLog) {
        if (this.commercialInvoiceWHTotalAmountLog != null) {
            this.commercialInvoiceWHTotalAmountLog.forEach(i -> i.setCommercialInvoiceWH(null));
        }
        if (commercialInvoiceWHTotalAmountLog != null) {
            commercialInvoiceWHTotalAmountLog.forEach(i -> i.commercialInvoiceWH(this));
        }
        this.commercialInvoiceWHTotalAmountLog = commercialInvoiceWHTotalAmountLog;
    }

}
