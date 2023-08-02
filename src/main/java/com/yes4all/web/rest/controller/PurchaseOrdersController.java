package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.model.*;
import com.yes4all.service.PurchaseOrdersService;


import com.yes4all.service.impl.UploadExcelService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;

@RestController
@RequestMapping("/api")
public class PurchaseOrdersController {
    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private PurchaseOrdersService service;
    @Value("${attribute.template.path}")
    private String templatePath;
    private static final String NAME_TEMPLATE = "/PurchaseOrder_Template.xlsx";


    @PostMapping("/purchase-order/detail")
    public ResponseEntity<RestResponse<Object>> findDetailsWithFilter(@RequestBody @Validated BodyGetDetailDTO request) {
        PurchaseOrderDetailPageDTO response = service.getPurchaseOrdersDetailWithFilter(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/purchase-order/{orderNo}")
    public ResponseEntity<RestResponse<Object>> findDetailsWithFilter(@PathVariable String orderNo, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit)  {
        PurchaseOrderDetailPageDTO response = service.getPurchaseOrdersDetailWithFilterOrderNo(orderNo, page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }


    @PostMapping("/purchase-order/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO)   {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams=CommonDataUtil.searchFilter(listingDTO);
        Page<PurchaseOrdersMainDTO> response = service.listingPurchaseOrdersWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

//    @PostMapping("/purchase-order/detail/delete/{purchaseOrderId}")
//    public ResponseEntity<List<Integer>> removeSkuFromDetails(@PathVariable Integer purchaseOrderId, @RequestBody() @Validated ListIdDTO request) {
//        boolean isRemoved = service.removeSkuFromDetails(purchaseOrderId, request.getId(), request.getUserId());
//        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
//
//    }

    @PostMapping("/purchase-order/delete")
    public ResponseEntity<List<Integer>> removePurchaseOrders(@RequestBody() @Validated ListIdDTO request)   {
        boolean isRemoved = service.removePurchaseOrders(request.getId(), request.getUserId());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/purchase-order/create-proforma-invoice")
    public ResponseEntity<RestResponse<Object>> createProformaInvoice(@RequestBody() @Validated ListIdDTO request) {
        ProformaInvoiceDTO result = service.createProformaInvoice(request.getId().get(0), request.getUserId());
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
//    @PostMapping("/purchase-order/get-list-sku")
//    public ResponseEntity<RestResponse<Object>> getListSkuFromPO(@RequestBody() @Validated ListIdDTO request) throws JsonProcessingException {
//        ListDetailPODTO result = service.getListSkuFromPO(request.getId());
//        if (!CommonDataUtil.isNotNull(result)) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
//    }

    //    @PostMapping("/purchase-order/confirmed")
//    public ResponseEntity<RestResponse<Object>> confirm(@RequestBody() @Validated ListIdDTO request) {
//        boolean isConfirmed = service.confirmedPurchaseOrders(request.getId(),request.getUserId());
//        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
//    }
    @PostMapping("/purchase-order/send")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.sendPurchaseOrders(request.getId(), request.getUserId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PutMapping("/purchase-order/update-ship-window")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody @Validated PurchaseOrdersShipWindowRequestDTO dto) {
        boolean result = service.updateShipWindow(dto);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
//    @PostMapping("/purchase-order/cancel")
//    public ResponseEntity<RestResponse<Object>> cancel(@RequestBody() @Validated ListIdDTO request) {
//        boolean isConfirmed = service.cancelPurchaseOrders(request.getId(),request.getUserId(),request.getReason());
//        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
//    }
    @PostMapping(value = "/purchase-order/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResources(@RequestParam("file") MultipartFile file,@RequestParam("userId") String userId) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToPO(file,userId);
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
    @PostMapping(value = "/purchase-order/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file, @RequestParam("id") Integer id) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailPO(file, id);
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

    @GetMapping(value = "/purchase-order/template")
    public HttpEntity<ByteArrayResource> downloadCustomFieldTemplate() throws IOException {
        try {
            log.info("START download template");
            Path source = Paths.get("");
            Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator +GlobalConstant.PATH_RESOURCE+NAME_TEMPLATE );
            File resource =resourcePath.toFile();
            byte[] excelContent = Files.readAllBytes(resource.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PurchaseOrder_Template.xlsx");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }

    @GetMapping(value = "/purchase-order/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable int id) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate = "PurchaseOrder.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath+"/"+nameTemplate);
            String fileName = file.getPath();
            service.export(fileName, id);
            byte[] excelContent = Files.readAllBytes(file.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameTemplate + "");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }


    @PutMapping("/purchase-order/update-ship-date")
    public ResponseEntity<RestResponse<Object>> updateShipDate(@RequestBody @Validated LogUpdateDateRequestDTO dto) {
        boolean isConfirmed = service.updateShipDate(dto.getId(), dto.getDate(), dto.getUserId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }
}
