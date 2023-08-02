package com.yes4all.common.utils;

public class UploadResultDetail {

    private Integer index;
    private String fromSo;
    private String aSin;
    private String sku;
    private String status;
    private String note;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getFromSo() {
        return fromSo;
    }

    public UploadResultDetail(Integer index, String fromSo, String aSin, String sku, String status, String note) {
        this.index = index;
        this.fromSo = fromSo;
        this.aSin = aSin;
        this.sku = sku;
        this.status = status;
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setFromSo(String fromSo) {
        this.fromSo = fromSo;
    }

    public String getaSin() {
        return aSin;
    }

    public void setaSin(String aSin) {
        this.aSin = aSin;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
