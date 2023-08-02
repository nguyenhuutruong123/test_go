package com.yes4all.service;

import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.model.BillOfLadingDTO;
import com.yes4all.domain.model.BillOfLadingExportExcelDTO;
import com.yes4all.domain.model.BillOfLadingMainDTO;
import com.yes4all.domain.model.DetailObjectDTO;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Service Interface for managing {@link CommercialInvoice}.
 */
public interface BillOfLadingService {

    Page<BillOfLadingMainDTO> listingBillOfLadingWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    BillOfLadingDTO getBillOfLadingDetail(DetailObjectDTO request);
    Boolean submitBOL(DetailObjectDTO request);
    Boolean requestBOL(DetailObjectDTO request);
    Boolean confirmBOL(DetailObjectDTO request);

    BillOfLadingDTO createBillOfLading(BillOfLadingDTO commercialInvoiceDTO) throws IOException, URISyntaxException;
    BillOfLadingDTO updateBillOfLading(BillOfLadingDTO billOfLadingDTO) throws IOException, URISyntaxException;
    byte[] exportExcel(BillOfLadingExportExcelDTO dto);
 }
