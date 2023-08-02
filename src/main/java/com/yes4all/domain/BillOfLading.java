package com.yes4all.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "bill_of_lading")
public class BillOfLading implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Integer id;

    @Column(name = "bill_of_lading_no")
    private String billOfLadingNo;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "handling_charge")
    private Double handlingCharge;

    @Column(name = "customs_clearance_free")
    private Double customsClearanceFree;

    @Column(name = "us_custom")
    private Double usCustom;

    @Column(name = "other_fee")
    private Double otherFee;

    @Column(name = "atd")
    private LocalDate atd;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "broker_id")
    private String brokerId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "reason_reject")
    private String reason;


    @OneToMany(mappedBy = "billOfLading", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"booking"}, allowSetters = true)
    private Set<Booking> booking = new HashSet<>();

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    @PrePersist
    protected void onCreate() {
        updatedAt = new Date().toInstant();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date().toInstant();
    }

    public LocalDate getAtd() {
        return atd;
    }

    public void setAtd(LocalDate atd) {
        this.atd = atd;
    }

    public LocalDate getEta() {
        return eta;
    }

    public void setEta(LocalDate eta) {
        this.eta = eta;
    }

    public Set<Booking> getBooking() {
        return booking;
    }

    public void setBooking(Set<Booking> booking) {
        if (this.booking != null) {
            this.booking.forEach(i -> i.setBillOfLading(null));
        }
        if (booking != null) {
            booking.forEach(i -> i.billOfLading(this));
        }
        this.booking = booking;
    }
    public Double getHandlingCharge() {
        return handlingCharge;
    }

    public void setHandlingCharge(Double handlingCharge) {
        this.handlingCharge = handlingCharge;
    }

    public Double getCustomsClearanceFree() {
        return customsClearanceFree;
    }

    public void setCustomsClearanceFree(Double customsClearanceFree) {
        this.customsClearanceFree = customsClearanceFree;
    }

    public Double getUsCustom() {
        return usCustom;
    }

    public void setUsCustom(Double usCustom) {
        this.usCustom = usCustom;
    }

    public Double getOtherFee() {
        return otherFee;
    }

    public void setOtherFee(Double otherFee) {
        this.otherFee = otherFee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBillOfLadingNo() {
        return billOfLadingNo;
    }

    public void setBillOfLadingNo(String billOfLadingNo) {
        this.billOfLadingNo = billOfLadingNo;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BillOfLading)) return false;
        return id != null && id.equals(((BillOfLading) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
