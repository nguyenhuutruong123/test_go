package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.model.*;
import com.yes4all.service.BillOfLadingService;
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
public class BillOfLadingController {

    @Autowired
    private BillOfLadingService service;

    @PostMapping("/bill-of-lading/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody DetailObjectDTO request) {
        BillOfLadingDTO response = service.getBillOfLadingDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping(value = "/bill-of-lading/create")
    public ResponseEntity<RestResponse<Object>> createBillOfLading(@RequestBody @Validated BillOfLadingDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            BillOfLadingDTO result = service.createBillOfLading(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/bill-of-lading/submit")
    public ResponseEntity<RestResponse<Object>> submitBillOfLading(@RequestBody DetailObjectDTO request) {
        Boolean result = service.submitBOL(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
    @PostMapping(value = "/bill-of-lading/request-upload")
    public ResponseEntity<RestResponse<Object>> requestBillOfLading(@RequestBody DetailObjectDTO request) {
        Boolean result = service.requestBOL(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
    @PostMapping(value = "/bill-of-lading/confirm")
    public ResponseEntity<RestResponse<Object>> confirmBillOfLading(@RequestBody DetailObjectDTO request) {
        Boolean result = service.confirmBOL(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
    @PostMapping(value = "/bill-of-lading/update")
    public ResponseEntity<RestResponse<Object>> updateBillOfLading(@RequestBody @Validated BillOfLadingDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            BillOfLadingDTO result = service.updateBillOfLading(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/bill-of-lading/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO) {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams = CommonDataUtil.searchFilter(listingDTO);
        Page<BillOfLadingMainDTO> response = service.listingBillOfLadingWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/bill-of-lading/detail/export")
    public HttpEntity<ByteArrayResource> exportExcel(@RequestBody(required = false)
                                                     @Validated BillOfLadingExportExcelDTO dto) {
        String nameTemplate = "Booking.zip";
        byte[] result = service.exportExcel(dto);

        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameTemplate + "");
        return new HttpEntity<>(new ByteArrayResource(result), header);
    }

}
