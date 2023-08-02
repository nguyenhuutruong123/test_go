package com.yes4all.common.utils;

public class ResultUploadPackingListDetail {

    private Integer index;
    private String invoiceNo;
    private String poNumber;
    private String sku;
    private String aSin;
    private String status;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }


    public ResultUploadPackingListDetail(Integer index, String invoiceNo, String poNumber, String sku, String aSin, String status) {
        this.index = index;
        this.invoiceNo = invoiceNo;
        this.poNumber = poNumber;
        this.sku = sku;
        this.aSin = aSin;
        this.status = status;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
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
