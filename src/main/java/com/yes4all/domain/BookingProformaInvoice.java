package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "booking_proforma_invoice")
public class BookingProformaInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"booking"}, allowSetters = true)
    @JoinColumn(name = "booking_id", referencedColumnName = "id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"bookingPackingList"}, allowSetters = true)
    @JoinColumn(name = "booking_packing_list_id", referencedColumnName = "id")
    private BookingPackingList bookingPackingList;


    @Column(name = "proforma_invoice_no")
    private String proformaInvoiceNo;

    @Column(name = "po_amazon")
    private String poAmazon;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "ctn")
    private Double ctn;

    @Column(name = "cbm")
    private Double cbm;

    @Column(name = "supplier")
    private String supplier;

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getCtn() {
        return ctn;
    }

    public void setCtn(Double ctn) {
        this.ctn = ctn;
    }

    public Double getCbm() {
        return cbm;
    }

    public void setCbm(Double cbm) {
        this.cbm = cbm;
    }

    public String getPoAmazon() {
        return poAmazon;
    }

    public void setPoAmazon(String poAmazon) {
        this.poAmazon = poAmazon;
    }

    public LocalDate getShipDate() {
        return shipDate;
    }

    public void setShipDate(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    public String getProformaInvoiceNo() {
        return proformaInvoiceNo;
    }

    public void setProformaInvoiceNo(String proformaInvoiceNo) {
        this.proformaInvoiceNo = proformaInvoiceNo;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }

    public BookingProformaInvoice booking(Booking booking) {
        this.setBooking(booking);
        return this;
    }

    public BookingProformaInvoice bookingPackingList(BookingPackingList bookingPackingList) {
        this.setBookingPackingList(bookingPackingList);
        return this;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }



    public BookingPackingList getBookingPackingList() {
        return bookingPackingList;
    }

    public void setBookingPackingList(BookingPackingList bookingPackingList) {
        this.bookingPackingList = bookingPackingList;
    }




}
