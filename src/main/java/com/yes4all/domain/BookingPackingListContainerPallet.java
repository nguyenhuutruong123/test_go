package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;


/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "booking_packing_list_container_pallet")
public class BookingPackingListContainerPallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "container")
    private String container;

    @Column(name = "total_pallet_qty")
    private Integer totalPalletQty;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"bookingPackingList"}, allowSetters = true)
    @JoinColumn(name = "booking_packing_list_id", referencedColumnName = "id")
    private BookingPackingList bookingPackingList;

    public void setBookingPackingList(BookingPackingList bookingPackingList) {
        this.bookingPackingList = bookingPackingList;
    }

    public BookingPackingList getBookingPackingList() {
        return bookingPackingList;
    }

    public BookingPackingListContainerPallet bookingPackingList(BookingPackingList bookingPackingList) {
        this.setBookingPackingList(bookingPackingList);
        return this;
    }

    public Integer getTotalPalletQty() {
        return totalPalletQty;
    }

    public void setTotalPalletQty(Integer totalPalletQty) {
        this.totalPalletQty = totalPalletQty;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }
}
