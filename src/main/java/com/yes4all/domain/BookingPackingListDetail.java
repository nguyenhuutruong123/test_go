package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;



/**
 * A PurchaseOrdersDetail.
 */
@Entity
@Table(name = "booking_packing_list_detail")
public class BookingPackingListDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "sku")
    private String sku;

    @Column(name = "title")
    private String title;

    @Column(name = "asin")
    private String aSin;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "quantity_previous")
    private Integer quantityPrevious;

    @Column(name = "qty_each_carton")
    private Integer qtyEachCarton;

    @Column(name = "qty_each_carton_previous")
    private Integer qtyEachCartonPrevious;

    @Column(name = "total_carton")
    private Double totalCarton;

    @Column(name = "total_carton_previous")
    private Double totalCartonPrevious;

    @Column(name = "net_weight")
    private Double netWeight;

    @Column(name = "net_weight_previous")
    private Double netWeightPrevious;

    @Column(name = "gross_weight")
    private Double grossWeight;

    @Column(name = "gross_weight_previous")
    private Double grossWeightPrevious;

    @Column(name = "cbm")
    private Double cbm;

    @Column(name = "cbm_previous")
    private Double cbmPrevious;

    @Column(name = "proforma_invoice_no")
    private String proformaInvoiceNo;

    @Column(name = "container")
    private String container;



    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"bookingPackingList"}, allowSetters = true)
    @JoinColumn(name = "booking_packing_list_id", referencedColumnName = "id")
    private BookingPackingList bookingPackingList;



    public String getProformaInvoiceNo() {
        return proformaInvoiceNo;
    }

    public void setProformaInvoiceNo(String proformaInvoiceNo) {
        this.proformaInvoiceNo = proformaInvoiceNo;
    }

    public void setBookingPackingList(BookingPackingList bookingPackingList) {
        this.bookingPackingList = bookingPackingList;
    }

    public BookingPackingList getBookingPackingList() {
        return bookingPackingList;
    }

    public BookingPackingListDetail bookingPackingList(BookingPackingList bookingPackingList) {
        this.setBookingPackingList(bookingPackingList);
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Integer getQuantityPrevious() {
        return quantityPrevious;
    }

    public void setQuantityPrevious(Integer quantityPrevious) {
        this.quantityPrevious = quantityPrevious;
    }

    public Integer getQtyEachCartonPrevious() {
        return qtyEachCartonPrevious;
    }

    public void setQtyEachCartonPrevious(Integer qtyEachCartonPrevious) {
        this.qtyEachCartonPrevious = qtyEachCartonPrevious;
    }

    public Double getTotalCartonPrevious() {
        return totalCartonPrevious;
    }

    public void setTotalCartonPrevious(Double totalCartonPrevious) {
        this.totalCartonPrevious = totalCartonPrevious;
    }

    public Double getNetWeightPrevious() {
        return netWeightPrevious;
    }

    public void setNetWeightPrevious(Double netWeightPrevious) {
        this.netWeightPrevious = netWeightPrevious;
    }

    public Double getGrossWeightPrevious() {
        return grossWeightPrevious;
    }

    public void setGrossWeightPrevious(Double grossWeightPrevious) {
        this.grossWeightPrevious = grossWeightPrevious;
    }

    public Double getCbmPrevious() {
        return cbmPrevious;
    }

    public void setCbmPrevious(Double cbmPrevious) {
        this.cbmPrevious = cbmPrevious;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getaSin() {
        return aSin;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQtyEachCarton() {
        return qtyEachCarton;
    }

    public void setQtyEachCarton(Integer qtyEachCarton) {
        this.qtyEachCarton = qtyEachCarton;
    }

    public Double getTotalCarton() {
        return totalCarton;
    }

    public void setTotalCarton(Double totalCarton) {
        this.totalCarton = totalCarton;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public Double getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public Double getCbm() {
        return cbm;
    }

    public void setCbm(Double cbm) {
        this.cbm = cbm;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }
}
