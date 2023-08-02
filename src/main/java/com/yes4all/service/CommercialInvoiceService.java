package com.yes4all.service;

import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Service Interface for managing {@link CommercialInvoice}.
 */
public interface CommercialInvoiceService {

    Page<CommercialInvoiceMainDTO> listingCommercialInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams);


    CommercialInvoiceDTO getCommercialInvoiceDetail(BodyGetDetailDTO request,Boolean isViewPKL);
    CommercialInvoiceDTO getCommercialInvoiceDetailWithInvoiceNo(String invoiceNo);
    CommercialInvoiceDTO createCommercialInvoice(CommercialInvoiceDTO commercialInvoiceDTO) throws IOException, URISyntaxException;
    CommercialInvoiceDTO updateCommercialInvoice(Integer iD,CommercialInvoiceDTO commercialInvoiceDTO) throws IOException, URISyntaxException;

    boolean confirmed(ListingIdSupplierDTO id);
    boolean send(ListingIdSupplierDTO request);

    FileDTO exportExcel(Integer bookingPackingListId);

    FileDTO exportExcelByIdCI(Integer id);

    FileDTO exportExcelById(Integer id);
 }
