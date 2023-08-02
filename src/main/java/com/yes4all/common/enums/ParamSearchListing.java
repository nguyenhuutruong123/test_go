package com.yes4all.common.enums;

import com.yes4all.constants.GlobalConstant;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public enum ParamSearchListing {
    // 0-999 is normal
    PO_NUMBER(GlobalConstant.SEARCH_TEXT, "poNumber"),
    PO_AMAZON(GlobalConstant.SEARCH_TEXT, "poAmazon"),
    BOOKING_NUMBER(GlobalConstant.SEARCH_TEXT, "bookingNumber"),
    FULFILLMENT_CENTER(GlobalConstant.SEARCH_TEXT, "fulfillmentCenter"),
    COUNTRY(GlobalConstant.SEARCH_TEXT, "country"),
    STATUS(GlobalConstant.SEARCH_TEXT, "status"),
    SUPPLIER(GlobalConstant.SEARCH_TEXT, "supplierSearch"),
    INVOICE_NO_PI(GlobalConstant.SEARCH_TEXT, "invoiceNoPI"),
    BOOKING(GlobalConstant.SEARCH_TEXT, "booking"),
    PO_AMAZON_BOOKING(GlobalConstant.SEARCH_TEXT, "poAmazonBooking"),
    MASTER_PO_BOOKING(GlobalConstant.SEARCH_TEXT, "masterPOCIBooking"),
    BOL_NO_BOL(GlobalConstant.SEARCH_TEXT, "bolNoBOL"),
    BOOKING_NO_BOL(GlobalConstant.SEARCH_TEXT, "bookingNoBOL"),
    FROM_SO(GlobalConstant.SEARCH_TEXT, "fromSO"),
    INVOICE(GlobalConstant.SEARCH_TEXT, "invoiceNo"),
    TERM(GlobalConstant.SEARCH_TEXT, "term"),
    USER_ID(GlobalConstant.SEARCH_TEXT, "userId"),
    CONFIRMED_BY_PU(GlobalConstant.SEARCH_TEXT, "confirmedByPU"),
    CONFIRMED_BY_SC(GlobalConstant.SEARCH_TEXT, "confirmedBySC"),
    SHIPMENT_ID(GlobalConstant.SEARCH_TEXT, "shipmentId"),
    DEMAND(GlobalConstant.SEARCH_TEXT, "demand"),
    CONTAINER(GlobalConstant.SEARCH_TEXT, "container"),
    PORT_OF_LOADING(GlobalConstant.SEARCH_TEXT, "portOfLoading"),
    PORT_OF_DISCHARGE(GlobalConstant.SEARCH_TEXT, "portOfDischarge"),
    ETD_SHIPMENT(GlobalConstant.SEARCH_TEXT, "etdShipment"),
    ETA_SHIPMENT(GlobalConstant.SEARCH_TEXT, "etaShipment"),
    //search vietnamese
    UPDATED_BY(GlobalConstant.SEARCH_TEXT_VN, "updatedBy"),

    CREATED_BY(GlobalConstant.SEARCH_TEXT_VN, "createdBy"),
    // 1000-1999 is date from to
    ETD(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "etd"),
    ETA(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "eta"),
    ATD(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "atd"),
    ATA(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "ata"),
    EXPECTED_SHIP_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "expectedShipDate"),
    ACTUAL_SHIP_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "actualShipDate"),
    UPDATED_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "updatedDate"),
    DEADLINE_SUBMIT_BOOKING(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "deadlineSubmitBooking"),
    SHIP_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "shipDate"),
    CREATED_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "createdDate"),
    ORDERED_DATE(GlobalConstant.SEARCH_TEXT_DATE_FROM_TO, "orderedDate"),
    // >2000 is number
    AMOUNT(GlobalConstant.SEARCH_TEXT_NUMBER_FROM_TO, "amount"),

    ;


    private final String type;
    private final String name;

    ParamSearchListing(String type, String name) {
        this.type = type;
        this.name = name;
        ParamSearchListing.Holder.map.put(name, this);
    }

    private static class Holder {
        static Map<String, ParamSearchListing> map = new HashMap<>();
    }

    public static ParamSearchListing find(String id) {
        return ParamSearchListing.Holder.map.get(id);
    }

}
