package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "booking_packing_list")
public class BookingPackingList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "from_company")
    private String fromCompany;

    @Column(name = "sold_to_company")
    private String soldToCompany;

    @Column(name = "from_address")
    private String fromAddress;

    @Column(name = "sold_to_address")
    private String soldToAddress;

    @Column(name = "from_fax")
    private String fromFax;

    @Column(name = "sold_to_fax")
    private String soldToFax;

    @Column(name = "invoice")
    private String invoice;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "status")
    private Integer status;

    @Column(name = "sold_to_telephone")
    private String soldToTelephone;

    @Column(name = "from_telephone")
    private String fromTelephone;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "is_sendmail")
    private Boolean sendMail;

    @OneToMany(mappedBy = "bookingPackingList", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingProformaInvoice"}, allowSetters = true)
    private Set<BookingProformaInvoice> bookingProformaInvoice;

    @OneToMany(mappedBy = "bookingPackingList", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingPackingListDetail"}, allowSetters = true)
    private Set<BookingPackingListDetail> bookingPackingListDetail = new HashSet<>();

    @OneToMany(mappedBy = "bookingPackingList", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingPackingListContainerPallet"}, allowSetters = true)
    private Set<BookingPackingListContainerPallet> bookingPackingListContainerPallet = new HashSet<>();

    public Boolean getSendMail() {
        return sendMail;
    }

    public void setSendMail(Boolean sendMail) {
        this.sendMail = sendMail;
    }

    public Set<BookingProformaInvoice> getBookingProformaInvoice() {
        return bookingProformaInvoice;
    }

    public void setBookingProformaInvoice(Set<BookingProformaInvoice> bookingProformaInvoice) {
        this.bookingProformaInvoice = bookingProformaInvoice;
    }
    public void setBookingPackingList(Set<BookingProformaInvoice> bookingProformaInvoice) {
        if (this.bookingProformaInvoice != null) {
            this.bookingProformaInvoice.forEach(i -> i.setBooking(null));
        }
        if (bookingProformaInvoice != null) {
            bookingProformaInvoice.forEach(i -> i.bookingPackingList(this));
        }
        this.bookingProformaInvoice = bookingProformaInvoice;
    }
    @OneToOne(mappedBy = "bookingPackingList", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private CommercialInvoice commercialInvoice;
    @PreUpdate
    protected void onUpdate() {
        updatedAt  = new Date().toInstant();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt  = new Date().toInstant();
    }
    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSoldToTelephone() {
        return soldToTelephone;
    }

    public void setSoldToTelephone(String soldToTelephone) {
        this.soldToTelephone = soldToTelephone;
    }

    public String getFromTelephone() {
        return fromTelephone;
    }

    public void setFromTelephone(String fromTelephone) {
        this.fromTelephone = fromTelephone;
    }



    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public CommercialInvoice getCommercialInvoice() {
        return commercialInvoice;
    }

    public void setCommercialInvoice(CommercialInvoice commercialInvoice) {
        this.commercialInvoice = commercialInvoice;
    }

    public void setBookingPackingListDetail(Set<BookingPackingListDetail> bookingPackingListDetail) {
        if (this.bookingPackingListDetail != null) {
            this.bookingPackingListDetail.forEach(i -> i.setBookingPackingList(null));
        }
        if (bookingPackingListDetail != null) {
            bookingPackingListDetail.forEach(i -> i.bookingPackingList(this));
        }
        this.bookingPackingListDetail = bookingPackingListDetail;
    }

    public Set<BookingPackingListDetail> getBookingPackingListDetail() {
        return bookingPackingListDetail;
    }

    public void setBookingPackingListContainerPallet(Set<BookingPackingListContainerPallet> bookingPackingListContainerPallet) {
        if (this.bookingPackingListContainerPallet != null) {
            this.bookingPackingListContainerPallet.forEach(i -> i.setBookingPackingList(null));
        }
        if (bookingPackingListContainerPallet != null) {
            bookingPackingListContainerPallet.forEach(i -> i.bookingPackingList(this));
        }
        this.bookingPackingListContainerPallet = bookingPackingListContainerPallet;
    }

    public Set<BookingPackingListContainerPallet> getBookingPackingListContainerPallet() {
        return bookingPackingListContainerPallet;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFromCompany(String fromCompany) {
        this.fromCompany = fromCompany;
    }

    public void setSoldToCompany(String soldToCompany) {
        this.soldToCompany = soldToCompany;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setSoldToAddress(String soldToAddress) {
        this.soldToAddress = soldToAddress;
    }

    public void setFromFax(String fromFax) {
        this.fromFax = fromFax;
    }

    public void setSoldToFax(String soldToFax) {
        this.soldToFax = soldToFax;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getId() {
        return id;
    }

    public String getFromCompany() {
        return fromCompany;
    }

    public String getSoldToCompany() {
        return soldToCompany;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getSoldToAddress() {
        return soldToAddress;
    }

    public String getFromFax() {
        return fromFax;
    }

    public String getSoldToFax() {
        return soldToFax;
    }

    public String getInvoice() {
        return invoice;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }


}
