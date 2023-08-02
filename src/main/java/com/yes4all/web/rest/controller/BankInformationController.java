package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.model.*;
import com.yes4all.service.BankInformationService;
import com.yes4all.service.IShipmentProformaInvoiceDTO;
import com.yes4all.service.IShipmentPurchaseOrdersDTO;
import com.yes4all.service.ShipmentService;
import com.yes4all.service.impl.UploadExcelService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;


@RestController
@RequestMapping("/api")
public class BankInformationController {

    @Autowired
    private BankInformationService service;

    private final Logger log = LoggerFactory.getLogger(BankInformationController.class);


    @PostMapping("/bank/list")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody DetailObjectDTO request) {
        List<BankInformationDTO> response = service.findAllBankInformationByVendor(request.getUserId());
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/bank/save")
    public ResponseEntity<RestResponse<Object>> saveBank(@RequestBody BankInformationDTO request) {
        BankInformationDTO response = service.saveBank(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

}
