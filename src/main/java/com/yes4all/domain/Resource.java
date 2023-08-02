package com.yes4all.domain;



import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Photo.
 */
@Entity
@Table(name = "Resource")
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "path")
    private String path;

    @Column(name = "module")
    private String module;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "upload_date")
    private Instant uploadDate;

    @Column(name = "proforma_invoice_id")
    private Integer proformaInvoiceId;

    @Column(name = "proforma_invoice_wh_id")
    private Integer proformaInvoiceWHId;
    @Column(name = "commercial_invoice_id")
    private Integer commercialInvoiceId;

    @Column(name = "commercial_invoice_wh_id")
    private Integer commercialInvoiceWHId;

    @Column(name = "packing_list_wh_id")
    private Integer packingListWhId;
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "bill_of_lading_id")
    private Integer billOfLadingId;

    @Column(name = "shipment_id")
    private Integer shipmentId;

    public Integer getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Integer getCommercialInvoiceWHId() {
        return commercialInvoiceWHId;
    }

    public void setCommercialInvoiceWHId(Integer commercialInvoiceWHId) {
        this.commercialInvoiceWHId = commercialInvoiceWHId;
    }

    public Integer getPackingListWhId() {
        return packingListWhId;
    }

    public void setPackingListWhId(Integer packingListWhId) {
        this.packingListWhId = packingListWhId;
    }

    public Integer getProformaInvoiceWHId() {
        return proformaInvoiceWHId;
    }

    public void setProformaInvoiceWHId(Integer proformaInvoiceWHId) {
        this.proformaInvoiceWHId = proformaInvoiceWHId;
    }

    public Integer getBillOfLadingId() {
        return billOfLadingId;
    }

    public void setBillOfLadingId(Integer billOfLadingId) {
        this.billOfLadingId = billOfLadingId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public void setProformaInvoiceId(Integer proformaInvoiceId) {
        this.proformaInvoiceId = proformaInvoiceId;
    }

    public void setCommercialInvoiceId(Integer commercialInvoiceId) {
        this.commercialInvoiceId = commercialInvoiceId;
    }

    public Integer getProformaInvoiceId() {
        return proformaInvoiceId;
    }

    public Integer getCommercialInvoiceId() {
        return commercialInvoiceId;
    }
// jhipster-needle-entity-add-field - JHipster will add fields here

    public Integer getId() {
        return this.id;
    }

    public Resource id(Integer id) {
        this.setId(id);
        return this;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Resource name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public Resource type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return this.path;
    }

    public Resource path(String path) {
        this.setPath(path);
        return this;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModule() {
        return this.module;
    }

    public Resource module(String module) {
        this.setModule(module);
        return this;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public Resource fileSize(Long fileSize) {
        this.setFileSize(fileSize);
        return this;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Instant getUploadDate() {
        return this.uploadDate;
    }

    public Resource uploadDate(Instant uploadDate) {
        this.setUploadDate(uploadDate);
        return this;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return this.fileType;
    }

    public Resource fileType(String fileType) {
        this.setFileType(fileType);
        return this;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }


// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        return id != null && id.equals(((Resource) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Photo{" + "id=" + getId() + ", name='" + getName() + "'" + ", type='" + getType() + "'" + ", path='" + getPath() + "'" + ", module='" + getModule() + "'" + ", fileSize=" + getFileSize() + ", uploadDate='" + getUploadDate() + "'" + "}";
    }
}
