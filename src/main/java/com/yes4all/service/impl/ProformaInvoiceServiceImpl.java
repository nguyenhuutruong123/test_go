package com.yes4all.service.impl;

import com.yes4all.common.enums.EnumColumn;
import com.yes4all.common.enums.EnumNoteExcel;
import com.yes4all.common.enums.EnumUserType;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.ProformaInvoiceService;
import com.yes4all.service.SendMailService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yes4all.common.constants.Constant.MODULE_PROFORMA_INVOICE;
import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.common.utils.ExcelHelper.createCell;
import static com.yes4all.common.utils.ExcelHelper.createCellNOTE;
import static com.yes4all.constants.GlobalConstant.*;


/**
 * Service Implementation for managing {@link ProformaInvoice}.
 */
@Service
@Transactional
public class ProformaInvoiceServiceImpl implements ProformaInvoiceService {

    private final Logger log = LoggerFactory.getLogger(ProformaInvoiceServiceImpl.class);
    private static final String TITLE_HASHMAP = "detail_";

    @Autowired
    private BookingProformaInvoiceRepository bookingProformaInvoiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SendMailService sendMailService;
    @Value("${attribute.link.url}")
    private String linkPOMS;

    @Value("${spring.datasource.type}")
    private String type;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.username}")
    private String username;

    private static final String LINK_DETAIL_PI = "/proforma-invoices/detail/";
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserService userService;

    @Override
    public Page<ProformaInvoiceMainDTO> listingProformaInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");

        Page<ProformaInvoice> data = proformaInvoiceRepository.findByCondition(
            filterParams.get("invoiceNoPI"), filterParams.get("supplierSearch")
            , filterParams.get("bookingNumber"), filterParams.get("shipDateFrom")
            , filterParams.get("shipDateTo"), filterParams.get("amountFrom")
            , filterParams.get("amountTo"), filterParams.get("createdDateFrom")
            , filterParams.get("createdDateTo"), filterParams.get("status")
            , filterParams.get("updatedBy")
            , filterParams.get("supplier")
            , pageable);


        return data.map(item -> mappingEntityToDto(item, ProformaInvoiceMainDTO.class, null));
    }

    @Override
    public boolean removeProformaInvoice(List<Integer> listId, String userName) {
        try {
            listId.forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (!Objects.equals(proformaInvoice.getStatus(), STATUS_PI_NEW)) {
                        throw new BusinessException("This status can not delete Proforma Invoice");
                    }
                    PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_SENT);
                    purchaseOrders.setProformaInvoice(null);
                    purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                    proformaInvoice.setPurchaseOrders(null);
                    proformaInvoiceRepository.deleteById(proformaInvoice.getId());
                }
            });
            return true;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ProformaInvoiceDTO getProformaInvoiceDetail(BodyGetDetailDTO request) {
        try {
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(request.getId());
            if (oProformaInvoice.isPresent()) {
                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                ProformaInvoiceDTO result = mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class, request.getIsSupplier());
                if (result == null) {
                    return null;
                }
                if (request.getVendor().length() > 0 && (!request.getVendor().equals(proformaInvoice.getSupplier()))) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                if (request.getVendor().length() == 0 && proformaInvoice.getStatus().equals(STATUS_PI_NEW)) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                result.setPurchaserOrderId(proformaInvoice.getPurchaseOrders().getId());
                result.setPurchaserOrderNo(proformaInvoice.getPurchaseOrders().getPoNumber());
                List<Resource> resourcesListing = resourceRepository.findByFileTypeAndProformaInvoiceId(GlobalConstant.FILE_UPLOAD, proformaInvoice.getId());
                if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                    List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                        ResourceDTO data = new ResourceDTO();
                        data.setPath(item.getPath());
                        data.setId(item.getId());
                        data.setModule(item.getModule());
                        data.setName(item.getName());
                        data.setType(item.getType());
                        data.setSize(item.getFileSize());
                        return data;
                    }).collect(Collectors.toList());
                    result.setFileUploads(resources);
                }
                return result;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public void export(String filename, Integer id, String userId) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Optional<User> oUserAction = userRepository.findOneByLogin(userId);
        int userType = 0;
        if (oUserAction.isPresent()) {
            User user = oUserAction.get();
            if (user.getSupplier()) {
                userType = USER_SUPPLIER;
            } else if (user.getSourcing()) {
                userType = USER_SOURCING;
            } else {
                userType = USER_PU;
            }
        } else {
            throw new BusinessException("Can not find user login.");
        }
        generateExcelFile(id, workbook, userType);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }
    @Override
    public FileDTO exportExcelByIdPI(Integer id, String userId) {
        try {
            FileDTO fileDTO = new FileDTO();
            byte[] content = null;
            Optional<User> oUser = userRepository.findOneByLogin(userId);
            User user;
            if (oUser.isPresent()) {
                user = oUser.get();
            } else {
                throw new BusinessException("Can not find user");
            }
            Integer userType;
            if (user.getSupplier()) {
                userType = USER_SUPPLIER;
            } else if (user.getSourcing()) {
                userType = USER_SOURCING;
            } else {
                userType = USER_PU;
            }
            // get data PI
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(id);
            if (oProformaInvoice.isPresent()) {
                // file name
                fileDTO.setFileName(oProformaInvoice.get().getOrderNo() + ".xlsx");

                // header excel
                List<String> listHeader = getHeaderExcel();
                ProformaInvoice proformaInvoice = oProformaInvoice.get();

                Map<Long, List<Object>> mapData = getDetailExcel(proformaInvoice.getProformaInvoiceDetail(), userType);
                Optional<Vendor> oVendor = vendorRepository.findByVendorCode(proformaInvoice.getSupplier());
                Vendor vendor;
                if (oVendor.isPresent()) {
                    vendor = oVendor.get();
                } else {
                    throw new BusinessException("Can not find vendor");
                }
                content = ExcelUtil.generateExcelFilePI(null, 0, listHeader, mapData, proformaInvoice,vendor);
            } else {
                throw new BusinessException(String.format("id ProformaInvoice %s not exists", id));
            }

            if (CommonDataUtil.isNull(content)) {
                throw new BusinessException("Could not created excel file ProformaInvoice.");
            }
            // file data
            fileDTO.setContent(content);
            return fileDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    private Map<Long, List<Object>> getDetailExcel(Set<ProformaInvoiceDetail> listProformaInvoiceDetail, Integer userType) {
        // Detail excel
        Long row = 10L;
        Map<Long, List<Object>> mapData = new HashMap<>();
        List<Long> maxVersions = listProformaInvoiceDetail.stream().map(m -> m.getCdcVersion() == null ? 0 : m.getCdcVersion()).collect(Collectors.toList());
        Long maxVersion = Collections.max(maxVersions, null);
        listProformaInvoiceDetail=listProformaInvoiceDetail.stream().filter(m->m.getCdcVersion().equals(maxVersion)).collect(Collectors.toSet());

        for (ProformaInvoiceDetail proformaInvoiceDetail : listProformaInvoiceDetail) {
            List<Object> listData = new ArrayList<>();
            listData.add(CommonDataUtil.toEmpty(proformaInvoiceDetail.getFromSo()));
            listData.add(CommonDataUtil.toEmpty(proformaInvoiceDetail.getSku()));
            listData.add(CommonDataUtil.toEmpty(proformaInvoiceDetail.getAsin()));
            listData.add(CommonDataUtil.toEmpty(proformaInvoiceDetail.getProductName()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getQty()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getUnitPrice()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getAmount()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getTotalBox()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getPcs()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getTotalVolume()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getNetWeight()));
            listData.add(CommonDataUtil.toZero(proformaInvoiceDetail.getGrossWeight()));
            if (userType.equals(USER_PU)) {
                listData.add(proformaInvoiceDetail.getNoteAdjust()==null?"":proformaInvoiceDetail.getNoteAdjust());
            } else if (userType.equals(USER_SOURCING)) {
                listData.add(proformaInvoiceDetail.getNoteAdjustSourcing()==null?"":proformaInvoiceDetail.getNoteAdjustSourcing());
            }else{
                listData.add(proformaInvoiceDetail.getNoteAdjustSupplier()==null?"":proformaInvoiceDetail.getNoteAdjustSupplier());
            }
            mapData.put(row++, listData);
        }

        return mapData;
    }

    private List<String> getHeaderExcel() {
        List<String> listHeader = new ArrayList<>();
        listHeader.add("PO");
        listHeader.add("SKU");
        listHeader.add("Barcode");
        listHeader.add("Commodities and Specifications");
        listHeader.add("QTY");
        listHeader.add("Unit Price");
        listHeader.add("Amount");
        listHeader.add("CTN/QTY");
        listHeader.add("PCS/CTN");
        listHeader.add("CBM (m³)");
        listHeader.add("NW (kg)");
        listHeader.add("GW (kg)");
        listHeader.add("Note");
        return listHeader;
    }

    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook, int userType) {
        try {
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(id);
            ProformaInvoice proformaInvoice;
            if (oProformaInvoice.isEmpty()) {
                throw new BusinessException("Proforma Invoice not found.");
            }
            proformaInvoice = oProformaInvoice.get();
            XSSFSheet sheet = workbook.createSheet(proformaInvoice.getOrderNo());
            int rowCount = 1;

            writeHeaderLine(workbook, sheet, userType);
            CellStyle style = workbook.createCellStyle();
            XSSFCellStyle styleDate = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
            Optional<ProformaInvoiceDetail> oProformaInvoiceDetail = proformaInvoiceDetailRepository.findTop1CdcVersionByProformaInvoiceOrderByCdcVersionDesc(proformaInvoice);
            if (oProformaInvoiceDetail.isEmpty()) {
                throw new BusinessException("Can not find new version detail proforma invoice");
            }
            //get new cdcVersion detail
            Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();
            Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoice.getProformaInvoiceDetail().stream().filter(k -> k.getCdcVersion().equals(cdcVersionMax) && !k.isDeleted()).collect(Collectors.toSet());
            XSSFCellStyle styleFieldNumber2= workbook.createCellStyle();
            styleFieldNumber2.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
            XSSFCellStyle styleFieldNumber3= workbook.createCellStyle();
            styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
            for (ProformaInvoiceDetail detail : proformaInvoiceDetailSet) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(sheet, row, columnCount++, detail.getFromSo(), style);
                createCell(sheet, row, columnCount++, detail.getSku(), style);
                createCell(sheet, row, columnCount++, detail.getAsin(), style);
                createCell(sheet, row, columnCount++, detail.getProductName(), style);
                createCell(sheet, row, columnCount++, detail.getQty(), style);
                createCell(sheet, row, columnCount++, detail.getUnitPrice(), styleFieldNumber2);
                createCell(sheet, row, columnCount++, detail.getAmount(), styleFieldNumber2);
                createCell(sheet, row, columnCount++, detail.getTotalBox(), style);
                createCell(sheet, row, columnCount++, detail.getPcs(), style);
                createCell(sheet, row, columnCount++, detail.getTotalVolume(), styleFieldNumber3);
                createCell(sheet, row, columnCount++, detail.getNetWeight(), styleFieldNumber3);
                createCell(sheet, row, columnCount++, detail.getGrossWeight(), styleFieldNumber3);
                createCell(sheet, row, columnCount++, detail.getMakeToStock(), style);
                if (userType == USER_PU) {
                    String noteAdjust = detail.getNoteAdjust();
                    noteAdjust = EnumNoteExcel.getKeyByValue(noteAdjust);
                    createCell(sheet, row, columnCount, noteAdjust, style);
                } else if (userType == USER_SOURCING) {
                    String noteAdjust = detail.getNoteAdjustSourcing();
                    createCell(sheet, row, columnCount, noteAdjust, style);
                } else {
                    String noteAdjust = detail.getNoteAdjustSupplier();
                    createCell(sheet, row, columnCount, noteAdjust, style);
                }

            }
            return workbook;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private void writeHeaderLine(XSSFWorkbook workbook, XSSFSheet sheet, int userType) {

        Row row = sheet.createRow(0);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        XSSFColor myColor = new XSSFColor(rgb);
        style.setFillForegroundColor(myColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        int i = 0;
        createCell(sheet, row, i++, "AMAZON PO", style);
        createCell(sheet, row, i++, "SKU", style);
        createCell(sheet, row, i++, "BARCODE", style);
        createCell(sheet, row, i++, "COMMODITIES AND SPECIFICATIONS", style);
        createCell(sheet, row, i++, "QTY", style);
        createCell(sheet, row, i++, "UNIT PRICE", style);
        createCell(sheet, row, i++, "AMOUNT", style);
        createCell(sheet, row, i++, "CTN/QTY", style);
        createCell(sheet, row, i++, "PCS/CTN", style);
        createCell(sheet, row, i++, "CBM (m3)", style);
        createCell(sheet, row, i++, "NW (kg)", style);
        createCell(sheet, row, i++, "GW (kg)", style);
        createCell(sheet, row, i++, "MAKE-TO-STOCK", style);
        if (userType == USER_PU) {
            createCell(sheet, row, i, "NOTE", style);
        } else {
            createCellNOTE(sheet, row, i, "NOTE", style, workbook);
        }
    }

//    @Override
//    public ProformaInvoiceDTO createProformaInvoice(ProformaInvoiceDTO request, Boolean isSupplier) {
//        try {
//            ProformaInvoice proformaInvoice = new ProformaInvoice();
//            BeanUtils.copyProperties(request, proformaInvoice);
//            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(request.getOrderNo());
//            if (oProformaInvoice.isPresent()) {
//                throw new BusinessException(String.format("Proforma Invoice No %s already exists.", request.getOrderNo()));
//            }
//            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_NEW);
//            //get list PO
//            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(request.getPurchaserOrderId());
//            if (oPurchaseOrders.isPresent()) {
//                if (Boolean.TRUE.equals(oPurchaseOrders.get().getIsDeleted())) {
//                    throw new BusinessException(String.format("id PO %s deleted ", request.getPurchaserOrderId()));
//                }
//            } else {
//                throw new BusinessException(String.format("id PO %s not exists ", request.getPurchaserOrderId()));
//            }
//
//            PurchaseOrders purchaseOrders = oPurchaseOrders.get();
//            //get details PI from DTO
//            Set<ProformaInvoiceDetail> detailSet = new HashSet<>();
//            request.getProformaInvoiceDetail().forEach(
//                (key, value)
//                    -> {
//                    Set<ProformaInvoiceDetail> detailSetDetail = value.parallelStream().map(item -> {
//                        ProformaInvoiceDetail proformaInvoiceDetail;
//                        proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
//                        proformaInvoiceDetail.setCdcVersion(0L);
//                        proformaInvoiceDetail.setQtyPrevious(proformaInvoiceDetail.getQty());
//                        proformaInvoiceDetail.setAmountPrevious(proformaInvoiceDetail.getAmount());
//                        proformaInvoiceDetail.setUnitPricePrevious(proformaInvoiceDetail.getUnitPricePrevious());
//                        proformaInvoiceDetail.setPcsPrevious(proformaInvoiceDetail.getPcs());
//                        proformaInvoiceDetail.setTotalBoxPrevious(proformaInvoiceDetail.getTotalBox());
//                        proformaInvoiceDetail.setTotalVolumePrevious(proformaInvoiceDetail.getTotalVolume());
//                        proformaInvoiceDetail.setGrossWeightPrevious(proformaInvoiceDetail.getGrossWeight());
//                        proformaInvoiceDetail.setNetWeightPrevious(proformaInvoiceDetail.getNetWeight());
//                        proformaInvoiceDetail.setUpdatedBy(request.getUpdatedBy());
//                        proformaInvoiceDetail.setUpdatedDate(new Date().toInstant());
//
//                        return proformaInvoiceDetail;
//                    }).collect(Collectors.toSet());
//                    detailSet.addAll(detailSetDetail);
//                });
//            //Remove new row and deleted
//            if (detailSet.stream().filter(i -> !i.isDeleted()).collect(Collectors.toSet()).isEmpty()) {
//                throw new BusinessException("List sku must not empty");
//            }
//
//            proformaInvoice.setProformaInvoiceDetail(detailSet);
//            proformaInvoice.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_SUPPLIER);
//            proformaInvoice.setAmount(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
//            proformaInvoice.setCtn(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalBox()) ? 0 : x.getTotalBox()).reduce(0.0, Double::sum));
//            proformaInvoice.setGrossWeight(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
//            proformaInvoice.setCbmTotal(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
//            proformaInvoice.setPurchaseOrders(purchaseOrders);
//            proformaInvoice.setSupplier(purchaseOrders.getVendorId());
//            if (purchaseOrders.getStatus().equals(GlobalConstant.STATUS_PO_NEW)) {
//                throw new BusinessException("The status a purchase order not valid.");
//            }
//            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PROCESSING);
//            proformaInvoice.setIsSupplier(isSupplier);
//            //default value new version is false
//            proformaInvoice.setNewVersionDetail(false);
//            proformaInvoiceRepository.saveAndFlush(proformaInvoice);
//            return mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class, null);
//        } catch (Exception ex) {
//            log.error(ex.getMessage());
//            throw new BusinessException(ex.getMessage());
//        }
//
//    }

    @Override
    public ProformaInvoiceDTO updateProformaInvoice(Integer id, ProformaInvoiceDTO request) {
        try {
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(id);
            ProformaInvoice proformaInvoice;
            if (oProformaInvoice.isEmpty()) {
                throw new BusinessException("Proforma Invoice not found.");
            }
            proformaInvoice = oProformaInvoice.get();
            //get supplier
            String supplier = proformaInvoice.getSupplier();
            //get info user
            Optional<User> oUserAction = userRepository.findOneByLogin(request.getUpdatedBy());
            boolean isSupplier = false;
            boolean isSourcing = false;
            boolean isPU = false;
            if (oUserAction.isPresent()) {
                User user = oUserAction.get();
                if (user.getSupplier()) {
                    isSupplier = true;
                } else if (user.getSourcing()) {
                    isSourcing = true;
                } else {
                    isPU = true;
                }
            } else {
                throw new BusinessException("Can not find user login.");
            }
            //set value user sourcing primary
            if (isSourcing && proformaInvoice.getUserSourcingPrimary() == null) {
                proformaInvoice.setUserSourcingPrimary(request.getUpdatedBy());
            }
            //after create booking can not update proforma invoice
            if (proformaInvoice.getStatus() > GlobalConstant.STATUS_PI_CONFIRMED) {
                throw new BusinessException("The status not valid.");
            }
//            if ((request.getStatus() == GlobalConstant.STATUS_PI_SUPPLIER_REVIEW ||
//                request.getStatus() == GlobalConstant.STATUS_PI_SUPPLIER_APPROVED) && !isSupplier) {
//                throw new BusinessException("This is the status role Y4A can not adjust PI.");
//            }
            if (Objects.equals(request.getStatus(), STATUS_PI_Y4A_REVIEW) && isSupplier) {
                throw new BusinessException("This is the status role Supplier can not adjust PI.");
            }
            final boolean[] isSupplierFinal = {isSupplier};
            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(request.getPurchaserOrderId());
            if (oPurchaseOrders.isPresent()) {
                if (Boolean.TRUE.equals(oPurchaseOrders.get().getIsDeleted())) {
                    throw new BusinessException(String.format("id PO %s deleted ", request.getPurchaserOrderId()));
                }
            } else {
                throw new BusinessException(String.format("id PO %s not exists ", request.getPurchaserOrderId()));
            }

            //get po
            PurchaseOrders purchaseOrders = oPurchaseOrders.get();
            //get details previous update
            BeanUtils.copyProperties(request, proformaInvoice);
            proformaInvoice.setId(id);
            Long cdcVersionMax = proformaInvoice.getProformaInvoiceDetail().stream().mapToLong(ProformaInvoiceDetail::getCdcVersion).max().orElseThrow(
                () -> {
                    throw new BusinessException("Can not find new version detail proforma invoice");
                }
            );
            //version new
            final Long[] cdcVersionNew = {cdcVersionMax};

            //Get all qty old of sku for revert qty used in Purchase Order
            Set<ProformaInvoiceDetail> detailSet = new HashSet<>();
            boolean finalIsSourcing = isSourcing;
            boolean finalIsPU = isPU;
            boolean finalIsSupplier = isSupplier;
            request.getProformaInvoiceDetail().forEach((key, value) -> {
                Set<ProformaInvoiceDetail> detailSetDetail = value.parallelStream().filter(i -> i.getId() != null && i.getId() > 0).map(item -> {
                    ProformaInvoiceDetail proformaInvoiceDetail;
                    proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                    if (item.getUpdatedBy() == null) {
                        proformaInvoiceDetail.setUpdatedBy(request.getUpdatedBy());
                        proformaInvoiceDetail.setUpdatedDate(new Date().toInstant());
                    }
                    if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_Y4A_ADJUST) && Objects.equals(item.getCdcVersion(), cdcVersionMax)) {
                        if (request.getListRejectDetail() != null && !request.getListRejectDetail().isEmpty()
                            && request.getListRejectDetail().stream().anyMatch(element -> element.equals(item.getId()))) {
                            proformaInvoiceDetail.setIsConfirmed(false);
                        } else {
                            proformaInvoiceDetail.setIsConfirmed(true);
                        }
                    }
                    if (Objects.equals(item.getCdcVersion(), cdcVersionMax) && item.getUpdatedBy() != null) {
                        if ((!proformaInvoice.getUserUpdatedLatest().equals(USER_UPDATED_PU) || !finalIsSourcing) &&
                            (!proformaInvoice.getUserUpdatedLatest().equals(USER_UPDATED_SOURCING) || !finalIsPU)) {
                            return proformaInvoiceDetail;
                        }
                        proformaInvoiceDetail.setUpdatedBy(request.getUpdatedBy());
                        proformaInvoiceDetail.setUpdatedDate(new Date().toInstant());
                    }
                    return proformaInvoiceDetail;
                }).collect(Collectors.toSet());
                detailSet.addAll(detailSetDetail);

                //define new detail of proforma invoice if id null or status pi=STATUS_PI_Y4A_ADJUST
                Set<ProformaInvoiceDetail> detailSetDetailNew = value.stream().filter(i ->
                        //if id is null or id < 0 then new version for detail
                        i.getId() == null || i.getId() < 0 || (finalIsSupplier && proformaInvoice.getStatus().equals(STATUS_PI_Y4A_ADJUST) && Objects.equals(i.getCdcVersion(), cdcVersionMax))
                    ).map(item -> {
                        ProformaInvoiceDetail proformaInvoiceDetail;
                        proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                        // create new detail if some of all field had change
                        // if status Y4A ADJUST then new detail with isConfirm = true
                        Optional<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOPrevious = Optional.empty();
                        if (request.getProformaInvoiceDetail().get(TITLE_HASHMAP + (cdcVersionMax - 1)) != null) {
                            proformaInvoiceDetailDTOPrevious = request.getProformaInvoiceDetail().get(TITLE_HASHMAP + (cdcVersionMax - 1)).stream().filter(i ->
                                i.getAsin().equals(item.getAsin()) && i.getFromSo().equals(item.getFromSo())
                            ).findFirst();
                        }
                        // if the line has been rejected then revert data version previous
                        if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_Y4A_ADJUST) && request.getListRejectDetail() != null
                            && !request.getListRejectDetail().isEmpty()
                            && request.getListRejectDetail().stream().anyMatch(element -> element.equals(proformaInvoiceDetail.getId()))) {
                            if (proformaInvoiceDetailDTOPrevious.isEmpty()) {
                                throw new BusinessException("Wrong payload");
                            }
                            BeanUtils.copyProperties(proformaInvoiceDetailDTOPrevious.get(), proformaInvoiceDetail);
                            proformaInvoiceDetail.setId(null);
                            proformaInvoiceDetail.setIsConfirmed(null);
                            proformaInvoiceDetail.setCdcVersion(cdcVersionMax + 1);
                            proformaInvoiceDetail.setQtyPrevious(proformaInvoiceDetail.getQty());
                            proformaInvoiceDetail.setUnitPricePrevious(proformaInvoiceDetail.getUnitPrice());
                            proformaInvoiceDetail.setAmountPrevious(proformaInvoiceDetail.getAmount());
                            proformaInvoiceDetail.setPcsPrevious(proformaInvoiceDetail.getPcs());
                            proformaInvoiceDetail.setTotalBoxPrevious(proformaInvoiceDetail.getTotalBox());
                            proformaInvoiceDetail.setTotalVolumePrevious(proformaInvoiceDetail.getTotalVolume());
                            proformaInvoiceDetail.setNetWeightPrevious(proformaInvoiceDetail.getNetWeight());
                            proformaInvoiceDetail.setGrossWeightPrevious(proformaInvoiceDetail.getGrossWeight());
                            proformaInvoiceDetail.setProformaInvoice(null);
                            proformaInvoiceDetail.setProformaInvoiceDetailLog(null);
                            return proformaInvoiceDetail;
                        }
                        proformaInvoiceDetail.setId(null);
                        proformaInvoiceDetail.setUpdatedBy(request.getUpdatedBy());
                        proformaInvoiceDetail.setUpdatedDate(new Date().toInstant());
                        proformaInvoiceDetail.setCdcVersion(cdcVersionMax + 1);
                        proformaInvoiceDetail.setProformaInvoice(null);
                        proformaInvoiceDetail.setProformaInvoiceDetailLog(null);
                        if (finalIsSourcing && proformaInvoice.getStatus().equals(STATUS_PI_Y4A_ADJUST) && item.getCdcVersion() == cdcVersionMax) {
                            proformaInvoiceDetail.setNoteAdjust(null);
                            proformaInvoiceDetail.setNoteAdjustSourcing(null);
                            proformaInvoiceDetail.setNoteAdjustSupplier(null);
                        }
                        return proformaInvoiceDetail;
                    })
                    .filter(obj -> true)
                    .collect(Collectors.toSet());
                if (!detailSetDetailNew.isEmpty()) {
                    detailSet.addAll(detailSetDetailNew);
                    //if create new version set value true
                    proformaInvoice.setNewVersionDetail(true);
                    cdcVersionNew[0] = cdcVersionNew[0] + 1;
                }
            });
            // add row new detail into detail previous
            proformaInvoice.setProformaInvoiceDetail(detailSet);
            if (isSupplier) {
                proformaInvoice.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_SUPPLIER);
            } else if (isSourcing) {
                proformaInvoice.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_SOURCING);
                // proformaInvoice.setStatusSourcing(GlobalConstant.STATUS_SOURCING_ADJUST);
            } else {
                proformaInvoice.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_PU);
                // proformaInvoice.setStatusPU(STATUS_PU_ADJUST);
            }
            proformaInvoice.setAmount(detailSet.stream().filter(i -> Objects.equals(i.getCdcVersion(), cdcVersionNew[0]) && !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            proformaInvoice.setCtn(detailSet.stream().filter(i -> Objects.equals(i.getCdcVersion(), cdcVersionNew[0]) && !i.isDeleted()).map(x -> Objects.isNull(x.getTotalBox()) ? 0 : x.getTotalBox()).reduce(0.0, Double::sum));
            proformaInvoice.setGrossWeight(detailSet.stream().filter(i -> Objects.equals(i.getCdcVersion(), cdcVersionNew[0]) && !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
            proformaInvoice.setCbmTotal(detailSet.stream().filter(i -> Objects.equals(i.getCdcVersion(), cdcVersionNew[0]) && !i.isDeleted()).map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
            proformaInvoice.setPurchaseOrders(purchaseOrders);
            List<Long> maxVersions = detailSet.stream().map(m -> m.getCdcVersion() == null ? 0 : m.getCdcVersion()).collect(Collectors.toList());
            Long maxVersion = Collections.max(maxVersions, null);
            if (isSupplier) {
                proformaInvoice.setVersionLatestSupplier(Math.toIntExact(maxVersion));
            } else {
                proformaInvoice.setVersionLatestY4a(Math.toIntExact(maxVersion));
            }
            //Y4a adjust PI
            if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_Y4A_REVIEW)) {
                purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_REVIEWING);
            } else if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_Y4A_ADJUST)) {
                // APPROVED PI AFTER SUPPLIER REVIEWING
                purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_SUPPLIER_APPROVED);
                proformaInvoice.setStatus(GlobalConstant.STATUS_PI_SUPPLIER_APPROVED);
                proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                //supplier and y4a will see all version
                proformaInvoice.setVersionLatestSupplier(Math.toIntExact(maxVersion));
                proformaInvoice.setVersionLatestY4a(Math.toIntExact(maxVersion));
                proformaInvoice.setStatusSourcing(null);
                proformaInvoice.setStatusPU(null);
                proformaInvoice.setUserUpdatedLatest(USER_UPDATED_SUPPLIER);
            } else if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_CONFIRMED)) {
                proformaInvoice.setIsConfirmed(false);
                if (isSourcing) {
                    proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                    // proformaInvoice.setStatusSourcing(STATUS_SOURCING_ADJUST);
                    // proformaInvoice.setStatusPU(null);
                } else {
                    proformaInvoice.setStepActionBy(STEP_ACTION_BY_PU);
                    // proformaInvoice.setStatusPU(STATUS_PU_ADJUST);
                    // proformaInvoice.setStatusSourcing(null);
                }

            }
            proformaInvoice.setIsSupplier(isSupplierFinal[0]);
            //get data send mail
            String poNumber = purchaseOrders.getPoNumber();
            String country = purchaseOrders.getCountry();
            Integer statusPI = proformaInvoice.getStatus();
            proformaInvoiceRepository.save(proformaInvoice);
            String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
            List<String> listEmailTo = new ArrayList<>();
            if (userPUPrimaryStr != null) {
                Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                if (oUserPUPrimary.isEmpty()) {
                    throw new BusinessException("Can not find user PU.");
                }
                User userPUPrimary = oUserPUPrimary.get();
                listEmailTo.add(userPUPrimary.getEmail());
            }
            String userSCPrimaryStr = proformaInvoice.getUserSourcingPrimary();
            if (userSCPrimaryStr != null) {
                Optional<User> oUserSCPrimary = userRepository.findOneByLogin(userSCPrimaryStr);
                if (oUserSCPrimary.isEmpty()) {
                    throw new BusinessException("Can not find user PU.");
                }
                User userSCPrimary = oUserSCPrimary.get();
                listEmailTo.add(userSCPrimary.getEmail());
            }
            Optional<User> oUserSupplier = userRepository.findOneByVendor(supplier);
            if (oUserSupplier.isEmpty()) {
                throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", supplier));
            }
            User userSupplier = oUserSupplier.get();
            List<String> listMailSC = userService.getListUserSC(userSupplier);
            List<String> listMailPU = userService.getListUserPU(userSupplier);
            String supplierName = (userSupplier.getLastName() == null ? "" : userSupplier.getLastName() + " ") + userSupplier.getFirstName();
            if (statusPI.equals(GlobalConstant.STATUS_PI_Y4A_ADJUST)) {
                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + id + "?type=DI", poNumber, supplierName, "The Proforma Invoice", "replied", "Replied");
                List<String> listMailCC = new ArrayList<>();
                List<String> listEmail = new ArrayList<>();
                listMailCC.addAll(listMailSC);
                listMailCC.addAll(listMailPU);
                listEmail.add(userSupplier.getEmail());
                String subject = getSubjectMail(poNumber, country, userSupplier);
                sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
            }
            if (statusPI.equals(GlobalConstant.STATUS_PI_SUPPLIER_APPROVED)) {

                List<String> listMailCC = new ArrayList<>();
                listMailCC.addAll(listMailSC);
                listMailCC.addAll(listMailPU);
                listMailCC.add(userSupplier.getEmail());
                String subject = getSubjectMail(poNumber, country, userSupplier);
                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + id + "?type=DI", poNumber, supplierName, "The Proforma Invoice", "replied", "Replied");
                sendMailService.sendMail(subject, content, listEmailTo, listMailCC, null, null);
            }
            return mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean confirmed(List<Integer> id) {
        try {
            List<String> listPI = new ArrayList<>();
            id.forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (Objects.equals(proformaInvoice.getStatus(), STATUS_PI_CONFIRMED)
                        || !Objects.equals(proformaInvoice.getStatusSourcing(), STATUS_SOURCING_CONFIRMED)
                    ) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_CONFIRMED);
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_CONFIRMED);
                        proformaInvoice.setIsConfirmed(true);
                        proformaInvoice.setStepActionBy(null);
                        proformaInvoice.setStatusSourcing(null);
                        proformaInvoice.setStatusPU(null);
                        Optional<ProformaInvoiceDetail> oProformaInvoiceDetail = proformaInvoiceDetailRepository.findTop1CdcVersionByProformaInvoiceOrderByCdcVersionDesc(proformaInvoice);
                        if (oProformaInvoiceDetail.isEmpty()) {
                            throw new BusinessException("Can not find new version detail proforma invoice");
                        }
                        //get new cdcVersion detail
                        Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();
                        //supplier and y4a will see all version
                        proformaInvoice.setVersionLatestY4a(Math.toIntExact(cdcVersionMax));
                        proformaInvoice.setVersionLatestSupplier(Math.toIntExact(cdcVersionMax));
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                        if (userEmailsSupplier.isEmpty()) {
                            throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                        }
                        User userVendor = userEmailsSupplier.get();
                        List<String> listEmail = new ArrayList<>();
                        proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                        listEmail.add(userVendor.getEmail());
                        List<String> listMailSC = userService.getListUserSC(userVendor);
                        List<String> listMailPU = userService.getListUserPU(userVendor);
                        List<String> listMailCC = new ArrayList<>();
                        listMailCC.addAll(listMailSC);
                        listMailCC.addAll(listMailPU);
                        String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), userVendor);
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?type=DI", proformaInvoice.getPurchaseOrders().getPoNumber(), "Yes4all", "The Proforma invoice", "confirmed", "Confirmed");
                        sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                    }
                }
            });
            if (!listPI.isEmpty()) {
                throw new BusinessException("The status Proforma Invoice not valid. Please check again. ", String.join(",", listPI));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean confirmedDetailPI(ActionSingleIdDTO request) {
        try {
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(request.getId());
            if (oProformaInvoice.isPresent()) {
                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                Optional<User> oUserAction = userRepository.findOneByLogin(request.getUserId());
                User user;
                if (oUserAction.isPresent()) {
                    user = oUserAction.get();
                    if (user.getSourcing()) {
                        proformaInvoice.setStepActionBy(STEP_ACTION_BY_PU);
                        proformaInvoice.setUserUpdatedLatest(USER_UPDATED_SOURCING);
                        if (proformaInvoice.getUserSourcingPrimary() == null) {
                            proformaInvoice.setUserSourcingPrimary(request.getUserId());
                        }
                        proformaInvoice.setStatusPU(null);
                    } else {
                        //proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                        proformaInvoice.setUserUpdatedLatest(USER_UPDATED_PU);
                    }
                } else {
                    throw new BusinessException("Can not find User.");
                }
                if (user.getSourcing()) {
                    proformaInvoice.setStatusSourcing(GlobalConstant.STATUS_SOURCING_CONFIRMED);
                } else {
                    proformaInvoice.setStatusPU(GlobalConstant.STATUS_PU_CONFIRMED);
                }
                Optional<User> userEmailsSupplier = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                if (userEmailsSupplier.isEmpty()) {
                    throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                }
                List<String> listEmail = new ArrayList<>();
                List<String> listEmailCC = new ArrayList<>();
                User userSupplier = userEmailsSupplier.get();
                List<String> listMailSC = userService.getListUserSC(userSupplier);
                List<String> listMailPU = userService.getListUserPU(userSupplier);

                if (user.getSourcing()) {
                    String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                    if (userPUPrimaryStr != null) {
                        Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                        if (oUserPUPrimary.isEmpty()) {
                            throw new BusinessException("Can not find user PU.");
                        }
                        User userPUPrimary = oUserPUPrimary.get();
                        listEmail.add(userPUPrimary.getEmail());
                        listEmailCC.addAll(listMailSC);
                        listEmailCC.addAll(listMailPU);
                    }
                }
                proformaInvoice.setSendMail(false);
                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?type=DI", proformaInvoice.getOrderNo(), "", "The Proforma Invoice", "replied", GlobalConstant.CONFIRM_SOURCING_PI);
                proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                String subject = getSubjectMail(proformaInvoice.getPurchaseOrders().getPoNumber(), proformaInvoice.getPurchaseOrders().getCountry(), userSupplier);
                sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);

            } else {
                throw new BusinessException("Can not find Proforma Invoice");
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean changeStep(ActionSingleIdDTO request) {
        try {
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(request.getId());
            if (oProformaInvoice.isPresent()) {
                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                Optional<User> oUserAction = userRepository.findOneByLogin(request.getUserId());
                User user;
                if (oUserAction.isPresent()) {
                    user = oUserAction.get();
                    if (user.getSourcing()) {
                        proformaInvoice.setStepActionBy(STEP_ACTION_BY_PU);
                        proformaInvoice.setStatusSourcing(STATUS_SOURCING_ADJUST);
                        proformaInvoice.setStatusPU(null);
                    } else {
                        proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                        proformaInvoice.setStatusSourcing(null);
                        proformaInvoice.setStatusPU(STATUS_PU_ADJUST);
                    }
                } else {
                    throw new BusinessException("Can not find User.");
                }
                Set<ProformaInvoiceDetail> details = proformaInvoice.getProformaInvoiceDetail();
                List<Long> maxVersions = details.stream().map(i -> i.getCdcVersion() == null ? 0 : i.getCdcVersion()).collect(Collectors.toList());
                Long maxVersion = Collections.max(maxVersions, null);
                User finalUser = user;
                details = details.stream().map(item -> {
                    ProformaInvoiceDetail proformaInvoiceDetail;
                    proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                    Set<ProformaInvoiceDetailLog> detailLogs = new HashSet<>();
                    if (!item.getProformaInvoiceDetailLog().isEmpty()) {
                        detailLogs = item.getProformaInvoiceDetailLog();
                    }
                    if (Objects.equals(maxVersion, item.getCdcVersion())) {
                        Set<ProformaInvoiceDetailLog> detailLogNews;
                        String userType = "PU";
                        if (finalUser.getSourcing()) {
                            userType = "SOURCING";
                        }
                        detailLogNews = setLogDetail(item, detailLogs, userType, request.getUserId());
                        proformaInvoiceDetail.setProformaInvoiceDetailLog(detailLogNews);
                    } else {
                        proformaInvoiceDetail.setProformaInvoiceDetailLog(detailLogs);
                    }
                    return proformaInvoiceDetail;
                }).collect(Collectors.toSet());
                proformaInvoice.setProformaInvoiceDetail(details);
                proformaInvoiceRepository.save(proformaInvoice);
                List<String> listEmail = new ArrayList<>();
                List<String> listEmailCC = new ArrayList<>();
                Optional<User> oUserSupplier = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                if (oUserSupplier.isEmpty()) {
                    throw new BusinessException("Can not find user Supplier.");
                }
                User userSupplier = oUserSupplier.get();
                List<String> listMailSC = userService.getListUserSC(userSupplier);
                List<String> listMailPU = userService.getListUserPU(userSupplier);

                if (!user.getSourcing()) {
                    String userSCPrimaryStr = proformaInvoice.getUserSourcingPrimary();
                    if (userSCPrimaryStr != null) {
                        Optional<User> oUserSCPrimary = userRepository.findOneByLogin(userSCPrimaryStr);
                        if (oUserSCPrimary.isEmpty()) {
                            throw new BusinessException("Can not find user PU.");
                        }
                        User userSCPrimary = oUserSCPrimary.get();
                        listEmail.add(userSCPrimary.getEmail());
                        listEmailCC.addAll(listMailSC);
                        listEmailCC.addAll(listMailPU);
                    } else {
                        listEmail.addAll(listMailSC);
                        listEmailCC.addAll(listMailPU);
                    }
                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?type=DI", proformaInvoice.getOrderNo(), "", "The Proforma Invoice", "", GlobalConstant.CONFIRM_PU_PI);
                    String subject = getSubjectMail(proformaInvoice.getPurchaseOrders().getPoNumber(), proformaInvoice.getPurchaseOrders().getCountry(), userSupplier);
                    sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                } else {
                    String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                    if (userPUPrimaryStr != null) {
                        Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                        if (oUserPUPrimary.isEmpty()) {
                            throw new BusinessException("Can not find user PU.");
                        }
                        User userPUPrimary = oUserPUPrimary.get();
                        listEmail.add(userPUPrimary.getEmail());
                        listEmailCC.addAll(listMailSC);
                        listEmailCC.addAll(listMailPU);
                    } else {
                        listEmail.addAll(listMailPU);
                        listEmailCC.addAll(listMailSC);
                    }
                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?type=DI", proformaInvoice.getOrderNo(), "", "The Proforma Invoice", "", CONFIRM_SOURCING_PI);
                    String subject = getSubjectMail(proformaInvoice.getPurchaseOrders().getPoNumber(), proformaInvoice.getPurchaseOrders().getCountry(), userSupplier);
                    sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);

                }
            } else {
                throw new BusinessException("Can not find Proforma Invoice");
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public Set<ProformaInvoiceDetailLog> setLogDetail(ProformaInvoiceDetail item, Set<ProformaInvoiceDetailLog> detailLogs, String userType, String userId) {
        Map<String, Integer> columnsVersion = new HashMap<>();
        detailLogs.forEach(element -> {
            int version = element.getVersion() == null ? 0 : element.getVersion();
            if (columnsVersion.get(element.getColumnChange().getCode()) != null && columnsVersion.get(element.getColumnChange().getCode()) < version) {
                columnsVersion.put(element.getColumnChange().getCode(), version);
            } else {
                columnsVersion.putIfAbsent(element.getColumnChange().getCode(), version);
            }
        });

        Set<ProformaInvoiceDetailLog> detailLogSet = new HashSet<>(detailLogs);
        List<String> enumColumns = Stream.of(EnumColumn.values())
            .map(EnumColumn::name)
            .collect(Collectors.toList());
        for (String column : enumColumns) {
            double valueAfter = 0;
            double valueBefore = 0;
            switch (column) {
                case "QTY":
                    valueAfter = item.getQty();
                    valueBefore = item.getQtyPrevious();
                    break;
                case "UNIT_PRICE":
                    valueAfter = item.getUnitPrice();
                    valueBefore = item.getUnitPricePrevious();
                    break;
                case "AMOUNT":
                    valueAfter = item.getAmount();
                    valueBefore = item.getAmountPrevious();
                    break;
                case "PCS":
                    valueAfter = item.getPcs();
                    valueBefore = item.getPcsPrevious();
                    break;
                case "CTN":
                    valueAfter = item.getTotalBox();
                    valueBefore = item.getTotalBoxPrevious();
                    break;
                case "CBM":
                    valueAfter = item.getTotalVolume();
                    valueBefore = item.getTotalVolumePrevious();
                    break;
                case "NW":
                    valueAfter = item.getNetWeight();
                    valueBefore = item.getNetWeightPrevious();
                    break;
                case "GW":
                    valueAfter = item.getGrossWeight();
                    valueBefore = item.getGrossWeightPrevious();
                    break;
                default:
                    // code block
            }
            if (valueAfter != valueBefore) {
                Integer maxVersion = columnsVersion.get(column);
                if (maxVersion == null) {
                    maxVersion = 0;
                }
                ProformaInvoiceDetailLog proformaInvoiceDetailLogNew = null;
                if (detailLogs.isEmpty()) {
                    proformaInvoiceDetailLogNew = getLogDetail(column, maxVersion, userType, userId, valueAfter, valueBefore);
                } else {
                    Integer finalMaxVersion = maxVersion;
                    Optional<ProformaInvoiceDetailLog> oProformaInvoiceDetailLog = detailLogs.stream().filter(
                        i -> i.getVersion().equals(finalMaxVersion) && i.getColumnChange().equals(EnumColumn.valueOf(column))
                    ).findFirst();
                    if (oProformaInvoiceDetailLog.isPresent()) {
                        ProformaInvoiceDetailLog proformaInvoiceDetailLog = oProformaInvoiceDetailLog.get();
                        if (proformaInvoiceDetailLog.getValueAfter() != valueAfter || proformaInvoiceDetailLog.getValueBefore() != valueBefore) {
                            proformaInvoiceDetailLogNew = getLogDetail(column, maxVersion, userType, userId, valueAfter, valueBefore);
                        }
                    } else {
                        proformaInvoiceDetailLogNew = getLogDetail(column, maxVersion, userType, userId, valueAfter, valueBefore);
                    }
                }
                if (proformaInvoiceDetailLogNew != null) {
                    detailLogSet.add(proformaInvoiceDetailLogNew);
                }

            }
        }
        return detailLogSet;
    }

    public ProformaInvoiceDetailLog getLogDetail(String column, int maxVersion, String userType, String userId, double valueAfter, double valueBefore) {
        ProformaInvoiceDetailLog proformaInvoiceDetailLogNew = new ProformaInvoiceDetailLog();
        proformaInvoiceDetailLogNew.setColumnChange(EnumColumn.valueOf(column));
        proformaInvoiceDetailLogNew.setVersion(maxVersion + 1);
        proformaInvoiceDetailLogNew.setUpdatedDate(new Date().toInstant());
        proformaInvoiceDetailLogNew.setUserType(EnumUserType.valueOf(userType));
        proformaInvoiceDetailLogNew.setUpdatedBy(userId);
        proformaInvoiceDetailLogNew.setValueAfter(valueAfter);
        proformaInvoiceDetailLogNew.setValueBefore(valueBefore);
        return proformaInvoiceDetailLogNew;
    }

    @Override
    public boolean unConfirmed(List<Integer> id) {
        try {
            List<String> listPI = new ArrayList<>();
            id.forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (proformaInvoice.getStatus() != GlobalConstant.STATUS_PI_CONFIRMED) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_Y4A_ADJUST);
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_Y4A_ADJUST);
                        proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                    }
                }
            });
            if (!listPI.isEmpty()) {
                throw new BusinessException("The status Proforma Invoice not valid. Please check again.", String.join(",", listPI));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean approved(List<Integer> id) {
        try {
            List<String> listPI = new ArrayList<>();
            id.forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (!Objects.equals(proformaInvoice.getStatus(), STATUS_PI_Y4A_ADJUST)
                        && !Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_REVIEW)) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_SUPPLIER_APPROVED);
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_SUPPLIER_APPROVED);
                        proformaInvoice.setStatusSourcing(null);
                        proformaInvoice.setStatusPU(null);
                        proformaInvoice.setIsConfirmed(true);
                        proformaInvoice.setUserUpdatedLatest(USER_UPDATED_SUPPLIER);
                        proformaInvoiceRepository.save(proformaInvoice);
                        List<String> listEmail = new ArrayList<>();
                        List<String> listEmailCC = new ArrayList<>();

                        Optional<User> oUserSupplier = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                        if (oUserSupplier.isEmpty()) {
                            throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                        }
                        User userSupplier = oUserSupplier.get();
                        List<String> listMailSC = userService.getListUserSC(userSupplier);
                        List<String> listMailPU = userService.getListUserPU(userSupplier);
                        String supplier = (userSupplier.getLastName() == null ? "" : userSupplier.getLastName() + " ") + userSupplier.getFirstName();
                        String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                        String userSCPrimaryStr = proformaInvoice.getUserSourcingPrimary();
                        if (userPUPrimaryStr != null) {
                            Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                            if (oUserPUPrimary.isEmpty()) {
                                throw new BusinessException("Can not find user PU.");
                            }
                            User userPUPrimary = oUserPUPrimary.get();
                            listEmail.add(userPUPrimary.getEmail());
                            listEmailCC.addAll(listMailSC);
                            listEmailCC.addAll(listMailPU);
                        } else {
                            listEmail.addAll(listMailPU);
                            listEmailCC.addAll(listMailSC);
                            listEmailCC.add(userSupplier.getEmail());
                        }
                        if (userSCPrimaryStr != null) {
                            Optional<User> oUserSCPrimary = userRepository.findOneByLogin(userSCPrimaryStr);
                            if (oUserSCPrimary.isEmpty()) {
                                throw new BusinessException("Can not find user PU.");
                            }
                            User userSCPrimary = oUserSCPrimary.get();
                            listEmail.add(userSCPrimary.getEmail());
                            listEmailCC.addAll(listMailSC);
                            listEmailCC.addAll(listMailPU);
                            listEmailCC.add(userSupplier.getEmail());
                        } else {
                            listEmail.addAll(listMailPU);
                            listEmailCC.addAll(listMailSC);
                        }
                        proformaInvoice.setNewVersionDetail(false);
                        String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), userSupplier);
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?type=DI", proformaInvoice.getPurchaseOrders().getPoNumber(), supplier, "The Proforma Invoice", "replied", "Replied");
                        sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                    }
                }
            });
            if (!listPI.isEmpty()) {
                throw new BusinessException("The status Proforma Invoice not valid. Please check again.", String.join(",", listPI));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean send(ListIdDTO request) {
        try {
            List<String> listPI = new ArrayList<>();
            request.getId().forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (!Objects.equals(proformaInvoice.getStatus(), STATUS_PI_NEW)
                        && !Objects.equals(proformaInvoice.getStatus(), STATUS_PI_CONFIRMED)
                        && !Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_APPROVED)
                        && !Objects.equals(proformaInvoice.getStatus(), STATUS_PI_Y4A_REVIEW)
                        && !Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_REVIEW)) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        if (request.getVendor() == null || request.getVendor().length() == 0 && (Objects.equals(proformaInvoice.getStatus(), STATUS_PI_NEW))) {
                            throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);

                        }
                        List<String> listEmail = new ArrayList<>();
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                        if (userEmailsSupplier.isEmpty()) {
                            throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                        }

//                        List<Resource> resourcesListing = resourceRepository.findByFileTypeAndProformaInvoiceId(GlobalConstant.FILE_UPLOAD, proformaInvoice.getId());
//                        if (!CommonDataUtil.isNotEmpty(resourcesListing)) {
//                            throw new BusinessException("Please attach the necessary file(s) before clicking Send.");
//                        }
                        User userVendor = userEmailsSupplier.get();
                        Set<ProformaInvoiceDetail> details = proformaInvoice.getProformaInvoiceDetail();
                        List<Long> maxVersions = details.stream().map(m -> m.getCdcVersion() == null ? 0 : m.getCdcVersion()).collect(Collectors.toList());
                        Long maxVersion = Collections.max(maxVersions, null);
                        //supplier and y4a will see all version
                        proformaInvoice.setVersionLatestSupplier(Math.toIntExact(maxVersion));
                        proformaInvoice.setVersionLatestY4a(Math.toIntExact(maxVersion));
                        if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_SUPPLIER_REVIEW)) {
                            details = details.stream().map(item -> {
                                ProformaInvoiceDetail proformaInvoiceDetail;
                                proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                                Set<ProformaInvoiceDetailLog> detailLogs = new HashSet<>();
                                if (!item.getProformaInvoiceDetailLog().isEmpty()) {
                                    detailLogs = item.getProformaInvoiceDetailLog();
                                }
                                if (maxVersion.equals(item.getCdcVersion())) {
                                    Set<ProformaInvoiceDetailLog> detailLogNews;
                                    String userType = "SUPPLIER";
                                    detailLogNews = setLogDetail(item, detailLogs, userType, request.getUserId());
                                    proformaInvoiceDetail.setProformaInvoiceDetailLog(detailLogNews);
                                } else {
                                    proformaInvoiceDetail.setProformaInvoiceDetailLog(detailLogs);
                                }
                                return proformaInvoiceDetail;
                            }).collect(Collectors.toSet());
                            proformaInvoice.setProformaInvoiceDetail(details);
                        }
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        Integer statusPI = proformaInvoice.getStatus();
                        //After Y4a review send for Supplier review
                        if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_Y4A_REVIEW)) {
                            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_SUPPLIER_REVIEW);
                            proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                            proformaInvoice.setStatusPU(null);
                            proformaInvoice.setStatusSourcing(null);
                            proformaInvoice.setSendMail(false);
                        } else if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_NEW)) {
                            //Supplier send for Y4a review
                            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_Y4A_REVIEW);
                            proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_RECEIVED);
                        } else if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_SUPPLIER_REVIEW)) {
                            //Supplier send for Y4a review
                            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_Y4A_REVIEW);
                            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_REVIEWING);
                            proformaInvoice.setStatusPU(null);
                            proformaInvoice.setStatusSourcing(null);
                            proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                            proformaInvoice.setSendMail(false);
                        } else if (proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_CONFIRMED) || proformaInvoice.getStatus().equals(STATUS_PI_SUPPLIER_APPROVED)) {
                            //Supplier send for Y4a review OR PU SEND FOR SUPPLIER AFTER CONFIRMED
                            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_Y4A_ADJUST);
                            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_Y4A_ADJUST);
                        }
                        if (proformaInvoice.getStatus().equals(STATUS_PI_Y4A_REVIEW) || proformaInvoice.getStatus().equals(STATUS_PI_SUPPLIER_APPROVED)) {
                            proformaInvoice.setSendMail(false);
                        }
                        List<String> listMailSC = userService.getListUserSC(userVendor);
                        List<String> listMailPU = userService.getListUserPU(userVendor);
                        String poNumber = proformaInvoice.getPurchaseOrders().getPoNumber();
                        String country = proformaInvoice.getPurchaseOrders().getCountry();
                        Integer id = proformaInvoice.getId();
                        String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                        String userSCPrimaryStr = proformaInvoice.getUserSourcingPrimary();
                        if (request.getUserId().equals(userVendor.getLogin())) {
                            String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                            String status = "created";
                            String type = "CreatedPI";
                            String object = "The Proforma invoice for PO";
                            if (statusPI == GlobalConstant.STATUS_PI_SUPPLIER_REVIEW) {
                                status = "adjusted";
                                type = "Adjusted";
                                object = "The Proforma invoice";
                            }

                            // Create file attachment
                            List<Resource> fileAttachment = resourceRepository.findByFileTypeAndProformaInvoiceId(GlobalConstant.FILE_UPLOAD, id);
//                            if (fileAttachment.isEmpty()) {
//                                throw new BusinessException("Can not find file attachment.");
//                            }
                            Map<String, String> attachments = new HashMap<>();
                            if (!fileAttachment.isEmpty()) {
                                Optional<Resource> oResource = fileAttachment.stream().findFirst();
                                Resource resource = oResource.get();
                                String filename = resource.getName();
                                String nameTemplate = "Proforma-Invoice.xlsx";
                                String filePath = GlobalConstant.FILE_UPLOAD_FOLDER_PATH + MODULE_PROFORMA_INVOICE + "/" + id + File.separator + filename;
                                File file = new File(filePath);
                                // export(fileName, proformaInvoice.getId(), false);
                                attachments.put(nameTemplate, file.getPath());
                            }

                            // Send mail
                            // get all mail SC+PU

                            // get user pu primary
                            if (userPUPrimaryStr == null) {
                                throw new BusinessException("Can not find user PU.");
                            }
                            Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                            if (oUserPUPrimary.isEmpty()) {
                                throw new BusinessException("Can not find user PU.");
                            }
                            User userPUPrimary = oUserPUPrimary.get();
                            listEmail.add(userPUPrimary.getEmail());
                            List<String> listMailCC = new ArrayList<>(listMailPU);
                            if (userSCPrimaryStr != null) {
                                Optional<User> oUserSCPrimary = userRepository.findOneByLogin(userSCPrimaryStr);
                                if (oUserSCPrimary.isEmpty()) {
                                    throw new BusinessException("Can not find user PU.");
                                }
                                User userSCPrimary = oUserSCPrimary.get();
                                listEmail.add(userSCPrimary.getEmail());
                                listMailCC.addAll(listMailSC);
                            } else {
                                listEmail.addAll(listMailSC);
                            }
                            listMailCC.add(userVendor.getEmail());
                            String subject = getSubjectMail(poNumber, country, userVendor);
                            String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + id + "?type=DI", poNumber, supplier, object, status, type);
                            sendMailService.sendMail(subject, content, listEmail, listMailCC, null, attachments);
                        } else {
                            listEmail.add(userVendor.getEmail());
                            List<String> listMailCC = new ArrayList<>();
                            listMailCC.addAll(listMailSC);
                            listMailCC.addAll(listMailPU);
                            String subject = getSubjectMail(poNumber, country, userVendor);
                            String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + id + "?type=DI", poNumber, "Yes4all", "The Proforma invoice", "adjusted", "Adjusted");
                            sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                        }
                        proformaInvoiceRepository.save(proformaInvoice);

                    }
                }
            });
            if (!listPI.isEmpty()) {
                throw new BusinessException(String.format("Proforma Invoice { %s } were sent to Yes4All. Please check again.", String.join(",", listPI)));
            }
            return true;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }

    }



    private <T> T mappingEntityToDto(ProformaInvoice proformaInvoice, Class<T> clazz, Boolean isSupplier) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(proformaInvoice, dto);
            ProformaInvoice objectModel = null;
            if (proformaInvoice.getProformaInvoiceDetail().isEmpty()) {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(proformaInvoice.getId());
                if (oProformaInvoice.isPresent()) {
                    objectModel = oProformaInvoice.get();
                }
            } else {
                objectModel = proformaInvoice;
            }
            if (objectModel == null) {
                objectModel = proformaInvoice;
            }
            final String[] bookingNumber = {null};
            final Integer[] bookingId = {null};
            List<ProformaInvoiceDetail> detailSet = objectModel.getProformaInvoiceDetail().stream().filter(i -> !i.isDeleted()).sorted(Comparator.comparing(ProformaInvoiceDetail::getSku)).collect(Collectors.toList());
            List<BookingProformaInvoice> bookingProformaInvoiceList = bookingProformaInvoiceRepository.findAllByProformaInvoiceNo(proformaInvoice.getOrderNo());
            bookingProformaInvoiceList.forEach(bookingProformaInvoice -> {
                if (bookingProformaInvoice.getBookingPackingList() != null && (bookingProformaInvoice.getBooking().getStatus() != GlobalConstant.STATUS_BOOKING_CANCEL) && !Objects.equals(bookingProformaInvoice.getBooking().getStatus(), STATUS_BOOKING_UPLOAD)) {
                    bookingNumber[0] = bookingProformaInvoice.getBooking().getBookingConfirmation();
                    bookingId[0] = bookingProformaInvoice.getBooking().getId();
                }
            });
            String fromSoStr = detailSet.stream().map(ProformaInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(", "));
            Optional<User> oUser = userRepository.findOneByLogin(proformaInvoice.getUpdatedBy());
            String updatedBy = "";
            if (oUser.isPresent()) {
                updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
            }
            Optional<User> oUserCreated = userRepository.findOneByLogin(proformaInvoice.getCreatedBy());
            String createdBy = "";
            if (oUserCreated.isPresent()) {
                createdBy = oUserCreated.get().getLastName() + " " + oUserCreated.get().getFirstName();
            }
            if (dto instanceof ProformaInvoiceDTO) {
                //check permissions user Y4a and Supplier can view new version
                //-----

                Optional<ProformaInvoiceDetail> oProformaInvoiceDetail = proformaInvoiceDetailRepository.findTop1CdcVersionByProformaInvoiceOrderByCdcVersionDesc(proformaInvoice);
                if (oProformaInvoiceDetail.isEmpty()) {
                    throw new BusinessException("Can not find new version detail proforma invoice");
                }
                Map<String, List<ProformaInvoiceDetailDTO>> detailDTO = new HashMap<>();
                detailSet.stream().filter(i -> {
                    if (isSupplier && (Objects.equals(proformaInvoice.getStatus(), STATUS_PI_Y4A_REVIEW)
                        || Objects.equals(proformaInvoice.getStatus(), STATUS_PI_CONFIRMED) || Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_APPROVED))) {
                        return i.getCdcVersion() <= proformaInvoice.getVersionLatestSupplier();
                    }
                    if (!isSupplier && (Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_REVIEW) || Objects.equals(proformaInvoice.getStatus(), STATUS_PI_SUPPLIER_APPROVED))) {
                        return i.getCdcVersion() <= proformaInvoice.getVersionLatestY4a();
                    }
                    return true;
                }).forEach(item -> {
                    ProformaInvoiceDetailDTO proformaInvoiceDetailDTO;
                    Set<ProformaInvoiceDetailLog> proformaInvoiceDetailLogs = item.getProformaInvoiceDetailLog();
                    proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetailDTO.class);
                    Optional<User> oUserDetail = userRepository.findOneByLogin(item.getUpdatedBy());
                    String updatedDetailBy = "";
                    if (oUserDetail.isPresent()) {
                        updatedDetailBy = oUserDetail.get().getLastName() + " " + oUserDetail.get().getFirstName();
                    }
                    proformaInvoiceDetailDTO.setUpdatedNameBy(updatedDetailBy);
                    //get max version of log in detail PI
                    Integer maxVersion;
                    List<Integer> maxVersions;
                    if (isSupplier) {
                        maxVersions = proformaInvoiceDetailLogs.stream().filter(m -> false).map(i -> i.getVersion() == null ? 0 : i.getVersion()).collect(Collectors.toList());
                    } else {
                        maxVersions = proformaInvoiceDetailLogs.stream().map(i -> i.getVersion() == null ? 0 : i.getVersion()).collect(Collectors.toList());
                    }
                    if (maxVersions.isEmpty()) {
                        maxVersion = null;
                    } else {
                        maxVersion = Collections.max(maxVersions, null);
                    }
                    //set log
                    List<ProformaInvoiceDetailLogDTO> detailLogDTOS = proformaInvoiceDetailLogs.stream().map(element -> {
                        ProformaInvoiceDetailLogDTO proformaInvoiceDetailLogDTO;
                        proformaInvoiceDetailLogDTO = CommonDataUtil.getModelMapper().map(element, ProformaInvoiceDetailLogDTO.class);
                        if (maxVersion != null && (proformaInvoiceDetailLogDTO.getVersion() > maxVersion)) {
                            return null;

                        }
                        Optional<User> oUserDetailLog = userRepository.findOneByLogin(element.getUpdatedBy());
                        String updatedDetailLogBy = "";
                        if (oUserDetailLog.isPresent()) {
                            updatedDetailLogBy = oUserDetailLog.get().getLastName() + " " + oUserDetailLog.get().getFirstName();
                        }
                        proformaInvoiceDetailLogDTO.setUpdatedNameBy(updatedDetailLogBy);
                        return proformaInvoiceDetailLogDTO;
                    }).filter(Objects::nonNull).sorted(Comparator.comparing(ProformaInvoiceDetailLogDTO::getUpdatedDate).reversed()).collect(Collectors.toList());
                    proformaInvoiceDetailDTO.setProformaInvoiceDetailLog(detailLogDTOS);
                    List<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                    if (detailDTO.get(TITLE_HASHMAP + item.getCdcVersion()) != null) {
                        proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + item.getCdcVersion());
                    }
                    proformaInvoiceDetailDTOSet.add(proformaInvoiceDetailDTO);
                    detailDTO.put(TITLE_HASHMAP + item.getCdcVersion(), proformaInvoiceDetailDTOSet);
                });
                clazz.getMethod("setIsNotSend", Boolean.class).invoke(dto, proformaInvoice.getIsConfirmed());
                clazz.getMethod("setProformaInvoiceDetail", Map.class).invoke(dto, detailDTO);
            } else if (dto instanceof ProformaInvoiceMainDTO) {
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setBookingNumber", String.class).invoke(dto, bookingNumber[0]);
                clazz.getMethod("setBookingId", Integer.class).invoke(dto, bookingId[0]);
                clazz.getMethod("setUpdatedBy", String.class).invoke(dto, updatedBy);
                clazz.getMethod("setCreatedBy", String.class).invoke(dto, createdBy);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }


}
