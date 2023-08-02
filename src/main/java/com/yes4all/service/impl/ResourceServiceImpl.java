package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.*;
import com.yes4all.repository.*;
import com.yes4all.service.ResourceService;
import com.yes4all.service.ShipmentService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static com.yes4all.common.constants.Constant.*;
import static com.yes4all.constants.GlobalConstant.*;

@Service
public class ResourceServiceImpl implements ResourceService {
    private final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Value("${attribute.host.url}")
    private String hostUrl;
    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;

    @Autowired
    private BookingServiceImpl bookingServiceImpl;
    @Autowired
    private ShipmentServiceImpl shipmentServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Resource handleUploadFile(MultipartFile multipartFile, Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        filePath += module + "/" + id;
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        if (!Files.exists(resourcePath)) {
            FileUtils.createParentDirectories(resourcePath.toFile());
            Files.createDirectory(resourcePath);
        }
        String dateUpload = "";
        String createBy = "";
        String filename = "";
        if (CommonDataUtil.isNotNull(multipartFile.getOriginalFilename())) {
            filename = multipartFile.getOriginalFilename();
        }
        if ((module.equals(MODULE_BOOKING) || module.equals(MODULE_BILL_OF_LADING)) && filename != null) {
            Map<String, String> info = CommonDataUtil.getInfoFile(filename);
            createBy = info.get(CREATED_BY_UPLOAD);
            dateUpload = info.get(DATE_UPLOAD);
            filename = info.get(FILE_NAME);
        }
        String originalFileName = Objects.requireNonNull(filename).replace(" ", "_");
        String uploadPath = resourcePath + File.separator + originalFileName;
        File uploadFile = doUploadFile(multipartFile, uploadPath);
        if (!uploadFile.exists()) {
            throw new IOException("Fail to upload file");
        }
        return setMetadata(uploadFile, id, module, fileType, multipartFile.getSize(), dateUpload, createBy);
    }

    @Override
    public Boolean deleteFileUploadTemp(Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        filePath += module + "/" + id + "_temp";
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        FileUtils.cleanDirectory(resourcePath.toFile());
        return Files.deleteIfExists(resourcePath);
    }


    private File doUploadFile(MultipartFile multipartFile, String filePath) {
        File uploadedFile = new File(filePath);
        try {
            multipartFile.transferTo(uploadedFile);
            logger.info("Moved file {} to path {}", uploadedFile.getName(), filePath);
        } catch (IOException e) {
            logger.error(String.format("Fail to upload file %s to inbound folder", uploadedFile.getName()));
        }
        return uploadedFile;
    }


    public Resource setMetadata(File uploadedFile, Integer id, String module, String fileType, long fileSize, String dateUpload, String createdBy) {
        Resource resource = new Resource();
        String fileName = uploadedFile.getName();
        resource.setName(fileName);
        String resourcePath = "";
        if (FILE_UPLOAD.equals(fileType)) {
            resourcePath = hostUrl + RESOURCE_FILE_UPLOAD_URL + module + "/";
        }
        resource.setPath(resourcePath + id + "/" + fileName);
        resource.setType(FilenameUtils.getExtension(fileName));
        resource.setModule(module);
        resource.setFileSize(fileSize);
        resource.setFileType(fileType);
        if (module.equals(MODULE_BOOKING)) {
            resource.setUploadDate(DateUtils.convertStringToInstant(dateUpload));
            resource.setCreatedBy(createdBy);
        } else {
            resource.setUploadDate(Instant.now());
        }
        if (module.equals(MODULE_PROFORMA_INVOICE)) {
            resource.setProformaInvoiceId(id);
        } else if (module.equals(MODULE_PROFORMA_INVOICE_WH)) {
            resource.setProformaInvoiceWHId(id);
        } else if (module.equals(MODULE_COMMERCIAL_INVOICE)) {
            resource.setCommercialInvoiceId(id);
        } else if (module.equals(MODULE_COMMERCIAL_INVOICE_WH)) {
            resource.setCommercialInvoiceWHId(id);
        } else if (module.equals(MODULE_PACKING_LIST_WH)) {
            resource.setPackingListWhId(id);
        } else if (module.equals(MODULE_BILL_OF_LADING)) {
            resource.setBillOfLadingId(id);
            resource.setUploadDate(DateUtils.convertStringToInstant(dateUpload));
            resource.setCreatedBy(createdBy);
        } else if (module.equals(MODULE_BILL_OF_LADING_INVOICE)
            || module.equals(MODULE_BILL_OF_LADING_DUTY_FEE)
            || module.equals(MODULE_BILL_OF_LADING_OTHER)) {
            resource.setBillOfLadingId(id);
        } else if (module.equals(MODULE_BOOKING)) {
            resource.setBookingId(id);
            Optional<Booking> oBooking = bookingRepository.findById(id);
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                bookingServiceImpl.updateStatusResource(booking);
            }
        } else if (module.equals(MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_INFO) ||
            module.equals(MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_OTHERS) ||
            module.equals(MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE) ||
            module.equals(MODULE_SHIPMENT_US_BROKER_DUTY_FEE) ||
            module.equals(MODULE_SHIPMENT_US_BROKER_OTHERS)) {
            resource.setShipmentId(id);
        } else if (module.equals(MODULE_PACKING_LIST_WH_TELEX_RELEASE) ||
            module.equals(MODULE_PACKING_LIST_WH_TCSA_FORM) ||
            module.equals(MODULE_PACKING_LIST_WH_LACEY_ACT) ||
            module.equals(MODULE_PACKING_LIST_WH_FUMIGATION_CERTIFICATE)||
            module.equals(MODULE_PACKING_LIST_WH_OTHERS)) {
            resource.setPackingListWhId(id);
        } else if (module.equals(MODULE_SHIPMENT_US_BROKER_INVOICES)) {
            resource.setShipmentId(id);
            Optional<Shipment> oShipment = shipmentRepository.findById(id);
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                shipmentServiceImpl.updateStatusResource(shipment, STATUS_SHIPMENT_REVIEW_BROKER);
            }
        }
//        if (resource.getModule().equals(MODULE_FILE_LOGO)) {
//            Optional<Resource> oResource = resourceRepository.findByFileTypeAndModule(resource.getFileType(), resource.getModule());
//            if (oResource.isPresent()) {
//                resource.setId(oResource.get().getId());
//            }
//        } else {
            Optional<Resource> oResource = resourceRepository.findByFileTypeAndNameAndBookingIdAndModule(resource.getFileType(), resource.getName(), id, resource.getModule());
            if (oResource.isPresent()) {
                resource.setId(oResource.get().getId());
            }
//        }
        resource = resourceRepository.saveAndFlush(resource);

        return resource;
    }
    public List<String> getListUserSC(User userVendor) {
        try {
            String[] listUserSC;
            List<String> result = new ArrayList<>();
            if (userVendor.getListUserSc() != null && userVendor.getListUserSc().length() > 0) {
                listUserSC = userVendor.getListUserSc().split(";");
                for (String item : listUserSC) {
                    Optional<User> oUser = userRepository.findOneByLogin(item);
                    if (oUser.isEmpty()) {
                        throw new BusinessException(String.format("User  { %s } Can not find in System", item));
                    } else {
                        User user = oUser.get();
                        result.add(user.getEmail());
                    }
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public List<String> getListUserPU(User userVendor) {
        try {
            String[] listUserPU;
            List<String> result = new ArrayList<>();
            if (userVendor.getListUserPu() != null && userVendor.getListUserPu().length() > 0) {
                listUserPU = userVendor.getListUserPu().split(";");
                for (String item : listUserPU) {
                    Optional<User> oUser = userRepository.findOneByLogin(item);
                    if (oUser.isEmpty()) {
                        throw new BusinessException(String.format("User  { %s } Can not find in System", item));
                    } else {
                        User user = oUser.get();
                        result.add(user.getEmail());
                    }
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    public static String getFileResourcePath(String fileType) throws IOException {
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + fileType);
        if (!Files.exists(resourcePath)) {
            FileUtils.createParentDirectories(resourcePath.toFile());
            Files.createDirectory(resourcePath);
        }
        if (FILE_UPLOAD.equals(fileType)) {
            return FILE_UPLOAD_FOLDER_PATH;
        }
        return "";
    }

    public Boolean deleteFileUpload(Integer id) throws IOException {
        try {
            Optional<Resource> oResource = resourceRepository.findById(id);
            Integer functionId = 0;
            String fileType = "";
            String fileName = "";
            String module = "";
            if (!oResource.isPresent()) {
                throw new BusinessException("Can not find file upload.");
            } else {
                Resource resource = oResource.get();
                fileType = resource.getFileType();
                fileName = resource.getName();
                module = resource.getModule();
                if (resource.getCommercialInvoiceId() != null) {
                    functionId = resource.getCommercialInvoiceId();
                } else if (resource.getProformaInvoiceId() != null) {
                    functionId = resource.getProformaInvoiceId();
                } else if (resource.getBookingId() != null) {
                    functionId = resource.getBookingId();
                }else if (resource.getShipmentId() != null) {
                    functionId = resource.getShipmentId();
                }else if (resource.getCommercialInvoiceWHId() != null) {
                    functionId = resource.getCommercialInvoiceWHId();
                }else if (resource.getPackingListWhId() != null) {
                    functionId = resource.getPackingListWhId();
                }
            }
            String filePath = getFileResourcePath(fileType);
            filePath += module + "/" + functionId + "/" + fileName;
            Path source = Paths.get("");
            Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
            resourceRepository.deleteById(id);
            Files.deleteIfExists(resourcePath);
            return true;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }
    }


    public Boolean deleteResource(Object object){
            if (object instanceof ShipmentsPackingList) {
                ShipmentsPackingList shipmentsPackingList = (ShipmentsPackingList) object;
                List<Resource> resources = resourceRepository.findByFileTypeAndPackingListWhId(FILE_UPLOAD, shipmentsPackingList.getId());
                if (!resources.isEmpty()) {
                    resources.stream().forEach(resource -> {
                        try {
                            deleteFileUpload(resource.getId());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } else if (object instanceof Shipment) {
                Shipment shipment = (Shipment) object;
                List<Resource> resources = resourceRepository.findByFileTypeAndShipmentId(FILE_UPLOAD, shipment.getId());
                if (!resources.isEmpty()) {
                    resources.stream().forEach(resource -> {
                        try {
                            deleteFileUpload(resource.getId());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }else if (object instanceof CommercialInvoiceWH) {
                CommercialInvoiceWH commercialInvoiceWH = (CommercialInvoiceWH) object;
                List<Resource> resources = resourceRepository.findByFileTypeAndCommercialInvoiceWHId(FILE_UPLOAD, commercialInvoiceWH.getId());
                if (!resources.isEmpty()) {
                    resources.stream().forEach(resource -> {
                        try {
                            deleteFileUpload(resource.getId());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }else if (object instanceof CommercialInvoice) {
                CommercialInvoice commercialInvoice = (CommercialInvoice) object;
                List<Resource> resources = resourceRepository.findByFileTypeAndCommercialInvoiceId(FILE_UPLOAD, commercialInvoice.getId());
                if (!resources.isEmpty()) {
                    resources.stream().forEach(resource -> {
                        try {
                            deleteFileUpload(resource.getId());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        return true;
    }

}
