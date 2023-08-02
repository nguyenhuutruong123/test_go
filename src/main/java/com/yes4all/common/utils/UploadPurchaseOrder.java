package com.yes4all.common.utils;

import java.util.Map;

public class UploadPurchaseOrder {

    private String namePoNumber;
    private String status;
    private String message;


    private Map<String, Integer> resultUploadDetail;

    public void setResultUploadDetail(Map<String, Integer> resultUploadDetail) {
        this.resultUploadDetail = resultUploadDetail;
    }

    public Map<String, Integer> getResultUploadDetail() {
        return resultUploadDetail;
    }

    public UploadPurchaseOrder(String namePoNumber, String status, String message, Map<String, Integer> resultUploadDetail) {
        this.namePoNumber = namePoNumber;
        this.status = status;
        this.message = message;
        this.resultUploadDetail = resultUploadDetail;
    }

    public void setNamePoNumber(String namePoNumber) {
        this.namePoNumber = namePoNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNamePoNumber() {
        return namePoNumber;
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
