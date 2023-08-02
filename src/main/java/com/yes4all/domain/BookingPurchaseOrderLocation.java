package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;


/**
 * A PurchaseOrders.
 */
@Entity
@Table(name = "booking_purchase_order_location")
public class BookingPurchaseOrderLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;


    @Column(name = "po_number")
    private String poNumber;


    @Column(name = "ship_location")
    private String shipLocation;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"booking"}, allowSetters = true)
    @JoinColumn(name = "booking_id", referencedColumnName = "id")
    private Booking booking;

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
    public BookingPurchaseOrderLocation booking(Booking booking) {
        this.setBooking(booking);
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getShipLocation() {
        return shipLocation;
    }

    public void setShipLocation(String shipLocation) {
        this.shipLocation = shipLocation;
    }
}
