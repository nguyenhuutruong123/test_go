package com.yes4all.service;

import java.time.LocalDate;

public interface IShipmentPurchaseOrdersDTO {

    Integer getId();

    String getPurchaseOrderNo();

    String getProformaInvoiceNo();

    String getSupplier();

    String getPortOfLoading();

    String getPortOfDeparture();

    String getDemand();

    String getContainers();

    LocalDate getEtd();

    LocalDate getEta();

    Integer getPurchaseOrderId();

    Integer getProformaInvoiceId();


}
