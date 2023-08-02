package com.yes4all.service;

import com.yes4all.domain.ProformaInvoiceWH;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link ProformaInvoiceWH}.
 */
public interface ProformaInvoiceWHService {

    Page<ProformaInvoiceWHMainDTO> listingProformaInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    boolean removeProformaInvoice(List<Integer> listId, String userName);

    ProformaInvoiceWHDTO getProformaInvoiceDetail(BodyGetDetailDTO request);

    void export(String filename,Integer id,String userId) throws IOException;
    ProformaInvoiceWHDTO updateProformaInvoice(Integer id,ProformaInvoiceWHDTO proformaInvoiceDTO) throws IOException, URISyntaxException;


    boolean confirmed(ListIdDTO request);
    boolean confirmedDetailPI(ActionSingleIdDTO request);

    boolean changeStep(ActionSingleIdDTO request);
    boolean unConfirmed(List<Integer> id);
    boolean approved(List<Integer> id);
    boolean send(ListIdDTO request);

 }
