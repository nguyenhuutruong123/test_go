package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.model.*;
import com.yes4all.service.ProformaInvoiceService;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;

@RestController
@RequestMapping("/api")
public class ProformaInvoiceController {
    private final Logger log = LoggerFactory.getLogger(ProformaInvoiceController.class);


    @Autowired
    private ProformaInvoiceService service;
    @Autowired
    private UploadExcelService uploadExcelService;

    @PostMapping("/proforma-invoice/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody @Validated BodyGetDetailDTO request) {
        ProformaInvoiceDTO response = service.getProformaInvoiceDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


//    @PostMapping(value = "/proforma-invoice")
//    public ResponseEntity<RestResponse<Object>> submitProformaInvoice(@RequestBody @Validated ProformaInvoiceDTO request) {
//        if (CommonDataUtil.isNotNull(request)) {
//            ProformaInvoiceDTO result = service.createProformaInvoice(request, request.getIsSupplier());
//            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
//        }
//        return ResponseEntity.notFound().build();
//    }

    @PutMapping(value = "/proforma-invoice/{proformaInvoiceId}")
    public ResponseEntity<RestResponse<Object>> submitProformaInvoice(@PathVariable Integer proformaInvoiceId, @RequestBody ProformaInvoiceDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            ProformaInvoiceDTO result = service.updateProformaInvoice(proformaInvoiceId, request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/proforma-invoice/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file, @RequestParam("id") Integer id, @RequestParam("userId") String userId, @RequestParam("isNewVersion") Boolean isNewVersion) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailPI(file, id, userId, isNewVersion);
                response.setMessage(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setMessage(message);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
            }
        }
        message = "Please upload an excel file!";
        response.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());

    }

    @PostMapping("/proforma-invoice/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO) {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams = CommonDataUtil.searchFilter(listingDTO);
        Page<ProformaInvoiceMainDTO> response = service.listingProformaInvoiceWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping("/proforma-invoice/delete")
    public ResponseEntity<List<Integer>> removeProformaInvoice(@RequestBody() @Validated ListIdDTO request) {
        boolean isRemoved = service.removeProformaInvoice(request.getId(), request.getUserId());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/proforma-invoice/confirmed-detail")
    public ResponseEntity<RestResponse<Object>> confirmDetail(@RequestBody() @Validated ActionSingleIdDTO request) {
        boolean isConfirmed = service.confirmedDetailPI(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/proforma-invoice/change-step")
    public ResponseEntity<RestResponse<Object>> changeStep(@RequestBody() @Validated ActionSingleIdDTO request) {
        boolean isConfirmed = service.changeStep(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/proforma-invoice/confirmed")
    public ResponseEntity<RestResponse<Object>> confirm(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.confirmed(request.getId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/proforma-invoice/un-confirmed")
    public ResponseEntity<RestResponse<Object>> unConfirm(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.unConfirmed(request.getId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/proforma-invoice/supplier-approved")
    public ResponseEntity<RestResponse<Object>> approved(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.approved(request.getId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/proforma-invoice/send")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.send(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @GetMapping(value = "/proforma-invoice/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable Integer id, @RequestParam("userId") String userId) {
        try {
            log.info("START download PI template");
            String nameTemplate = "Proforma-Invoice.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath + "/" + nameTemplate);
            String fileName = file.getPath();
            service.export(fileName, id, userId);
            byte[] excelContent = Files.readAllBytes(file.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameTemplate + "");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (IOException ex) {
            log.error(String.format("Exception export excel %s %s.", ex.getMessage(), ex));
            return null;
        }
    }

    @GetMapping(value = "/proforma-invoice/export-general/{id}")
    public HttpEntity<ByteArrayResource> exportExcelBol(@PathVariable Integer id, @RequestParam("userId") String userId) {
        FileDTO result = service.exportExcelByIdPI(id, userId);
        if (CommonDataUtil.isNull(result) || CommonDataUtil.isEmpty(result.getFileName())) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFileName() + "");
        return new HttpEntity<>(new ByteArrayResource(result.getContent()), header);
    }
}
