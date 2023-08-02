package com.yes4all.service;

import java.time.LocalDate;

public interface IShipmentProformaInvoiceDTO {

    Integer getId();

    String getProformaInvoiceNo();
    Integer getProformaInvoiceId();
    String getSupplier();

    LocalDate getEtd();

    LocalDate getEta();

    String getPortOfDeparture();

    String getPortOfLoading();

}
