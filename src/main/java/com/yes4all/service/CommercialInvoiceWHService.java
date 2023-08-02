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
public interface CommercialInvoiceWHService {

    FileDTO exportExcelByIdCI(Integer id);

 }
