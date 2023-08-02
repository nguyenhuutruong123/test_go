package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.model.*;
import com.yes4all.service.CommercialInvoiceService;
import com.yes4all.web.rest.payload.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommercialInvoiceController {


    @Autowired
    private CommercialInvoiceService service;


    @PostMapping("/commercial-invoice/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody @Validated BodyGetDetailDTO request) {
        CommercialInvoiceDTO response = service.getCommercialInvoiceDetail(request, false);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/commercial-invoice/detail/{invoiceNo}")
    public ResponseEntity<RestResponse<Object>> findWithInvoiceNo(@PathVariable String invoiceNo) {
        CommercialInvoiceDTO response = service.getCommercialInvoiceDetailWithInvoiceNo(invoiceNo);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping(value = "/commercial-invoice")
    public ResponseEntity<RestResponse<Object>> submitCommercialInvoice(@RequestBody @Validated CommercialInvoiceDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            CommercialInvoiceDTO result = service.createCommercialInvoice(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/commercial-invoice/{commercialInvoiceId}")
    public ResponseEntity<RestResponse<Object>> submitCommercialInvoice(@PathVariable Integer commercialInvoiceId, @RequestBody @Validated CommercialInvoiceDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            CommercialInvoiceDTO result = service.updateCommercialInvoice(commercialInvoiceId, request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/commercial-invoice/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO) {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams = CommonDataUtil.searchFilter(listingDTO);
        Page<CommercialInvoiceMainDTO> response = service.listingCommercialInvoiceWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }



    @PostMapping("/commercial-invoice/confirmed")
    public ResponseEntity<RestResponse<Object>> confirm(@RequestBody() @Validated ListingIdSupplierDTO request) {
        boolean isConfirmed = service.confirmed(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }
    @PostMapping("/commercial-invoice/send")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody ListingIdSupplierDTO request) {
        boolean isSent = service.send(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isSent).build());
    }

    @GetMapping("/commercial-invoice/{id}/export-excel")
    public HttpEntity<ByteArrayResource> exportExcel(@PathVariable Integer id){
        FileDTO result = service.exportExcelById(id);

        if (CommonDataUtil.isNull(result) || CommonDataUtil.isEmpty(result.getFileName())) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFileName() + "");
        return new HttpEntity<>(new ByteArrayResource(result.getContent()), header);
    }

    @GetMapping("/commercial-invoice/{id}/export-excel-bol")
    public HttpEntity<ByteArrayResource> exportExcelBol(@PathVariable Integer id){
        FileDTO result = service.exportExcelByIdCI(id);

        if (CommonDataUtil.isNull(result) || CommonDataUtil.isEmpty(result.getFileName())) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFileName() + "");
        return new HttpEntity<>(new ByteArrayResource(result.getContent()), header);
    }


}
