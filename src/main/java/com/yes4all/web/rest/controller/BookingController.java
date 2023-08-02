package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.model.*;
import com.yes4all.service.BookingService;
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
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private BookingService service;


    @PostMapping(value = "/booking/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) {
        ResultUploadBookingDTO result = new ResultUploadBookingDTO();
        try {
            result = service.save(file, userId);
            return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(result).build());
        } catch (Exception e) {
            result.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(result).build());
        }


    }

    @PostMapping(value = "/booking")
    public ResponseEntity<RestResponse<Object>> submitBooking(@RequestBody @Validated BookingDTO request) {
        if (CommonDataUtil.isNotNull(request)) {
            BookingDTO result = service.createBooking(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/get-all")
    public ResponseEntity<RestResponse<Object>> getAllBooking(@RequestBody SearchBookingDTO request) {
        List<BookingMainDTO> result = service.getAllBookingListBookingNo(request);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping(value = "/booking/completed")
    public ResponseEntity<RestResponse<Object>> completedBooking(@RequestParam("id") Integer id) {

        BookingDTO result = service.completedBooking(id);
        if (result != null) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/submit-packing-list")
    public ResponseEntity<RestResponse<Object>> submitPackingList(@RequestBody BookingPackingListDTO request, @RequestParam("bookingId") Integer id) {
        BookingPackingListDTO result = service.submitPackingList(request, id);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/booking/{id}/send-packing-list")
    public ResponseEntity<RestResponse<Object>> sendPackingList(@PathVariable Integer id) {
        boolean result = service.sendPackingList(id);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/send")
    public ResponseEntity<RestResponse<Object>> sendBooking(@RequestBody SendBookingDTO request) {
        BookingDTO result = service.sendBooking(request);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(value = "/booking/{id}/delete")
    public ResponseEntity<RestResponse<Object>> deleteBooking(@PathVariable Integer id) {
        boolean result = service.deleteBooking(id);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

//    @PostMapping(value = "/booking/confirm-packing-list")
//    public ResponseEntity<RestResponse<Object>> confirmPackingList(@RequestBody BodyConfirmCIDTO request) {
//        BookingPackingListDTO result = service.confirmPackingList(request);
//        if (CommonDataUtil.isNotNull(result)) {
//            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
//        }
//        return ResponseEntity.notFound().build();
//    }

    @PatchMapping(value = "/booking/cancel/{id}")
    public ResponseEntity<RestResponse<Object>> cancelBooking(@PathVariable Integer id) {
        ResultDTO result = service.cancelBooking(id);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody BookingPageGetDetailDTO request) {
        BookingDetailsDTO response = service.getBookingDetailsDTO(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/update")
    public ResponseEntity<RestResponse<Object>> updateBooking(@RequestBody BookingDetailsDTO request) {
        Integer response = service.updateBooking(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/packing-list/detail")
    public ResponseEntity<RestResponse<Object>> findOnePackingList(@RequestBody @Validated BodyGetDetailDTO request) {
        BookingPackingListDTO response = service.getPackingListDetailsDTO(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/packing-list/create-get-detail")
    public ResponseEntity<RestResponse<Object>> getDetailCreatedPackingList(@RequestBody() ListIdDTO request) {
        BookingPackingListDTO response = service.getDetailsCreatedPackingList(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO listingDTO) {
        Integer page = listingDTO.getPage();
        Integer size = listingDTO.getSize();
        Map<String, String> filterParams;
        filterParams = CommonDataUtil.searchFilter(listingDTO);
        Page<BookingMainDTO> response = service.listingBookingWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping(value = "/booking/packing-list/export")
    public HttpEntity<ByteArrayResource> downloadResultExport(@RequestBody() ListIdDTO  request) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate = "PackingList.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath + "/" + nameTemplate);
            String fileName = file.getPath();
            service.export(fileName, request.getId());
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
    @PostMapping(value = "/booking/packing-list/export-bol")
    public HttpEntity<ByteArrayResource> downloadResultExportBOL(@RequestBody() ListIdDTO  request) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate = "PackingList.xlsx";
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            File file = new File(filePath + "/" + nameTemplate);
            String fileName = file.getPath();
            service.exportPKLFromBOL(fileName, request.getId().get(0));
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
    @PostMapping(value = "/booking/packing-list/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file, @RequestParam("id") List<Integer> id) {
        ResultUploadDTO response = new ResultUploadDTO();
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailPackingList(file, id);
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
}
