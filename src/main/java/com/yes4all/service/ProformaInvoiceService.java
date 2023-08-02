package com.yes4all.service;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link ProformaInvoice}.
 */
public interface ProformaInvoiceService {

    Page<ProformaInvoiceMainDTO> listingProformaInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    boolean removeProformaInvoice(List<Integer> listId, String userName);

    ProformaInvoiceDTO getProformaInvoiceDetail(BodyGetDetailDTO request);

    void export(String filename,Integer id,String userId) throws IOException;
   // ProformaInvoiceDTO createProformaInvoice(ProformaInvoiceDTO proformaInvoiceDTO,Boolean isSupplier) ;
    ProformaInvoiceDTO updateProformaInvoice(Integer iD,ProformaInvoiceDTO proformaInvoiceDTO) throws IOException, URISyntaxException;
    FileDTO exportExcelByIdPI(Integer id,String userId);

    boolean confirmed(List<Integer> id);
    boolean confirmedDetailPI(ActionSingleIdDTO request);

    boolean changeStep(ActionSingleIdDTO request);
    boolean unConfirmed(List<Integer> id);
    boolean approved(List<Integer> id);
    boolean send(ListIdDTO request);

 }
