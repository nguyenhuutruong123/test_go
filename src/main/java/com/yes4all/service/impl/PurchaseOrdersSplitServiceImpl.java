package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.PurchaseOrdersSplitService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link PurchaseOrders}.
 */
@Service
@Transactional
public class PurchaseOrdersSplitServiceImpl implements PurchaseOrdersSplitService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersSplitServiceImpl.class);
    private static final String KEY_UPLOAD = ";";
    @Autowired
    private PurchaseOrdersSplitRepository purchaseOrdersSplitRepository;

    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;

    @Autowired
    private PurchaseOrdersSplitResultRepository purchaseOrdersSplitResultRepository;

    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;
    @Autowired
    private PurchaseOrdersSplitDataRepository purchaseOrdersSplitDataRepository;

    @Autowired
    private PurchaseOrdersCommonService purchaseOrdersCommonService;

    @Override
    public List<PurchaseOrdersSplit> createPurchaseOrdersSplit(List<ResultUploadDTO> data, String user) {
        try {
            List<PurchaseOrdersSplit> purchaseOrdersSplitList = new ArrayList<>();
            data.forEach(i -> i.getPurchaseOrdersSplit().forEach((fileName, value) -> {
                PurchaseOrdersSplit purchaseOrdersSplit = new PurchaseOrdersSplit();
                Set<PurchaseOrdersSplitData> values = new HashSet<>(value);
                purchaseOrdersSplit.setRootFile(fileName);
                purchaseOrdersSplit.setCreatedBy(user);
                purchaseOrdersSplit.setStatus(GlobalConstant.STATUS_PO_SPLIT_NEW);
                purchaseOrdersSplit.setCreatedDate(new Date().toInstant());
                purchaseOrdersSplit.setUpdatedBy(user);
                purchaseOrdersSplit.setUpdatedDate(new Date().toInstant());
                purchaseOrdersSplit.setPurchaseOrdersSplitData(values);
                purchaseOrdersSplitList.add(purchaseOrdersSplit);
            }));
            purchaseOrdersSplitRepository.saveAllAndFlush(purchaseOrdersSplitList);
            return purchaseOrdersSplitList;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    private PurchaseOrderSplitDataDTO convertToObjectDto(Object o) {
        PurchaseOrderSplitDataDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, PurchaseOrderSplitDataDTO.class);
        return dto;
    }

    private PurchaseOrderSplitResultDTO convertToObjectResultDto(Object o) {
        PurchaseOrderSplitResultDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, PurchaseOrderSplitResultDTO.class);
        return dto;
    }


    @Override
    public PurchaseOrderDataPageDTO getPurchaseOrdersSplitData(BodyListingDTO request) {
        try {
            if (request.getVendor().length() > 0) {
                throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
            }
            Optional<PurchaseOrdersSplit> purchaseOrdersSplit = purchaseOrdersSplitRepository.findById(request.getId());
            if (purchaseOrdersSplit.isPresent()) {
                PurchaseOrdersSplit purchaseOrders = purchaseOrdersSplit.get();
                PurchaseOrderDataPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDataPageDTO.class);
                Pageable pageable = PageRequestUtil.genPageRequest(request.getPage(), request.getSize(), Sort.Direction.DESC, "sku");
                Page<PurchaseOrdersSplitData> pagePurchaseOrdersSplitData = purchaseOrdersSplitDataRepository.findByPurchaseOrdersSplit(purchaseOrders, pageable);
                Page<PurchaseOrderSplitDataDTO> pagePurchaseOrderSplitDataDTO = pagePurchaseOrdersSplitData.map(this::convertToObjectDto);
                data.setPurchaseOrderSplitDataDTO(pagePurchaseOrderSplitDataDTO);
                return data;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public PurchaseOrderResultPageDTO getPurchaseOrdersSplitResult(BodyListingDTO request) {
        try {
            if (request.getVendor().length() > 0) {
                throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
            }
            Optional<PurchaseOrdersSplit> purchaseOrdersSplit = purchaseOrdersSplitRepository.findById(request.getId());
            if (purchaseOrdersSplit.isPresent()) {
                PurchaseOrdersSplit purchaseOrders = purchaseOrdersSplit.get();
                PurchaseOrderResultPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderResultPageDTO.class);
                Pageable pageable = PageRequestUtil.genPageRequest(request.getPage(), request.getSize(), Sort.Direction.DESC, "vendor");
                Page<PurchaseOrdersSplitResult> pagePurchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findByPurchaseOrdersSplit(purchaseOrders, pageable);
                Page<PurchaseOrderSplitResultDTO> pagePurchaseOrderSplitResultDTO = pagePurchaseOrdersSplitResult.map(this::convertToObjectResultDto);
                data.setPurchaseOrderSplitResultDTO(pagePurchaseOrderSplitResultDTO);
                return data;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public PurchaseOrderSplitResultDetailsDTO getPurchaseOrdersSplitResultDetail(BodyListingDTO request) {
        try {
            if (request.getVendor().length() > 0) {
                throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
            }

            Pageable pageable = PageRequestUtil.genPageRequest(request.getPage(), request.getSize(), Sort.Direction.DESC, "sku");
            Optional<PurchaseOrdersSplitResult> oPurchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(request.getId());
            if (oPurchaseOrdersSplitResult.isEmpty()) {
                return null;
            }
            PurchaseOrdersSplitResult purchaseOrdersSplitResult = oPurchaseOrdersSplitResult.get();
            PurchaseOrderSplitResultDetailsDTO data = CommonDataUtil.getModelMapper().map(purchaseOrdersSplitResult, PurchaseOrderSplitResultDetailsDTO.class);
            Page<PurchaseOrdersSplitData> pagePurchaseOrdersSplitData = purchaseOrdersSplitDataRepository.findByPurchaseOrdersSplitResult(purchaseOrdersSplitResult, pageable);
            Page<PurchaseOrderSplitDataDTO> pagePurchaseOrderSplitResultDetailsDTO = pagePurchaseOrdersSplitData.map(this::convertToObjectDto);
            data.setPurchaseOrderSplitDataDTO(pagePurchaseOrderSplitResultDetailsDTO);
            return data;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public Page<PurchaseOrdersMainSplitDTO> getAll(BodyListingDTO request) {
        try {
            if (request.getVendor().length() > 0) {
                throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
            }

            Pageable pageable = PageRequestUtil.genPageRequest(request.getPage(), request.getSize(), Sort.Direction.DESC, "createdDate");
            Page<PurchaseOrdersSplit> data = purchaseOrdersSplitRepository.findAll(pageable);
            return data.map(item -> CommonDataUtil.getModelMapper().map(item, PurchaseOrdersMainSplitDTO.class));

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public String getNameFile(Integer id) {
        Optional<PurchaseOrdersSplitResult> oPurchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(id);
        if (oPurchaseOrdersSplitResult.isEmpty()) {
            return null;
        }
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = oPurchaseOrdersSplitResult.get();
        return purchaseOrdersSplitResult.getOrderNo();
    }

    @Override
    public boolean removePurchaseOrdersSplit(List<Integer> listPurchaseOrderId, String userName) {
        try {
            listPurchaseOrderId.forEach(i -> {
                Optional<PurchaseOrdersSplit> oPurchaseOrder = purchaseOrdersSplitRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrdersSplit purchaseOrders = oPurchaseOrder.get();
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_SPLIT_DELETE);
                    purchaseOrders.setDeletedDate(new Date().toInstant());
                    purchaseOrders.setDeletedBy(userName);
                    purchaseOrdersSplitRepository.saveAndFlush(purchaseOrders);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public PurchaseOrdersSplit splitPurchaseOrder(Integer id) {
        try {
            Optional<PurchaseOrdersSplit> oPurchaseOrdersSplit = purchaseOrdersSplitRepository.findById(id);
            if (oPurchaseOrdersSplit.isEmpty()) {
                throw new BusinessException("PO split not found.");
            }
            PurchaseOrdersSplit purchaseOrdersSplit = oPurchaseOrdersSplit.get();
            if (!purchaseOrdersSplit.getPurchaseOrdersSplitResult().isEmpty()) {
                throw new BusinessException("This root file already split.");
            }
            purchaseOrdersSplit.setStatus(GlobalConstant.STATUS_PO_SPLIT_SPLIT);
            List<PurchaseOrdersSplitResult> purchaseOrdersSplitResultList = new ArrayList<>();
            Set<PurchaseOrdersSplitData> purchaseOrdersSplitDataSet = new HashSet<>();
            Map<String, List<PurchaseOrdersSplitData>> mapPOSplit = new HashMap<>();
            List<String> keymapPO;
            keymapPO = purchaseOrdersSplit.getPurchaseOrdersSplitData().stream().map(k -> k.getCountry() + KEY_UPLOAD + k.getVendor() + KEY_UPLOAD + k.getFulfillmentCenter() + KEY_UPLOAD + k.getShipDate()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());

            //add data into keymapPO (count PO can create by this split PO)
            for (String element : keymapPO) {
                List<PurchaseOrdersSplitData> listPurchaseOrdersSplitData = new ArrayList<>();
                purchaseOrdersSplit.getPurchaseOrdersSplitData().stream().filter(k -> {
                    String key = k.getCountry() + KEY_UPLOAD + k.getVendor() + KEY_UPLOAD + k.getFulfillmentCenter() + KEY_UPLOAD + k.getShipDate();
                    return key.equals(element);
                }).forEach(m -> {
                    PurchaseOrdersSplitData purchaseOrdersSplitData = new PurchaseOrdersSplitData();
                    CommonDataUtil.getModelMapper().map(m, purchaseOrdersSplitData);
                    listPurchaseOrdersSplitData.add(purchaseOrdersSplitData);
                });
                mapPOSplit.put(element, listPurchaseOrdersSplitData);
            }
            //create list PurchaseOrdersSplitResult
            mapPOSplit.entrySet().forEach(entry -> {
                String key = entry.getKey();
                String[] array = key.split(KEY_UPLOAD);
                String country = array[0];
                String vendor = array[1];
                String fulfillmentCenter = array[2];
                String shipDate = array[3];
                PurchaseOrdersSplitResult purchaseOrdersSplitResult = new PurchaseOrdersSplitResult();
                //get list purchase order data
                Set<PurchaseOrdersSplitData> purchaseOrdersSplitData = entry.getValue().stream().map(item -> {
                    PurchaseOrdersSplitData purchaseOrdersSplitDataDetail = new PurchaseOrdersSplitData();
                    CommonDataUtil.getModelMapper().map(item, purchaseOrdersSplitDataDetail);
                    purchaseOrdersSplitDataDetail.setPurchaseOrdersSplitResult(purchaseOrdersSplitResult);
                    purchaseOrdersSplitDataSet.add(purchaseOrdersSplitDataDetail);
                    return purchaseOrdersSplitDataDetail;
                }).collect(Collectors.toSet());
                String strSo = purchaseOrdersSplitData.stream().map(PurchaseOrdersSplitData::getSaleOrder).distinct().collect(Collectors.joining(", "));
                purchaseOrdersSplitResult.setVendor(vendor);
                purchaseOrdersSplitResult.setCountry(country);
                purchaseOrdersSplitResult.setFulfillmentCenter(fulfillmentCenter);
                purchaseOrdersSplitResult.setShipDate(DateUtils.convertStringLocalDateDDMMYYYY(shipDate));
                purchaseOrdersSplitResult.setSaleOrder(strSo);
                //create new orderNo formula: COUNTRY+"DI"+VENDOR+YEAR+4 number increase
                String orderNo = purchaseOrdersCommonService.generateOrderNo(country, vendor,false);
                purchaseOrdersSplitResult.setOrderNo(orderNo);
                purchaseOrdersSplitResult.setTotalQuantity(purchaseOrdersSplitData.stream().map(x -> Objects.isNull(x.getQtyOrdered()) ? 0 : x.getQtyOrdered()).reduce(0L, Long::sum));
                purchaseOrdersSplitResult.setTotalAmount(purchaseOrdersSplitData.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                purchaseOrdersSplitResult.setDemand(purchaseOrdersSplit.getRootFile());
                purchaseOrdersSplitResultList.add(purchaseOrdersSplitResult);

            });
            purchaseOrdersSplit.setPurchaseOrdersSplitResult(new HashSet<>(purchaseOrdersSplitResultList));
            purchaseOrdersSplit.setPurchaseOrdersSplitData(purchaseOrdersSplitDataSet);
            purchaseOrdersSplitRepository.saveAndFlush(purchaseOrdersSplit);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Integer> createPurchaseOrder(Integer id, String userId) {
        try {
            Optional<PurchaseOrdersSplit> oPurchaseOrdersSplit = purchaseOrdersSplitRepository.findById(id);
            if (oPurchaseOrdersSplit.isEmpty()) {
                throw new BusinessException("Split PO not found.");
            }
            PurchaseOrdersSplit purchaseOrdersSplit = oPurchaseOrdersSplit.get();
            List<Integer> listPO = new ArrayList<>();
            Set<PurchaseOrdersSplitResult> purchaseOrdersSplitResults = purchaseOrdersSplit.getPurchaseOrdersSplitResult();
            purchaseOrdersSplitResults.forEach(result -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findByPoNumberAndIsDeleted(result.getOrderNo(), false);
                if (oPurchaseOrder.isPresent()) {
                    throw new BusinessException("Po number already exists in Purchase Order");
                }

                PurchaseOrders purchaseOrders = new PurchaseOrders();
                purchaseOrders.setIsDeleted(false);
                purchaseOrders.setPoNumber(result.getOrderNo());
                purchaseOrders.setCreatedBy(userId);
                purchaseOrders.setCreatedDate(new Date().toInstant());
                purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEW);
                purchaseOrders.setIsSendmail(false);
                purchaseOrders.setDemand(result.getDemand());
                //0 is Direct Import
                purchaseOrders.setChannel(0);
                Set<PurchaseOrdersDetail> purchaseOrdersDetailSet = new HashSet<>();
                List<String> checkDataMap = new ArrayList<>();
                Set<PurchaseOrdersSplitData> purchaseOrdersSplitDataSet = result.getPurchaseOrdersSplitData();
                purchaseOrdersDetailSet = purchaseOrdersSplitDataSet.stream().map(data -> {
                    PurchaseOrdersDetail purchaseOrdersDetail = new PurchaseOrdersDetail();
                    BeanUtils.copyProperties(data, purchaseOrdersDetail);
                    purchaseOrdersDetail.setFromSo(data.getSaleOrder());
                    purchaseOrdersDetail.setAsin(data.getaSin());
                    purchaseOrdersDetail.setId(null);
                    purchaseOrdersDetail.setUnitPrice(data.getUnitCost());
                    purchaseOrdersDetail.setTotalVolume(data.getCbm());
                    purchaseOrdersDetail.setNetWeight(data.getNetWeight());
                    purchaseOrdersDetail.setGrossWeight(data.getGrossWeight());
                    purchaseOrdersDetail.setQty(Math.toIntExact(data.getQtyOrdered()));
                    purchaseOrders.setExpectedShipDate(data.getShipDate());
                    purchaseOrders.setFulfillmentCenter(data.getFulfillmentCenter());
                    purchaseOrders.setVendorId(data.getVendor());
                    purchaseOrders.setVendorCode(data.getVendorCode());
                    purchaseOrders.setCountry(data.getCountry());
                    String key = purchaseOrdersDetail.getAsin() + "_" + purchaseOrdersDetail.getFromSo();
                    if (checkDataMap.contains(key)) {
                        throw new BusinessException(String.format("Can not create Purchase Order with duplicate {ASin= %s ,FromSo= %s }", purchaseOrdersDetail.getAsin(), purchaseOrdersDetail.getFromSo()));
                    } else {
                        checkDataMap.add(key);
                    }
                    Optional<PurchaseOrdersDetail> purchaseOrdersDetailExists = purchaseOrdersDetailRepository.findByFromSoAndAsinAndIsDeleted(purchaseOrdersDetail.getFromSo(), purchaseOrdersDetail.getAsin(), false);
                    if (purchaseOrdersDetailExists.isPresent()) {
                        throw new BusinessException(String.format("{ASin= %s ,FromSo= %s } already exists in another Purchase Order.", purchaseOrdersDetail.getAsin(), purchaseOrdersDetail.getFromSo()));
                    }
                    return purchaseOrdersDetail;
                }).collect(Collectors.toSet());
                purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetailSet);
                List<String> fromSo = purchaseOrders.getPurchaseOrdersDetail().stream().map(PurchaseOrdersDetail::getFromSo).distinct().collect(Collectors.toList());
                String lastestShipWindow = purchaseOrdersCommonService.getLastShipWindow(fromSo);
                if (CommonDataUtil.isEmpty(lastestShipWindow)) {
                    throw new BusinessException("Can not find ShipWindow from list SO in Purchaser Order.");
                } else {
                    purchaseOrders.setShipWindowStart(DateUtils.convertStringLocalDateBooking((lastestShipWindow)));
                }
                Set<PurchaseOrdersDate> purchaseOrdersDateSet = new HashSet<>();
                if (CommonDataUtil.isNotNull(purchaseOrders.getExpectedShipDate())) {
                    PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                    purchaseOrdersDate.setDateBefore(null);
                    purchaseOrdersDate.setDateAfter(purchaseOrders.getExpectedShipDate());
                    purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_DATE);
                    purchaseOrdersDate.setCreatedBy(userId);
                    purchaseOrdersDate.setUpdatedBy(userId);
                    purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                    purchaseOrdersDateSet.add(purchaseOrdersDate);
                }
                if (CommonDataUtil.isNotNull(purchaseOrders.getShipWindowStart())) {
                    PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                    purchaseOrdersDate.setDateBefore(null);
                    purchaseOrdersDate.setDateAfter(purchaseOrders.getShipWindowStart());
                    purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_WINDOW);
                    purchaseOrdersDate.setCreatedBy(userId);
                    purchaseOrdersDate.setUpdatedBy(userId);
                    purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                    purchaseOrdersDateSet.add(purchaseOrdersDate);
                }
                purchaseOrders.setPurchaseOrdersDate(purchaseOrdersDateSet);
                purchaseOrders.setTotalItem(purchaseOrdersDetailSet.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum));
                purchaseOrders.setTotalCost(purchaseOrdersDetailSet.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                listPO.add(purchaseOrders.getId());
            });
            return listPO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    private void writeHeaderLine(XSSFWorkbook workbook, XSSFSheet sheet) {

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
        createCell(sheet, row, i++, "PO number", style);
        createCell(sheet, row, i++, "SKU", style);
        createCell(sheet, row, i++, "ASIN", style);
        createCell(sheet, row, i++, "Product Name", style);
        createCell(sheet, row, i++, "Quantity Ordered", style);
        createCell(sheet, row, i++, "Ship date", style);
        createCell(sheet, row, i++, "Fulfillment center", style);
        createCell(sheet, row, i++, "Vendor", style);
        createCell(sheet, row, i++, "MTS", style);
        createCell(sheet, row, i++, "Unit Price", style);
        createCell(sheet, row, i++, "Amount", style);
        createCell(sheet, row, i++, "CBM", style);
        createCell(sheet, row, i++, "Net Weight", style);
        createCell(sheet, row, i++, "Gross Weight", style);
        createCell(sheet, row, i++, "Total Box", style);
        createCell(sheet, row, i++, "PCS/CTN", style);
        createCell(sheet, row, i, "Vendor Code", style);
    }

    private void createCell(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else {
            cell.setCellValue((Boolean) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) {
        Optional<PurchaseOrdersSplitResult> oPurchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(id);
        if (oPurchaseOrdersSplitResult.isEmpty()) {
            throw new BusinessException("Split PO result not found.");
        }
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = oPurchaseOrdersSplitResult.get();
        List<PurchaseOrdersSplitData> purchaseOrdersSplitDataList = purchaseOrdersSplitDataRepository.findAllByPurchaseOrdersSplitResult(purchaseOrdersSplitResult);
        XSSFSheet sheet = workbook.createSheet(purchaseOrdersSplitResult.getOrderNo());
        int rowCount = 1;
        writeHeaderLine(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        for (PurchaseOrdersSplitData data : purchaseOrdersSplitDataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, data.getSaleOrder(), style);
            createCell(sheet, row, columnCount++, data.getSku(), style);
            createCell(sheet, row, columnCount++, data.getaSin(), style);
            createCell(sheet, row, columnCount++, data.getProductName(), style);
            createCell(sheet, row, columnCount++, data.getQtyOrdered(), style);
            createCell(sheet, row, columnCount++, data.getShipDate(), styleDate);
            createCell(sheet, row, columnCount++, data.getFulfillmentCenter(), style);
            createCell(sheet, row, columnCount++, data.getVendor(), style);
            createCell(sheet, row, columnCount++, data.getMakeToStock(), style);
            createCell(sheet, row, columnCount++, data.getUnitCost(), style);
            createCell(sheet, row, columnCount++, data.getAmount(), style);
            createCell(sheet, row, columnCount++, data.getCbm(), style);
            createCell(sheet, row, columnCount++, data.getNetWeight(), style);
            createCell(sheet, row, columnCount++, data.getGrossWeight(), style);
            createCell(sheet, row, columnCount++, data.getTotalBox(), style);
            createCell(sheet, row, columnCount++, data.getPcs(), style);
            createCell(sheet, row, columnCount, data.getVendorCode(), style);
        }
        return workbook;
    }

    @Override
    public void export(String filename, Integer id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFile(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }
}
