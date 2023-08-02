package com.yes4all.web.rest.controller;


import com.yes4all.common.constants.Constant;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.Booking;
import com.yes4all.domain.Resource;
import com.yes4all.domain.Shipment;
import com.yes4all.domain.model.ResultUploadDTO;
import com.yes4all.repository.BookingRepository;
import com.yes4all.repository.ResourceRepository;
import com.yes4all.repository.ShipmentRepository;
import com.yes4all.service.SendMailService;
import com.yes4all.service.impl.ResourceServiceImpl;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.web.util.HeaderUtil;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static com.yes4all.common.constants.Constant.*;
import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.constants.GlobalConstant.SEND_REQUEST_SHIPMENT;
import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final Logger log = LoggerFactory.getLogger(ResourceController.class);
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private ResourceRepository photoRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private ResourceServiceImpl resourceService;


    @GetMapping(value = "/{module}/{id}/{fileName}")
    public void getProformaInvoiceFile(HttpServletRequest request, HttpServletResponse response, @PathVariable String module, @PathVariable Integer id, @PathVariable String fileName) {
        log.debug("REST request to get Technical Files : {}", fileName);
        try {
            String filePath = GlobalConstant.FILE_UPLOAD_FOLDER_PATH + module + "/" + id + File.separator + fileName;
            File resource = new File(filePath);
            InputStream targetStream = new FileInputStream(resource);
            String contentType = request.getServletContext().getMimeType(resource.getAbsolutePath());
            if (CommonDataUtil.isEmpty(contentType)) {
                contentType = "application/octet-stream";
            }
            log.info("Content-type = {}", contentType);
            response.setContentType(contentType);
            StreamUtils.copy(targetStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            log.debug("Exception xx : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResources(@RequestParam("file") @Nullable List<MultipartFile> files, @RequestParam("id") Integer id, @RequestParam("module") String module) {
        log.debug("REST request to save Photo");
        ResultUploadDTO response = new ResultUploadDTO();
        List<Object> result = new ArrayList<>();
        try {
            String message = "";
            if (files != null && files.get(0).getSize() > 0) {
                for (MultipartFile file : files) {
                    if (module.equals(Constant.MODULE_BOOKING)) {
                        Optional<Booking> oBooking = bookingRepository.findById(id);
                        if (oBooking.isPresent()) {
                            Booking booking = oBooking.get();
                            if (!Objects.equals(booking.getStatus(), GlobalConstant.STATUS_BOOKING_LOADED) && !Objects.equals(booking.getStatus(), GlobalConstant.STATUS_BOOKING_ON_BOARDING)) {
                                message = "Upload failed: The status must be loaded.";
                                response.setMessage(message);
                                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
                            }
                            String filename = file.getOriginalFilename();
                            Map<String, String> info = CommonDataUtil.getInfoFile(filename);
                            filename = info.get(FILE_NAME);
                            if (!filename.startsWith("[FCR]")) {
                                message = "The file name must start with the [FCR] prefix.";
                                response.setMessage(message);
                                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());

                            }
                        }
                    } else if (module.equals(Constant.MODULE_SHIPMENT_US_BROKER_INVOICES) || module.equals(MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE) || module.equals(MODULE_SHIPMENT_US_BROKER_DUTY_FEE)) {
                        Optional<Shipment> oShipment = shipmentRepository.findById(id);
                        if (oShipment.isPresent()) {
                            Shipment shipment = oShipment.get();
                            if (!Objects.equals(shipment.getStatus(), GlobalConstant.STATUS_SHIPMENT_DOCKED_US) || !Objects.equals(shipment.getStatus(), GlobalConstant.STATUS_SHIPMENT_REVIEW_BROKER)) {
                                message = "Upload failed: The status must be Docked US.";
                                response.setMessage(message);
                                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
                            }
                        }
                    }
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded files success";
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(RestResponse.builder().body(result).build());
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }
    }

    @PostMapping(value = "/upload/bill-of-lading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesBOL(
        @RequestParam("file-bol") @Nullable List<MultipartFile> fileBOLs,
        @RequestParam("file-invoice") @Nullable List<MultipartFile> fileInvoices,
        @RequestParam("file-duty") @Nullable List<MultipartFile> fileDuTys,
        @RequestParam("file-other") @Nullable List<MultipartFile> fileOthers,
        @RequestParam("id") Integer id) {
        log.debug("REST request to save Photo");
        List<Object> result = new ArrayList<>();
        try {
            String message = "";
            String module = "";
            if (CommonDataUtil.isNotNull(fileBOLs) && fileBOLs.get(0).getSize() > 0) {
                for (MultipartFile file : fileBOLs) {
                    module = "bill-of-lading";
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded Images success";
            }
            if (CommonDataUtil.isNotNull(fileInvoices) && fileInvoices.get(0).getSize() > 0) {
                for (MultipartFile file : fileInvoices) {
                    module = "bill-of-lading-invoice";
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded Images success";
            }
            if (CommonDataUtil.isNotNull(fileDuTys) && fileDuTys.get(0).getSize() > 0) {
                for (MultipartFile file : fileDuTys) {
                    module = "bill-of-lading-duty-fee";
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded Images success";
            }
            if (CommonDataUtil.isNotNull(fileOthers) && fileOthers.get(0).getSize() > 0) {
                for (MultipartFile file : fileOthers) {
                    module = "bill-of-lading-other";
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded Images success";
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(RestResponse.builder().body(result).build());
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }
    }



    @GetMapping("/test-sendmail")
    public ResponseEntity<Map<Integer, Boolean>> testSendMail() {
        try {
            String subject = "test send mail";
            List<String> listEmailTo = new ArrayList<>();
            listEmailTo.add("truongnh@yes4all.com");
            String content = CommonDataUtil.contentMail("", "invoiceShipment"+"@"+ "poNumber", "", "Shipment", "sendRequest", SEND_REQUEST_SHIPMENT);
            sendMailService.sendMail(subject, content, listEmailTo, null, null, null);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, "message", "")).body(null);
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }

    }
//    @PostMapping(value = "/upload/shipment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<RestResponse<Object>> uploadResourcesPKLWH(
//        @RequestParam("file-telex-release") @Nullable List<MultipartFile> fileTelexRelease,
//        @RequestParam("file-tcsa-form") @Nullable List<MultipartFile> fileTCSA,
//        @RequestParam("file-lacey-act") @Nullable List<MultipartFile> fileLACEY,
//        @RequestParam("file-fumigation-certificate") @Nullable List<MultipartFile> fileCertificate,
//        @RequestParam("file-shipment-info") @Nullable List<MultipartFile> fileShipmentInfo,
//        @RequestParam("file-shipping-order") @Nullable List<MultipartFile> fileShippingOrder,
//        @RequestParam("file-others-local-broker") @Nullable List<MultipartFile> fileOthersLocal,
//        @RequestParam("file-invoices") @Nullable List<MultipartFile> fileInvoices,
//        @RequestParam("file-arrival-notice") @Nullable List<MultipartFile> fileArrivalNotice,
//        @RequestParam("file-duty-fee") @Nullable List<MultipartFile> fileDutyFee,
//        @RequestParam("file-others-us-broker") @Nullable List<MultipartFile> fileOthersUs,
//        @RequestParam("id") Integer id) {
//        log.debug("REST request to save Photo");
//        List<Object> result = new ArrayList<>();
//        try {
//            String message = "";
//            String module = "";
//            if (CommonDataUtil.isNotNull(fileTelexRelease) && fileTelexRelease.get(0).getSize() > 0) {
//                for (MultipartFile file : fileTelexRelease) {
//                    module = MODULE_PACKING_LIST_WH_TELEX_RELEASE;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileTCSA) && fileTCSA.get(0).getSize() > 0) {
//                for (MultipartFile file : fileTCSA) {
//                    module = MODULE_PACKING_LIST_WH_TCSA_FORM;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileLACEY) && fileLACEY.get(0).getSize() > 0) {
//                for (MultipartFile file : fileLACEY) {
//                    module = MODULE_PACKING_LIST_WH_LACEY_ACT;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileCertificate) && fileCertificate.get(0).getSize() > 0) {
//                for (MultipartFile file : fileCertificate) {
//                    module = MODULE_PACKING_LIST_WH_FUMIGATION_CERTIFICATE;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileShipmentInfo) && fileShipmentInfo.get(0).getSize() > 0) {
//                for (MultipartFile file : fileShipmentInfo) {
//                    module = MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_INFO;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileShippingOrder) && fileShippingOrder.get(0).getSize() > 0) {
//                for (MultipartFile file : fileShippingOrder) {
//                    module = MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_ORDER;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileOthersLocal) && fileOthersLocal.get(0).getSize() > 0) {
//                for (MultipartFile file : fileOthersLocal) {
//                    module = MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_OTHERS;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileInvoices) && fileInvoices.get(0).getSize() > 0) {
//                for (MultipartFile file : fileInvoices) {
//                    module = MODULE_SHIPMENT_US_BROKER_INVOICES;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileArrivalNotice) && fileArrivalNotice.get(0).getSize() > 0) {
//                for (MultipartFile file : fileArrivalNotice) {
//                    module = MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileDutyFee) && fileDutyFee.get(0).getSize() > 0) {
//                for (MultipartFile file : fileDutyFee) {
//                    module = MODULE_SHIPMENT_US_BROKER_DUTY_FEE;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            if (CommonDataUtil.isNotNull(fileOthersUs) && fileOthersUs.get(0).getSize() > 0) {
//                for (MultipartFile file : fileOthersUs) {
//                    module = MODULE_SHIPMENT_US_BROKER_OTHERS;
//                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
//                    result.add(resource);
//                }
//                message = "Uploaded Images success";
//            }
//            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(RestResponse.builder().body(result).build());
//        } catch (Exception ipe) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
//        }
//    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<Integer, Boolean>> removeResource(@PathVariable Integer id) {
        try {
            Map<Integer, Boolean> result = new HashMap<>();
            boolean isResult = resourceService.deleteFileUpload(id);
            result.put(id, isResult);
            String message = "files success";
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(result);
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }

    }

}
