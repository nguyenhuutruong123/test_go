package com.yes4all.web.rest.controller;

import com.yes4all.common.enums.Tab;
import com.yes4all.common.enums.TabAction;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.ShipmentLocalBroker;
import com.yes4all.domain.model.*;
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

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class ShipmentController {

    @Autowired
    private ShipmentService service;

    private final Logger log = LoggerFactory.getLogger(ShipmentController.class);
    @Autowired
    private UploadExcelService uploadExcelService;

    @PostMapping("/shipment/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody DetailObjectDTO request) {
         //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.INFORMATION);
        ShipmentMainDTO response = service.getShipmentDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/shipment/packing-list/listing")
    public ResponseEntity<RestResponse<Object>> findAllPackingList(@RequestBody DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.SUPPLIER_DOCS);
        Set<ShipmentsPackingListDTO> response = service.getAllPackingList(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/shipment/packing-list/detail")
    public ResponseEntity<RestResponse<Object>> findDetailPackingList(@RequestBody DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.SUPPLIER_DOCS);
        ShipmentsPackingListDTO response = service.getPackingListDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/shipment/get-list-filter")
    public ResponseEntity<RestResponse<Object>> getListFilter() {
        ListFilterShipmentDTO response = service.getListFilter();
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping(value = "/shipment/create")
    public ResponseEntity<RestResponse<Object>> createShipment(@RequestBody @Validated ShipmentDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            //service.checkPermissionUser(request.getCreatedBy(), TabAction.EDIT, Tab.INFORMATION);
            ShipmentDTO result = service.createShipment(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/shipment/update")
    public ResponseEntity<RestResponse<Object>> updateShipment(@RequestBody @Validated ShipmentDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            //service.checkPermissionUser(request.getCreatedBy(), TabAction.EDIT, Tab.INFORMATION);
            ShipmentDTO result = service.updateShipment(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/shipment/containers/detail")
    public ResponseEntity<RestResponse<Object>> findDetailContainer(@RequestBody DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.CONTAINER_DETAIL);
        ShipmentsContainersDTO response = service.getContainersDetail(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/shipment/containers/listing")
    public ResponseEntity<RestResponse<Object>> findALLContainer(@RequestBody DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.CONTAINER);
        Set<ShipmentsContainersMainDTO> response = service.getAllContainers(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/shipment/containers/update")
    public ResponseEntity<RestResponse<Object>> updateContainers(@RequestBody @Validated ShipmentsContainersDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.CONTAINER);
            ShipmentsContainersDTO result = service.updateContainers(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/shipment/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO) {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams = CommonDataUtil.searchFilter(listingDTO);
        Page<ShipmentMainDTO> response = service.listingWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/search-all-po")
    public ResponseEntity<RestResponse<Object>> findAllPO(@RequestBody(required = false) @Validated ListSearchALLPOShipmentDTO request) {
        List<IShipmentPurchaseOrdersDTO> response = service.searchPO(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping("/shipment/search-all-pi")
    public ResponseEntity<RestResponse<Object>> findAllPO(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        List<IShipmentProformaInvoiceDTO> response = service.searchPI(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping("/shipment/search-detail-po")
    public ResponseEntity<RestResponse<Object>> findAllDetailPO(@RequestBody(required = false) @Validated ListSearchPOShipmentDTO request) {
        ShipmentsPurchaseOrdersMainDTO response = service.searchPODetail(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping("/shipment/packing-list/create")
    public ResponseEntity<RestResponse<Object>> createPKL(@RequestBody(required = false) @Validated ListSearchPIShipmentDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.SUPPLIER_DOCS);
        ShipmentDTO response = service.createPKL(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/packing-list/confirm")
    public ResponseEntity<RestResponse<Object>> confirmPKL(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.CONFIRM, Tab.SUPPLIER_DOCS);
        boolean response = service.confirmPKL(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/packing-list/reject")
    public ResponseEntity<RestResponse<Object>> rejectPKL(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.REJECT, Tab.SUPPLIER_DOCS);
        boolean response = service.rejectPKL(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/packing-list/update")
    public ResponseEntity<RestResponse<Object>> updatePKL(@RequestBody(required = false) @Validated ShipmentsPackingListDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.SUPPLIER_DOCS);
        ShipmentsPackingListDTO response = service.updatePKL(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping(value = "/shipment/delete")
    public ResponseEntity<RestResponse<Object>> deleteShipment(@RequestBody ListIdDTO id) {
        //service.checkPermissionUser(id.getUserId(), TabAction.DELETE, Tab.INFORMATION);
        boolean result = service.deleteShipment(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping(value = "/shipment/detail-purchase-order/delete")
    public ResponseEntity<RestResponse<Object>> deleteDetailPurchaserOrder(@RequestBody ListIdDTO id) {
        //service.checkPermissionUser(id.getUserId(), TabAction.EDIT, Tab.INFORMATION);
        boolean result = service.deleteDetailPurchaseOrder(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping(value = "/shipment/packing-list/delete")
    public ResponseEntity<RestResponse<Object>> deletePackingList(@RequestBody ListIdDTO id) {
        //service.checkPermissionUser(id.getUserId(), TabAction.EDIT, Tab.SUPPLIER_DOCS);
        boolean result = service.deletePackingList(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping(value = "/shipment/packing-list/send")
    public ResponseEntity<RestResponse<Object>> sendPackingList(@RequestBody ActionSingleIdDTO id) {
        //service.checkPermissionUser(id.getUserId(), TabAction.EDIT, Tab.SUPPLIER_DOCS);
        boolean result = service.sendPackingList(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping(value = "/shipment/order-quantity")
    public ResponseEntity<RestResponse<Object>> getOrderQuantity(@RequestBody ActionSingleIdDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.ORDER_QUANTITY);
        Set<ShipmentsQuantityDTO> result = service.getOrderQuantity(request);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/shipment/packing-list/export")
    public HttpEntity<ByteArrayResource> downloadResultExport(@RequestBody() ActionSingleIdDTO request) throws IOException {
        try {
            log.info("START download Packing List template");
            String nameTemplate = "PackingList.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath + "/" + nameTemplate);
            String fileName = file.getPath();
            service.export(fileName, request);
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

    @PostMapping(value = "/shipment/packing-list/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file, @RequestParam("id") List<Integer> id) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailShipmentPackingList(file, id);
                response.setMessage(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setMessage(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            }
        }
        message = "Please upload an excel file!";
        response.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());
    }

    @PutMapping("/shipment/commercial-invoice/generate/{id}")
    public ResponseEntity<RestResponse<Object>> createCommercialInvoice(@PathVariable Integer id) {
        ShipmentsPackingListDTO response = service.generateCommercialInvoice(id);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
    @PostMapping("/shipment/commercial-invoice/delete")
    public ResponseEntity<RestResponse<Object>> createCommercialInvoice(@RequestBody ActionSingleIdDTO request) {
        boolean response = service.deleteCommercialInvoice(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping(value = "/shipment/commercial-invoice/export")
    public HttpEntity<ByteArrayResource> downloadResultExportCI(@RequestBody() ActionSingleIdDTO request) throws IOException {
        try {
            log.info("START download Commercial Invoice template");
            String nameTemplate = "CommercialInvoice.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath + "/" + nameTemplate);
            String fileName = file.getPath();
            service.exportCI(fileName, request);
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

    @PostMapping(value = "/shipment/commercial-invoice/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetailCI(@RequestParam("file") MultipartFile file, @RequestParam("id") List<Integer> id) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailCommercialInvoiceWH(file, id);
                response.setMessage(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setMessage(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            }
        }
        message = "Please upload an excel file!";
        response.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());
    }

    @PostMapping(value = "/shipment/packing-list/export-general")
    public HttpEntity<ByteArrayResource> downloadResultExportPKL(@RequestBody() ActionSingleIdDTO request) throws IOException {

        FileDTO fileDTO = service.exportPKL(request);
        if (CommonDataUtil.isNull(fileDTO.getContent())) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileDTO.getFileName() + "");
        return new HttpEntity<>(new ByteArrayResource(fileDTO.getContent()), header);
    }

    @PostMapping(value = "/shipment/packing-list/export-general-ci")
    public HttpEntity<ByteArrayResource> downloadResultExportBOL(@RequestBody() ActionSingleIdDTO request) throws IOException {
        FileDTO fileDTO = service.exportCI(request);
        if (CommonDataUtil.isNull(fileDTO.getContent())) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileDTO.getFileName() + "");
        return new HttpEntity<>(new ByteArrayResource(fileDTO.getContent()), header);
    }
    @PostMapping(value = "/shipment/export")
    public HttpEntity<ByteArrayResource> exportShipment(@RequestBody() ListIdDTO request) {
        String nameTemplate = "Shipment.zip";
        byte[] result = service.exportMultiPKLCI(request);

        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameTemplate + "");
        return new HttpEntity<>(new ByteArrayResource(result), header);
    }
    @PutMapping("/shipment/update-date")
    public ResponseEntity<RestResponse<Object>> updateDate(@RequestBody @Validated LogUpdateDateRequestDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.INFORMATION);
        boolean isConfirmed = service.updateDate(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping("/shipment/get-all")
    public ResponseEntity<RestResponse<Object>> getAllShipment(@RequestBody @Validated InputGetShipmentDTO request) {
        List<ShipmentToIMSDTO> response = service.getAllShipment(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/get-detail-container")
    public ResponseEntity<RestResponse<Object>> getDetailShipmentContainer(@RequestBody @Validated InputGetContainerShipmentDTO request) {
        List<ShipmentContainerDetailToIMSDTO> response = service.getDetailShipmentContainer(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
//    @PostMapping("/shipment/container/detail/update")
//    public ResponseEntity<RestResponse<Object>> updateDetailContainer(@RequestBody @Validated InputUpdateContainerDTO request) {
//        boolean result = service.updateDetailContainer(request);
//        if (!CommonDataUtil.isNotNull(result)) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
//    }

    @PostMapping("/shipment/local-broker/detail")
    public ResponseEntity<RestResponse<Object>> getLocalBrokerDetail(@RequestBody @Validated DetailObjectDTO request) {
        ShipmentLocalBrokerDTO result = service.getLocalBrokerDetail(request);
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/shipment/local-broker/save")
    public ResponseEntity<RestResponse<Object>> saveLocalBrokerDetail(@RequestBody @Validated ShipmentLocalBrokerDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.LOCAL_BROKER);
        ShipmentLocalBrokerDTO result = service.saveLocalBrokerDetail(request);
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
//    @PostMapping("/shipment/us-broker/save")
//    public ResponseEntity<RestResponse<Object>> saveUSBrokerDetail(@RequestBody @Validated ActionSingleIdDTO request) {
//        //service.checkPermissionUser(request.getUserId(), TabAction.EDIT, Tab.LOCAL_BROKER);
//        boolean result = service.saveUSBrokerDetail(request);
//        if (!CommonDataUtil.isNotNull(result)) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
//    }
    @PostMapping("/shipment/logistics-info/save")
    public ResponseEntity<RestResponse<Object>> saveLogisticsInfo(@RequestBody @Validated ShipmentLogisticsInfoDTO request) {
        //service.checkPermissionUser(request.getUpdatedBy()==null?request.getCreatedBy():request.getUpdatedBy(), TabAction.EDIT, Tab.LOGISTIC);
        ShipmentLogisticsInfoDTO result = service.saveLogisticsInfo(request);
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/shipment/logistics-info/detail")
    public ResponseEntity<RestResponse<Object>> getLogisticsInfoDetail(@RequestBody @Validated DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.LOGISTIC);
        ShipmentLogisticsInfoDTO result = service.getLogisticsInfoDetail(request);
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/shipment/us-broker/detail")
    public ResponseEntity<RestResponse<Object>> getUSBrokerDetail(@RequestBody @Validated DetailObjectDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.VIEW, Tab.US_BROKER);
        USBrokerDTO result = service.getUSBrokerDetail(request);
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/shipment/us-broker/confirm")
    public ResponseEntity<RestResponse<Object>> confirmUSBroker(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.CONFIRM, Tab.US_BROKER);
        boolean response = service.confirmUSBroker(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/us-broker/reject")
    public ResponseEntity<RestResponse<Object>> rejectUSBroker(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        //service.checkPermissionUser(request.getUserId(), TabAction.REJECT, Tab.US_BROKER);
        boolean response = service.rejectUSBroker(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/container/inbound")
    public ResponseEntity<RestResponse<Object>> inbound(@RequestBody(required = false) @Validated RequestInboundDTO request) {
        boolean response = service.actionInbound(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/shipment/send-request")
    public ResponseEntity<RestResponse<Object>> sendRequest(@RequestBody(required = false) @Validated ActionSingleIdDTO request) {
        boolean response = service.sendRequest(request);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }
}
