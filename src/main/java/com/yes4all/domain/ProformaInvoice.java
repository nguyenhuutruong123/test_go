package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "proforma_invoice")
public class ProformaInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "seller")
    private String seller;

    @Column(name = "buyer")
    private String buyer;


    @NotNull
    @Column(name = "order_no")
    private String orderNo;


    @Column(name = "term")
    private String term;


    @NotNull
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "payment_term")
    private String paymentTerm;


    @Column(name = "status")
    private Integer status;


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

    @Column(name = "amount")
    private Double amount;

    @Column(name = "cbm_total")
    private Double cbmTotal;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "ctn")
    private Double ctn;

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

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "booking_number")
    private String bookingNumber;

    @Column(name = "fulfillment_center")
    private String fulfillmentCenter;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "is_supplier_adjust")
    private Boolean isSupplier;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    @Column(name = "new_version_detail")
    private Boolean newVersionDetail;

    @Column(name = "status_sourcing")
    private Integer statusSourcing;

    @Column(name = "status_pu")
    private Integer statusPU;

    @Column(name = "user_updated_latest")
    private Integer userUpdatedLatest;

    @Column(name = "step_action_by")
    private Integer stepActionBy;

    @Column(name = "user_pu_primary")
    private String userPUPrimary;

    @Column(name = "user_sourcing_primary")
    private String userSourcingPrimary;

    @Column(name = "version_latest_supplier")
    private Integer versionLatestSupplier;

    @Column(name = "version_latest_y4a")
    private Integer versionLatestY4a;

    @Column(name = "is_sendmail")
    private Boolean sendMail;
    @OneToMany(mappedBy = "proformaInvoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ProformaInvoiceDetail> proformaInvoiceDetail = new HashSet<>();

    public Boolean getSendMail() {
        return sendMail;
    }

    public void setSendMail(Boolean sendMail) {
        this.sendMail = sendMail;
    }

    @OneToOne(mappedBy = "proformaInvoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER)

    @JsonIgnore
    private PurchaseOrders purchaseOrders;

    public Integer getVersionLatestY4a() {
        return versionLatestY4a;
    }

    public void setVersionLatestY4a(Integer versionLatestY4a) {
        this.versionLatestY4a = versionLatestY4a;
    }

    public Integer getVersionLatestSupplier() {
        return versionLatestSupplier;
    }

    public void setVersionLatestSupplier(Integer versionLatestSupplier) {
        this.versionLatestSupplier = versionLatestSupplier;
    }

    public String getUserPUPrimary() {
        return userPUPrimary;
    }

    public void setUserPUPrimary(String userPUPrimary) {
        this.userPUPrimary = userPUPrimary;
    }

    public String getUserSourcingPrimary() {
        return userSourcingPrimary;
    }

    public void setUserSourcingPrimary(String userSourcingPrimary) {
        this.userSourcingPrimary = userSourcingPrimary;
    }

    public Integer getStatusPU() {
        return statusPU;
    }

    public void setStatusPU(Integer statusPU) {
        this.statusPU = statusPU;
    }

    public Integer getStepActionBy() {
        return stepActionBy;
    }

    public void setStepActionBy(Integer stepActionBy) {
        this.stepActionBy = stepActionBy;
    }

    public Boolean getNewVersionDetail() {
        return newVersionDetail;
    }

    public void setNewVersionDetail(Boolean newVersionDetail) {
        this.newVersionDetail = newVersionDetail;
    }

    public void setIsConfirmed(Boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public Boolean getIsConfirmed() {
        return isConfirmed;
    }

    public void setIsSupplier(Boolean isSupplier) {
        this.isSupplier = isSupplier;
    }
    public  Boolean getIsSupplier() {
       return isSupplier;
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

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getFulfillmentCenter() {
        return fulfillmentCenter;
    }

    public void setFulfillmentCenter(String fulfillmentCenter) {
        this.fulfillmentCenter = fulfillmentCenter;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setCbmTotal(Double cbmTotal) {
        this.cbmTotal = cbmTotal;
    }

    public Integer getStatusSourcing() {
        return statusSourcing;
    }

    public void setStatusSourcing(Integer statusSourcing) {
        this.statusSourcing = statusSourcing;
    }

    public Integer getUserUpdatedLatest() {
        return userUpdatedLatest;
    }

    public void setUserUpdatedLatest(Integer userUpdatedLatest) {
        this.userUpdatedLatest = userUpdatedLatest;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setCtn(Double ctn) {
        this.ctn = ctn;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getCbmTotal() {
        return cbmTotal;
    }

    public Double getGrossWeight() {
        return grossWeight;
    }

    public Double getCtn() {
        return ctn;
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



    public PurchaseOrders getPurchaseOrders() {
        return purchaseOrders;
    }

    public void setPurchaseOrders(PurchaseOrders purchaseOrders) {
        this.purchaseOrders = purchaseOrders;
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


    public ProformaInvoice id(Integer id) {
        this.setId(id);
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProformaInvoice)) {
            return false;
        }
        return id != null && id.equals(((ProformaInvoice) o).id);
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

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public String getOrderNo() {
        return orderNo;
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

    public Set<ProformaInvoiceDetail> getProformaInvoiceDetail() {
        return proformaInvoiceDetail;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setProformaInvoiceDetail(Set<ProformaInvoiceDetail> proformaInvoiceDetail) {
        if (this.proformaInvoiceDetail != null) {
            this.proformaInvoiceDetail.forEach(i -> i.setProformaInvoice(null));
        }
        if (proformaInvoiceDetail != null) {
            proformaInvoiceDetail.forEach(i -> i.proformaInvoice(this));
        }
        this.proformaInvoiceDetail = proformaInvoiceDetail;
    }


}
