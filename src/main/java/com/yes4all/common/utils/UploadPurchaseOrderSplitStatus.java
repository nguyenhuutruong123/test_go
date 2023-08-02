package com.yes4all.common.utils;

public class UploadPurchaseOrderSplitStatus {

    private String sku;
    private String status;
    private String message;





    public UploadPurchaseOrderSplitStatus(String sku, String status, String message ) {
        this.sku = sku;
        this.status = status;
        this.message = message;
    }


    public void setStatus(String status) {
        this.status = status;
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


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
