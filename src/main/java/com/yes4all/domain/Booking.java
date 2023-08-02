package com.yes4all.domain;

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
@Table(name = "booking")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;


    @NotNull
    @Column(name = "booking_confirmation")
    private String bookingConfirmation;


    @Column(name = "port_of_discharge")
    private String portOfDischarge;

    @Column(name = "container")
    private String container;

    @Column(name = "invoice")
    private String invoice;

    @Column(name = "port_of_loading")
    private String portOfLoading;

    @Column(name = "cds")
    private Instant cds;

    @Column(name = "destination")
    private String destination;

    @Column(name = "origin_etd")
    private LocalDate originEtd;

    @Column(name = "freight_mode")
    private String freightMode;

    @Column(name = "discharge_eta")
    private LocalDate dischargeEta;

    @Column(name = "fcr_no")
    private String fcrNo;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "po_dest")
    private String poDest;

    @Column(name = "freight_terms")
    private String freightTerms;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "ship_to_location")
    private String shipToLocation;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "stuffing_location")
    private String stuffingLocation;

    @Column(name = "status")
    private Integer status;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "type")
    private String type;

    @Column(name = "consolidator")
    private String consolidator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"billOfLading"}, allowSetters = true)
    @JoinColumn(name = "bill_of_lading_id", referencedColumnName = "id")
    private BillOfLading billOfLading;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingPurchaseOrder"}, allowSetters = true)
    private Set<BookingPurchaseOrder> bookingPurchaseOrder = new HashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingProformaInvoice"}, allowSetters = true)
    private Set<BookingProformaInvoice> bookingProformaInvoice = new HashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"bookingPurchaseOrderLocation"}, allowSetters = true)
    private Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocation = new HashSet<>();


    @PreUpdate
    protected void onUpdate() {
        updatedAt  = new Date().toInstant();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConsolidator() {
        return consolidator;
    }

    public void setConsolidator(String consolidator) {
        this.consolidator = consolidator;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPoDest() {
        return poDest;
    }

    public void setPoDest(String poDest) {
        this.poDest = poDest;
    }

    public void setBookingPurchaseOrder(Set<BookingPurchaseOrder> bookingPurchaseOrder) {
        if (this.bookingPurchaseOrder != null) {
            this.bookingPurchaseOrder.forEach(i -> i.setBooking(null));
        }
        if (bookingPurchaseOrder != null) {
            bookingPurchaseOrder.forEach(i -> i.booking(this));
        }
        this.bookingPurchaseOrder = bookingPurchaseOrder;
    }

    public Set<BookingPurchaseOrderLocation> getBookingPurchaseOrderLocation() {
        return bookingPurchaseOrderLocation;
    }
    public void setBookingPurchaseOrderLocation(Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocation) {
        if (this.bookingPurchaseOrderLocation != null) {
            this.bookingPurchaseOrderLocation.forEach(i -> i.setBooking(null));
        }
        if (bookingPurchaseOrderLocation != null) {
            bookingPurchaseOrderLocation.forEach(i -> i.booking(this));
        }
        this.bookingPurchaseOrderLocation = bookingPurchaseOrderLocation;
    }

    public Set<BookingPurchaseOrder> getBookingPurchaseOrder() {
        return bookingPurchaseOrder;
    }


    public void setBookingProformaInvoice(Set<BookingProformaInvoice> bookingProformaInvoice) {
        if (this.bookingProformaInvoice != null) {
            this.bookingProformaInvoice.forEach(i -> i.setBooking(null));
        }
        if (bookingProformaInvoice != null) {
            bookingProformaInvoice.forEach(i -> i.booking(this));
        }
        this.bookingProformaInvoice = bookingProformaInvoice;
    }
    public void setBillOfLading(BillOfLading billOfLading) {
        this.billOfLading = billOfLading;
    }
    public BillOfLading getBillOfLading() {
        return billOfLading;
    }
    public Booking billOfLading(BillOfLading billOfLading) {
        this.setBillOfLading(billOfLading);
        return this;
    }
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = new Date().toInstant();
    }

    public Set<BookingProformaInvoice> getBookingProformaInvoice() {
        return bookingProformaInvoice;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public void setBookingConfirmation(String bookingConfirmation) {
        this.bookingConfirmation = bookingConfirmation;
    }

    public void setPortOfDischarge(String portOfDischarge) {
        this.portOfDischarge = portOfDischarge;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public void setCds(Instant cds) {
        this.cds = cds;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setOriginEtd(LocalDate originEtd) {
        this.originEtd = originEtd;
    }

    public void setFreightMode(String freightMode) {
        this.freightMode = freightMode;
    }

    public void setDischargeEta(LocalDate dischargeEta) {
        this.dischargeEta = dischargeEta;
    }

    public void setFcrNo(String fcrNo) {
        this.fcrNo = fcrNo;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }


    public void setFreightTerms(String freightTerms) {
        this.freightTerms = freightTerms;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setShipToLocation(String shipToLocation) {
        this.shipToLocation = shipToLocation;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public void setStuffingLocation(String stuffingLocation) {
        this.stuffingLocation = stuffingLocation;
    }

    public Integer getId() {
        return id;
    }

    public String getBookingConfirmation() {
        return bookingConfirmation;
    }

    public String getPortOfDischarge() {
        return portOfDischarge;
    }

    public String getContainer() {
        return container;
    }

    public String getInvoice() {
        return invoice;
    }

    public String getPortOfLoading() {
        return portOfLoading;
    }

    public Instant getCds() {
        return cds;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getOriginEtd() {
        return originEtd;
    }

    public String getFreightMode() {
        return freightMode;
    }

    public LocalDate getDischargeEta() {
        return dischargeEta;
    }

    public String getFcrNo() {
        return fcrNo;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public String getFreightTerms() {
        return freightTerms;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getShipToLocation() {
        return shipToLocation;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public String getStuffingLocation() {
        return stuffingLocation;
    }
}
