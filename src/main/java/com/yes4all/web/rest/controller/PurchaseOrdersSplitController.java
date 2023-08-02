package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.common.utils.UploadPurchaseOrderSplitStatus;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.model.*;
import com.yes4all.service.PurchaseOrdersSplitService;
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
import java.util.ArrayList;
import java.util.List;

import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;

@RestController
@RequestMapping("/api")
public class PurchaseOrdersSplitController {
    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersSplitController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private PurchaseOrdersSplitService service;
    @Value("${attribute.template.path}")
    private String templatePath;
    private static final String NAME_TEMPLATE = "/PurchaseOrderSplit_Template.xlsx";
    @PostMapping("/purchase-order-split")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody  BodyListingDTO request) {

        Page<PurchaseOrdersMainSplitDTO> response = service.getAll(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping(value = "/purchase-order-split/split")
    public ResponseEntity<RestResponse<Object>> splitPO(@RequestParam Integer id) {
        PurchaseOrdersSplit result = service.splitPurchaseOrder(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
    @PostMapping(value = "/purchase-order-split/create-po")
    public ResponseEntity<RestResponse<Object>> createPurchaseOrder(@RequestParam Integer id, @RequestParam(required = false) String userId ) {
        List<Integer> result = service.createPurchaseOrder(id,userId);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
    @PostMapping("/purchase-order-split/data")
    public ResponseEntity<RestResponse<Object>> findDataWithId(@RequestBody  BodyListingDTO request) {
        PurchaseOrderDataPageDTO response = service.getPurchaseOrdersSplitData(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping("/purchase-order-split/result")
    public ResponseEntity<RestResponse<Object>> findResultWithId(@RequestBody  BodyListingDTO request) {
        PurchaseOrderResultPageDTO response = service.getPurchaseOrdersSplitResult(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping("/purchase-order-split/result/details")
    public ResponseEntity<RestResponse<Object>> findResultDetailsWithId(@RequestBody  BodyListingDTO request)  {
        PurchaseOrderSplitResultDetailsDTO response = service.getPurchaseOrdersSplitResultDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping("/purchase-order-split/delete")
    public ResponseEntity<List<Integer>> removePurchaseOrders(@RequestBody() @Validated ListIdDTO request)  {
        boolean isRemoved = service.removePurchaseOrdersSplit(request.getId(), request.getUserId());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/purchase-order-split/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResources(@RequestParam("file") List<MultipartFile> file, @RequestParam("user") String user) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        List<ResultUploadDTO> resultUploadDTOs = new ArrayList<>();
        List<UploadPurchaseOrderSplitStatus> uploadPurchaseOrderSplitStatus = new ArrayList<>();
        for (MultipartFile element : file) {
            if (ExcelHelper.hasExcelFormat(element)) {
                try {
                    response = uploadExcelService.mappingToPOSplit(element);
                    long countRowErrors = response.getUploadPurchaseOrderSplitStatus().stream().filter(i -> i.getStatus().equals("errors")).count();
                    if (countRowErrors > 0) {
                        uploadPurchaseOrderSplitStatus.add(response.getUploadPurchaseOrderSplitStatus().get(0));
                    } else {
                        resultUploadDTOs.add(response);
                    }
                    response.setMessage(message);
                } catch (Exception e) {
                    response.setMessage(e.getMessage());
                    return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
                }
            }
        }
        if(!uploadPurchaseOrderSplitStatus.isEmpty()){
            response.setMessage(message);
            response.setUploadPurchaseOrderSplitStatus(uploadPurchaseOrderSplitStatus);
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        if (!resultUploadDTOs.isEmpty()) {
            List<PurchaseOrdersSplit> result = service.createPurchaseOrdersSplit(resultUploadDTOs, user);
            if (!result.isEmpty()) {
                response.setMessage(message);
                response.setUploadPurchaseOrdersSplit(result);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } else {
                response.setMessage(message);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
            }
        } else {
            response.setMessage(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());
        }
    }

    @GetMapping(value = "/purchase-order-split/template")
    public HttpEntity<ByteArrayResource> downloadCustomFieldTemplate() throws IOException {
        try {
            log.info("START download template");
            Path source = Paths.get("");
            Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator +GlobalConstant.PATH_RESOURCE+NAME_TEMPLATE );
            File resource =resourcePath.toFile();
            byte[] excelContent = Files.readAllBytes(resource.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PurchaseOrderSplit_Template.xlsx");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }

    @GetMapping(value = "/purchase-order-split/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable int id) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate=service.getNameFile(id)+".xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath+"/"+nameTemplate);
            String fileName = file.getPath();
            service.export(fileName,id);
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
}
