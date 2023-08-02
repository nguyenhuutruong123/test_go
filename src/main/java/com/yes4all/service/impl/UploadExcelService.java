package com.yes4all.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.common.enums.EnumNoteExcel;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.*;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.SendMailService;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.constants.GlobalConstant.*;

import static com.yes4all.constants.GlobalConstant.CONTAINER_PALLET_SHEET;



@Service
public class UploadExcelService {
    private static final String TITLE_HASHMAP = "detail_";

    private static final Logger logger = LoggerFactory.getLogger(UploadExcelService.class);

    private static final String KEY_UPLOAD = "@#$!@";
    @Autowired
    private ShipmentPackingListRepository shipmentPackingListRepository;

    @Autowired
    private PurchaseOrdersWHRepository purchaseOrdersWHRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private ProformaInvoiceWHRepository proformaInvoiceWHRepository;
    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;

    @Autowired
    private ProformaInvoiceWHDetailRepository proformaInvoiceWHDetailRepository;
    @Autowired
    private PurchaseOrdersSplitRepository purchaseOrdersSplitRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VendorCountryRepository vendorCountryRepository;

    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private PurchaseOrdersCommonService purchaseOrdersCommonService;

    @Autowired
    private CommercialInvoiceWHRepository commercialInvoiceWHRepository;

    private static final String COLUMN_SO = "PO_NUMBER";
    private static final String COLUMN_SKU = "SKU";
    private static final String COLUMN_ASIN = "ASIN";

    private static final String COLUMN_PRODUCT_NAME = "PRODUCT_NAME";
    private static final String COLUMN_QUANTITY_ORDERED = "QUANTITY_ORDERED";
    private static final String COLUMN_SHIPDATE = "SHIPDATE";
    private static final String COLUMN_FULFILLMENT_CENTER = "FULFILLMENT_CENTER";
    private static final String COLUMN_VENDOR = "VENDOR";
    private static final String COLUMN_MTS = "MTS";
    private static final String COLUMN_UNIT_PRICE = "COLUMN_UNIT_PRICE";
    private static final String COLUMN_AMOUNT = "COLUMN_AMOUNT";

    private static final String COLUMN_PALLET_QUANTITY = "COLUMN_PALLET_QUANTITY";

    private static final String COLUMN_CONTAINER_NO = "COLUMN_CONTAINER_NO";

    private static final String COLUMN_CONTAINER_TYPE = "COLUMN_CONTAINER_TYPE";

    private static final String COLUMN_NOTE = "COLUMN_NOTE";
    private static final String COLUMN_ETD = "COLUMN_ETD";
    private static final String COLUMN_PL_ORDER = "COLUMN_PL_ORDER";

    private static final String COLUMN_POL = "COLUMN_POL";
    private static final String COLUMN_PCS = "COLUMN_PCS";
    private static final String COLUMN_VENDOR_CODE = "COLUMN_VENDOR_CODE";
    private static final String COLUMN_DEMAND = "COLUMN_DEMAND";

    private static final String COLUMN_TOTAL_BOX = "COLUMN_TOTAL_BOX";
    private static final String COLUMN_PROFORMA_INVOICE = "COLUMN_TOTAL_PROFORMA_INVOICE";
    private static final String COLUMN_PO_NUMBER = "COLUMN_TOTAL_PO_NUMBER";
    private static final String COLUMN_QTY_OF_EACH_CARTON = "COLUMN_TOTAL_QTY_OF_EACH_CARTON";
    private static final String COLUMN_TOTAL_CARTON = "COLUMN_TOTAL_CARTON";
    private static final String COLUMN_NET_WEIGHT = "COLUMN_TOTAL_NET_WEIGHT";
    private static final String COLUMN_GROSS_WEIGHT = "COLUMN_TOTAL_GROSS_WEIGHT";
    @Autowired
    SupplierCountryRepository supplierCountryRepository;
    private static final String COLUMN_CBM = "COLUMN_TOTAL_CBM";
    private static final String COLUMN_CONTAINER = "COLUMN_CONTAINER";
    private static final String COLUMN_NOTE_ADJUST = "COLUMN_NOTE_ADJUST";

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String COLUMN_PKL_CONTAINER = "PKL_CONTAINER";

    private static final String COLUMN_PKL_PALLET = "PKL_PALLET";
    @Value("${attribute.host.url}")
    private String linkPOMS;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToPO(MultipartFile file, String userId) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> purchaseOrdersInfo = mappingTableSheet(wb);
                if (purchaseOrdersInfo.isEmpty()) {
                    return null;
                }
                // count row i errors
                final int[] i = {-1};
                List<UploadPurchaseOrder> listUploadPurchaseOrder = new ArrayList<>();
                purchaseOrdersInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<String, Integer> resultUploadDetail = new HashMap<>();
                        // get list Po Number
                        List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                        PurchaseOrders purchaseOrders = new PurchaseOrders();
                        purchaseOrders.setIsDeleted(false);
                        purchaseOrders.setCreatedBy(userId);
                        purchaseOrders.setCreatedDate(new Date().toInstant());
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEW);
                        purchaseOrders.setIsSendmail(false);
                        //0 is Direct Import
                        purchaseOrders.setChannel(0);
                        final String[] country = {""};
                        final String[] vendorCode = {""};
                        Set<PurchaseOrdersDetail> sPurchaseOrdersDetail = new HashSet<>();
                        List<String> listSkuFromSo = new ArrayList<>();
                        List<String> listDuplicateASinSo = new ArrayList<>();
                        List<String> listExistsASinSo = new ArrayList<>();
                        final boolean[] flagError = {false};
                        listPO.stream().forEach(item -> {
                            PurchaseOrdersDetail purchaseOrdersDetail = new PurchaseOrdersDetail();
                            String strShipDate = item.get(COLUMN_SHIPDATE).toString();
                            String strFulfillmentCenter = item.get(COLUMN_FULFILLMENT_CENTER).toString();
                            String strVendor = item.get(COLUMN_VENDOR).toString();
                            String strSO = item.get(COLUMN_SO).toString();
                            String strSku = item.get(COLUMN_SKU).toString();
                            String strASin = item.get(COLUMN_ASIN).toString();
                            String strProductName = item.get(COLUMN_PRODUCT_NAME).toString();
                            Double dblQuantityOrdered = "".equals(item.get(COLUMN_QUANTITY_ORDERED)) ? 0 : Double.parseDouble(item.get(COLUMN_QUANTITY_ORDERED).toString());
                            String dblMTS = item.get(COLUMN_MTS).toString();
                            Double dblUnitPrice = "".equals(item.get(COLUMN_UNIT_PRICE)) ? 0 : Double.parseDouble(item.get(COLUMN_UNIT_PRICE).toString());
                            Double dblAmount = "".equals(item.get(COLUMN_AMOUNT)) ? 0 : Double.parseDouble(item.get(COLUMN_AMOUNT).toString());
                            dblAmount = Math.round(dblAmount * 100.0) / 100.0;
                            Double dblCBM = "".equals(item.get(COLUMN_CBM)) ? 0 : Double.parseDouble(item.get(COLUMN_CBM).toString());
                            Double dblNetWeight = "".equals(item.get(COLUMN_NET_WEIGHT)) ? 0 : Double.parseDouble(item.get(COLUMN_NET_WEIGHT).toString());
                            Double dblGrossWeight = "".equals(item.get(COLUMN_GROSS_WEIGHT)) ? 0 : Double.parseDouble(item.get(COLUMN_GROSS_WEIGHT).toString());
                            Double dblTotalBox = "".equals(item.get(COLUMN_TOTAL_BOX)) ? 0 : Double.parseDouble(item.get(COLUMN_TOTAL_BOX).toString());
                            Integer intPCS = "".equals(item.get(COLUMN_PCS)) ? 0 : Integer.parseInt(item.get(COLUMN_PCS).toString());
                            String strVendorCode = item.get(COLUMN_VENDOR_CODE).toString();
                            String strDemand = item.get(COLUMN_DEMAND).toString();
                            vendorCode[0] = strVendorCode;

                            Optional<VendorCountry> oVendorCountry = vendorCountryRepository.findByVendorCode(strVendorCode);
                            if (oVendorCountry.isPresent()) {
                                country[0] = oVendorCountry.get().getCountry();
                            } else {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Can not find country by vendor code " + strVendorCode, null));
                            }
                            if (strVendor.trim().length() == 0 || strShipDate.trim().length() == 0 || strFulfillmentCenter.trim().length() == 0) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Vendor or Fulfillment center or Ship date  cannot empty.", null));
                            }
                            boolean isDateValid = CommonDataUtil.isDateValid(strShipDate);
                            if (!isDateValid) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Wrong format ship date must dd/MM/yyyy", null));
                            } else {
                                purchaseOrders.setExpectedShipDate(DateUtils.convertStringLocalDate(strShipDate));
                            }
                            purchaseOrders.setFulfillmentCenter(strFulfillmentCenter);
                            purchaseOrders.setVendorId(strVendor);
                            purchaseOrders.setDemand(strDemand);
                            dblUnitPrice = Math.round(dblUnitPrice * 100.0) / 100.0;
                            purchaseOrdersDetail.setUnitPrice(dblUnitPrice);
                            purchaseOrdersDetail.setUnitPricePrevious(dblUnitPrice);
                            purchaseOrdersDetail.setAmount(dblAmount);
                            purchaseOrdersDetail.setAmountPrevious(dblAmount);
                            dblCBM = Math.round(dblCBM * 1000.0) / 1000.0;
                            purchaseOrdersDetail.setTotalVolume(dblCBM);
                            purchaseOrdersDetail.setTotalVolumePrevious(dblCBM);
                            dblNetWeight = Math.round(dblNetWeight * 1000.0) / 1000.0;
                            purchaseOrdersDetail.setNetWeight(dblNetWeight);
                            purchaseOrdersDetail.setNetWeightPrevious(dblNetWeight);
                            dblGrossWeight = Math.round(dblGrossWeight * 1000.0) / 1000.0;
                            purchaseOrdersDetail.setGrossWeight(dblGrossWeight);
                            purchaseOrdersDetail.setGrossWeightPrevious(dblGrossWeight);
                            purchaseOrdersDetail.setTotalBox(dblTotalBox);
                            purchaseOrdersDetail.setTotalBoxPrevious(dblTotalBox);
                            purchaseOrdersDetail.setPcs(intPCS);
                            purchaseOrdersDetail.setPcsPrevious(intPCS);
                            purchaseOrdersDetail.setFromSo(strSO);
                            if (isDateValid) {
                                purchaseOrdersDetail.setShipDate(DateUtils.convertStringLocalDate(strShipDate));
                            }
                            purchaseOrdersDetail.setSku(strSku);
                            purchaseOrdersDetail.setAsin(strASin);
                            purchaseOrdersDetail.setQtyUsed(0L);
                            purchaseOrdersDetail.setProductName(strProductName);
                            int qtyOrderedValue = dblQuantityOrdered.intValue();
                            purchaseOrdersDetail.setQty(qtyOrderedValue);
                            purchaseOrdersDetail.setQtyPrevious((long) qtyOrderedValue);
                            purchaseOrdersDetail.setMakeToStock(dblMTS);
                            purchaseOrdersDetail.setMakeToStockPrevious(dblMTS);
                            // Can not duplicate ASin-SO in 1 PO detail
                            String keyASinSo = strASin + "/" + strSO;
                            if (listSkuFromSo.contains(keyASinSo)) {
                                // Save list duplicate in PO with key ASin-SO
                                listDuplicateASinSo.add(keyASinSo);
                            }
                            listSkuFromSo.add(keyASinSo);
                            // Can not duplicate ASin-SO in ALL PI DETAIL
                            Optional<PurchaseOrdersDetail> purchaseOrdersDetailExists = purchaseOrdersDetailRepository.findByFromSoAndAsinAndIsDeleted(strSO, strASin, false);
                            if (purchaseOrdersDetailExists.isPresent()) {
                                // Save list duplicate in ALL PI with key ASin-SO
                                listExistsASinSo.add(keyASinSo);
                            }
                            sPurchaseOrdersDetail.add(purchaseOrdersDetail);
                            if (CommonDataUtil.isNotNull(resultUploadDetail.get(strSO))) {
                                resultUploadDetail.put(strSO, resultUploadDetail.get(strSO) + 1);
                            } else {
                                resultUploadDetail.put(strSO, 1);
                            }
                        });
                        String namePO = "";
                        if (listDuplicateASinSo.isEmpty() && listExistsASinSo.isEmpty() && !flagError[0]) {
                            namePO = purchaseOrdersCommonService.generateOrderNo(country[0], purchaseOrders.getVendorId(), false);
                            purchaseOrders.setCountry(country[0]);
                            purchaseOrders.setVendorCode(vendorCode[0]);
                            purchaseOrders.setPoNumber(namePO);
                            purchaseOrders.setPurchaseOrdersDetail(sPurchaseOrdersDetail);
                            List<String> fromSo = purchaseOrders.getPurchaseOrdersDetail().stream().map(item -> item.getFromSo()).distinct().collect(Collectors.toList());
                            String lastShipWindow = purchaseOrdersCommonService.getLastShipWindow(fromSo);
                            if (CommonDataUtil.isEmpty(lastShipWindow)) {
                                throw new BusinessException("Can not find ShipWindow from list SO in Purchaser Order.");
                            } else {
                                purchaseOrders.setShipWindowStart(DateUtils.convertStringLocalDateBooking((lastShipWindow)));
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
                            purchaseOrders.setTotalItem(sPurchaseOrdersDetail.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum));
                            purchaseOrders.setTotalCost(sPurchaseOrdersDetail.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
//                                List<String> listEmail = new ArrayList<>();
//                                Optional<User> userEmails = userRepository.findOneByVendor(purchaseOrders.getVendorId());
//                                if(userEmails.isPresent()){
//                                    User user=userEmails.get();
//                                    listEmail.add(user.getEmail());
//                                    if(user.getEmail().length()>0) {
//                                        String supplier = (user.getLastName() == null ? "" : user.getLastName() + " ") + user.getFirstName();
//                                        String content = CommonDataUtil.contentMail(linkPOMS+LINK_DETAIL_PO+purchaseOrders.getId()+"?size=20&page=0", purchaseOrders.getPoNumber(),supplier,"","","NEW");
//                                        sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - A new Purchase Order from Yes4All", content, listEmail);
//                                    }
//                                }
                            listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "successes", "PO Number created ", resultUploadDetail));
                        } else {
                            if (!listDuplicateASinSo.isEmpty()) {
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", String.format("SO and ASin duplicate in PO list={ %s }", listDuplicateASinSo.stream().collect(Collectors.joining(", "))), null));
                            }
                            if (!listExistsASinSo.isEmpty()) {
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", String.format("{ASin & FromSo : %s } already exists in Purchase Order Another.", listExistsASinSo.stream().collect(Collectors.joining(", "))), null));
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });

                resultUploadDTO.setUploadPurchaseOrder(listUploadPurchaseOrder);
                logger.info("END ==========");
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToPOWH(MultipartFile file, String userId) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> purchaseOrdersInfo = mappingTableSheetPOWH(wb);
                List<UploadPurchaseOrder> listUploadPurchaseOrder = new ArrayList<>();
                if (purchaseOrdersInfo.isEmpty()) {
                    listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Data can not empty", null));
                }
                // count row i errors
                final int[] i = {-1};
                purchaseOrdersInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<String, Integer> resultUploadDetail = new HashMap<>();
                        // get list Po Number
                        List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                        PurchaseOrdersWH purchaseOrders = new PurchaseOrdersWH();
                        purchaseOrders.setCreatedBy(userId);
                        purchaseOrders.setCreatedDate(new Date().toInstant());
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEW);
                        purchaseOrders.setIsSendmail(false);
                        //1 ware house
                        purchaseOrders.setChannel(1);
                        //default USA
                        final String[] country = {"USA"};
                        Set<PurchaseOrdersWHDetail> sPurchaseOrdersDetail = new HashSet<>();
                        List<String> listSku = new ArrayList<>();
                        List<String> listSkuDuplicate = new ArrayList<>();
                        final boolean[] flagError = {false};
                        for (Map<String, Object> item : listPO) {
                            PurchaseOrdersWHDetail purchaseOrdersDetail = new PurchaseOrdersWHDetail();
                            String strShipDate = item.get(COLUMN_SHIPDATE).toString();
                            String strETD = item.get(COLUMN_ETD).toString();
                            String strVendor = item.get(COLUMN_VENDOR).toString();
                            List<SupplierCountry> supplierCountries = supplierCountryRepository.findBySupplier(strVendor);
                            if (supplierCountries.isEmpty()) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Supplier not exists", null));
                            }
                            String strSku = item.get(COLUMN_SKU).toString();
                            String strASin = item.get(COLUMN_ASIN).toString();
                            String strProductName = item.get(COLUMN_PRODUCT_NAME).toString();
                            Double dblQuantityOrdered = "".equals(item.get(COLUMN_QUANTITY_ORDERED)) ? 0 : Double.parseDouble(item.get(COLUMN_QUANTITY_ORDERED).toString());
                            String dblMTS = item.get(COLUMN_MTS).toString();
                            Double dblUnitPrice = "".equals(item.get(COLUMN_UNIT_PRICE)) ? 0 : Double.parseDouble(item.get(COLUMN_UNIT_PRICE).toString());
                            Double dblAmount = "".equals(item.get(COLUMN_AMOUNT)) ? 0 : Double.parseDouble(item.get(COLUMN_AMOUNT).toString());
                            dblAmount = Math.round(dblAmount * 100.0) / 100.0;
                            Double dblCBM = "".equals(item.get(COLUMN_CBM)) ? 0 : Double.parseDouble(item.get(COLUMN_CBM).toString());
                            Double dblTotalBox = "".equals(item.get(COLUMN_TOTAL_BOX)) ? 0 : Double.parseDouble(item.get(COLUMN_TOTAL_BOX).toString());

                            Double dblGrossWeight = "".equals(item.get(COLUMN_GROSS_WEIGHT)) ? 0 : Double.parseDouble(item.get(COLUMN_GROSS_WEIGHT).toString());
                            Double dblNetWeight = "".equals(item.get(COLUMN_NET_WEIGHT)) ? 0 : Double.parseDouble(item.get(COLUMN_NET_WEIGHT).toString());
                            Double intPCS = "".equals(item.get(COLUMN_PCS)) ? 0 : Double.parseDouble(item.get(COLUMN_PCS).toString());
                            String strPLOrder = item.get(COLUMN_PL_ORDER).toString();
                            String strPOL = item.get(COLUMN_POL).toString();
                            String strContainerNo = item.get(COLUMN_CONTAINER_NO).toString();
                            String strContainerType = item.get(COLUMN_CONTAINER_TYPE).toString();
                            String strNote = item.get(COLUMN_NOTE).toString();
                            String strPalletQuantity = item.get(COLUMN_PALLET_QUANTITY).toString();
                            if (strSku.trim().length() == 0 || strProductName.trim().length() == 0 || item.get(COLUMN_QUANTITY_ORDERED).toString().trim().length() == 0
                                 || item.get(COLUMN_UNIT_PRICE).toString().trim().length() == 0|| item.get(COLUMN_PL_ORDER).toString().trim().length() == 0
                                || item.get(COLUMN_AMOUNT).toString().trim().length() == 0 || item.get(COLUMN_CBM).toString().trim().length() == 0
                                || item.get(COLUMN_GROSS_WEIGHT).toString().trim().length() == 0  || item.get(COLUMN_NET_WEIGHT).toString().trim().length() == 0
                                || item.get(COLUMN_TOTAL_BOX).toString().trim().length() == 0  || item.get(COLUMN_PCS).toString().trim().length() == 0
                                || item.get(COLUMN_CONTAINER_NO).toString().trim().length() == 0 || item.get(COLUMN_CONTAINER_TYPE).toString().trim().length() == 0) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Data can not empty", null));
                                break;
                            }

                            if (!strContainerType.equals(CONTAINER_TYPE_40) && !strContainerType.equals(CONTAINER_TYPE_20) && !strContainerType.equals(CONTAINER_TYPE_45)) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Wrong format value ContainerType", null));
                                break;
                            }
                            boolean isDateValidShipDate = CommonDataUtil.isDateValidWH(strShipDate);
                            if (!isDateValidShipDate) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Wrong format ship date must dd/mm/yyyy", null));
                                break;
                            } else {
                                purchaseOrders.setExpectedShipDate(DateUtils.convertStringLocalDateWH(strShipDate));
                            }
                            boolean isDateValidETD = CommonDataUtil.isDateValidWH(strETD);
                            if (!isDateValidETD) {
                                flagError[0] = true;
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder("", "errors", "Wrong format ETD must dd/mm/yyyy", null));
                                break;
                            } else {
                                purchaseOrders.setEtd(DateUtils.convertStringLocalDateWH(strETD));
                                purchaseOrders.setEtdOriginal(DateUtils.convertStringLocalDateWH(strETD));
                            }
                            purchaseOrders.setVendorId(strVendor);
                            purchaseOrders.setPlOrder(strPLOrder);
                            purchaseOrders.setPortOfLoading(strPOL);
                            purchaseOrders.setCountry(country[0]);
                            purchaseOrdersDetail.setContainerNo(strContainerNo);
                            purchaseOrdersDetail.setPalletQuantity(Integer.valueOf(strPalletQuantity));
                            purchaseOrdersDetail.setContainerType(strContainerType);

                            purchaseOrdersDetail.setTotalBox(dblTotalBox);

                            purchaseOrdersDetail.setNote(strNote);
                            dblUnitPrice = Math.round(dblUnitPrice * 100.0) / 100.0;
                            purchaseOrdersDetail.setUnitPrice(dblUnitPrice);
                            purchaseOrdersDetail.setAmount(dblAmount);
                            dblCBM = Math.round(dblCBM * 1000.0) / 1000.0;
                            purchaseOrdersDetail.setTotalVolume(dblCBM);
                            dblGrossWeight = Math.round(dblGrossWeight * 1000.0) / 1000.0;
                            dblNetWeight = Math.round(dblNetWeight * 1000.0) / 1000.0;
                            purchaseOrdersDetail.setGrossWeight(dblGrossWeight);
                            purchaseOrdersDetail.setNetWeight(dblNetWeight);
                            purchaseOrdersDetail.setPcs((int) Math.round(intPCS));
                            purchaseOrdersDetail.setSku(strSku);
                            purchaseOrdersDetail.setAsin(strASin);
                            purchaseOrdersDetail.setProductName(strProductName);
                            int qtyOrderedValue = dblQuantityOrdered.intValue();
                            purchaseOrdersDetail.setQty(qtyOrderedValue);
                            purchaseOrdersDetail.setMakeToStock(dblMTS);
                            // Can not duplicate SKU in 1 PO detail
                            if (listSku.contains(strSku)) {
                                // Save list duplicate in PO with key SKU
                                listSkuDuplicate.add(strSku);
                            } else {
                                listSku.add(strSku);
                            }
                            sPurchaseOrdersDetail.add(purchaseOrdersDetail);
                        }
                        ;
                        String namePO = "";
                        if (listSkuDuplicate.isEmpty() && !flagError[0]) {
                            namePO = purchaseOrdersCommonService.generateOrderNo(country[0], purchaseOrders.getVendorId(), true);
                            purchaseOrders.setCountry(country[0]);
                            purchaseOrders.setPoNumber(namePO);
                            purchaseOrders.setPurchaseOrdersWHDetail(sPurchaseOrdersDetail);
                            Set<PurchaseOrdersWHDate> purchaseOrdersDateSet = new HashSet<>();
                            if (CommonDataUtil.isNotNull(purchaseOrders.getExpectedShipDate())) {
                                PurchaseOrdersWHDate purchaseOrdersDate = new PurchaseOrdersWHDate();
                                purchaseOrdersDate.setDateBefore(null);
                                purchaseOrdersDate.setDateAfter(purchaseOrders.getExpectedShipDate());
                                purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_DATE);
                                purchaseOrdersDate.setCreatedBy(userId);
                                purchaseOrdersDate.setUpdatedBy(userId);
                                purchaseOrdersDate.setPurchaseOrdersWH(purchaseOrders);
                                purchaseOrdersDateSet.add(purchaseOrdersDate);
                            }
                            purchaseOrders.setPurchaseOrdersWHDate(purchaseOrdersDateSet);
                            purchaseOrders.setTotalItem(sPurchaseOrdersDetail.stream().map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum));
                            purchaseOrders.setTotalAmount(sPurchaseOrdersDetail.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                            purchaseOrders.setTotalCbm(sPurchaseOrdersDetail.stream().map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
                            purchaseOrders.setTotalGrossWeight(sPurchaseOrdersDetail.stream().map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
                            purchaseOrders.setNumberContainer((int) sPurchaseOrdersDetail.stream().map(PurchaseOrdersWHDetail::getContainerNo).distinct().count());
                            purchaseOrdersWHRepository.saveAndFlush(purchaseOrders);
                            listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "successes", "PO Number created ", resultUploadDetail));
                        } else {
                            if (!listSkuDuplicate.isEmpty()) {
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", String.format("Sku duplicate in PO list={ %s }", listSku.stream().collect(Collectors.joining(", "))), null));
                            }

                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadPurchaseOrder(listUploadPurchaseOrder);
                logger.info("END ==========");
                return resultUploadDTO;
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPO(MultipartFile file, Integer id) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        PurchaseOrderDTO purchaseOrderDTO = new PurchaseOrderDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> purchaseOrdersInfo = mappingTableSheetDetailPO(wb);
                // count row i errors
                final int[] i = {-1};
                List<UploadResultDetail> listUploadPurchaseOrderDetail = new ArrayList<>();
                purchaseOrdersInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<Integer, Boolean> resultUploadDetail = new HashMap<>();
                        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
                        if (oPurchaseOrder.isPresent()) {
                            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                            CommonDataUtil.getModelMapper().map(purchaseOrders, purchaseOrderDTO);
                            //get details previous
                            Set<PurchaseOrderDetailDTO> purchaseOrdersDetailsPrevious = purchaseOrderDTO.getPurchaseOrdersDetail();
                            //details new
                            Set<PurchaseOrderDetailDTO> purchaseOrdersDetailsNew = new HashSet<>();
                            // get list Po Number
                            List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                            final int[] index = {1};
                            listPO.stream().forEach(item -> {

                                Object oSO = item.get(COLUMN_SO);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oAmount = item.get(COLUMN_AMOUNT);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oPCS = item.get(COLUMN_PCS);
                                Object oMts = item.get(COLUMN_MTS);
                                double qtyOrdered = oQuantityOrdered.equals("") ? 0 : Double.parseDouble(oQuantityOrdered.toString());
                                double unitPrice = oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                                double amount = oAmount.equals("") ? 0 : Double.parseDouble(oAmount.toString());
                                double amountCalculate = unitPrice * qtyOrdered;
                                double totalVolume = oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString());
                                double totalGrossWeight = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                double totalNetWeight = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                //Data upload must equal formula amount
                                if (amountCalculate != amount) {
                                    listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Check Amount = Quantity Ordered * Unit Price", ""));
                                }
                                int pcs = oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString());
                                double totalBox = oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString());
                                double totalBoxCalculate = Math.round(qtyOrdered / pcs);
                                //Data upload must equal formula totalBox
                                if (totalBoxCalculate != totalBox) {
                                    listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Check Total box = Quantity Ordered / (PCS/Carton)", ""));
                                }
                                Optional<PurchaseOrderDetailDTO> purchaseOrdersDetail = purchaseOrdersDetailsPrevious.stream().filter(k -> k.getFromSo().equals(oSO.toString()) && k.getAsin().equals(oASin.toString()) && !k.isDeleted()).findFirst();
                                if (purchaseOrdersDetail.isPresent()) {
                                    PurchaseOrderDetailDTO purchaseOrderDetailDTOOld = purchaseOrdersDetail.get();
                                    if (!oSku.toString().equals(purchaseOrderDetailDTOOld.getSku())) {
                                        Optional<Product> oProduct = productRepository.findBySku(oSku.toString());
                                        if (oProduct.isPresent()) {
                                            purchaseOrderDetailDTOOld.setSku(oSku.toString());
                                        } else {
                                            listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Not exist", ""));
                                        }
                                    }
                                    //if quantity change then Total Volume, Total Net Weight, Total Gross Weight must change
                                    if (qtyOrdered != purchaseOrderDetailDTOOld.getQty() && (purchaseOrderDetailDTOOld.getTotalVolume() != totalVolume || purchaseOrderDetailDTOOld.getGrossWeight() != totalGrossWeight
                                        || purchaseOrderDetailDTOOld.getNetWeight() != totalNetWeight)) {
                                        listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Quantity changed [Total Volume], [Total Net Weight], [Total Gross Weight] not changed", ""));

                                    }
                                    purchaseOrderDetailDTOOld.setQtyPrevious(purchaseOrderDetailDTOOld.getQty());
                                    purchaseOrderDetailDTOOld.setQty(oQuantityOrdered.equals("") ? 0 : Integer.parseInt(oQuantityOrdered.toString()));
                                    purchaseOrderDetailDTOOld.setUnitPricePrevious(purchaseOrderDetailDTOOld.getUnitPricePrevious());
                                    purchaseOrderDetailDTOOld.setUnitPrice(unitPrice);
                                    purchaseOrderDetailDTOOld.setAmountPrevious(purchaseOrderDetailDTOOld.getAmount());
                                    purchaseOrderDetailDTOOld.setAmount(amount);
                                    purchaseOrderDetailDTOOld.setPcsPrevious(purchaseOrderDetailDTOOld.getPcs());
                                    purchaseOrderDetailDTOOld.setPcs(pcs);
                                    purchaseOrderDetailDTOOld.setMakeToStock(oMts.toString());
                                    purchaseOrderDetailDTOOld.setMakeToStockPrevious(oMts.toString());
                                    purchaseOrderDetailDTOOld.setNetWeightPrevious(purchaseOrderDetailDTOOld.getNetWeight());
                                    purchaseOrderDetailDTOOld.setNetWeight(totalNetWeight);
                                    purchaseOrderDetailDTOOld.setTotalBoxPrevious(purchaseOrderDetailDTOOld.getTotalBox());
                                    purchaseOrderDetailDTOOld.setTotalBox(totalBox);
                                    purchaseOrderDetailDTOOld.setTotalVolumePrevious(purchaseOrderDetailDTOOld.getTotalVolume());
                                    purchaseOrderDetailDTOOld.setTotalVolume(totalVolume);
                                    purchaseOrderDetailDTOOld.setGrossWeightPrevious(purchaseOrderDetailDTOOld.getGrossWeight());
                                    purchaseOrderDetailDTOOld.setGrossWeight(totalGrossWeight);
                                    purchaseOrdersDetailsNew.add(purchaseOrderDetailDTOOld);
                                    resultUploadDetail.put(purchaseOrderDetailDTOOld.getId(), true);
                                } else {
                                    listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Not exists in detail this Purchaser Order!", ""));
                                }
                                index[0]++;
                            });
                            if (resultUploadDetail.size() > 0) {
                                purchaseOrdersDetailsPrevious.stream().filter(item -> resultUploadDetail.get(item.getId()) == null || !resultUploadDetail.get(item.getId())).forEach(element -> listUploadPurchaseOrderDetail.add(new UploadResultDetail(index[0], element.getFromSo(), element.getAsin(), element.getSku(), "Not found in file Excel!", "")));
                            }
                            if (listUploadPurchaseOrderDetail.isEmpty()) {
                                purchaseOrderDTO.setPurchaseOrdersDetail(purchaseOrdersDetailsNew);
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadResultDetail(listUploadPurchaseOrderDetail);
                logger.info("END ==========");
                if (listUploadPurchaseOrderDetail.isEmpty()) {
                    resultUploadDTO.setPurchaseOrderDTO(purchaseOrderDTO);
                }
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPI(MultipartFile file, Integer id, String userId, Boolean isNewVersion) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        ProformaInvoiceDTO proformaInvoiceDTO = new ProformaInvoiceDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> proformaInvoiceInfo = mappingTableSheetDetailPI(wb);
                // count row i errors
                final int[] i = {-1};
                List<UploadResultDetail> uploadResultDetailsError = new ArrayList<>();
                Map<String, List<ProformaInvoiceDetailDTO>> detailDTO = new HashMap<>();
                //get info user
                Optional<User> oUserAction = userRepository.findOneByLogin(userId);
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
                boolean finalIsSupplier = isSupplier;
                boolean finalIsPU = isPU;
                boolean finalIsSourcing = isSourcing;
                proformaInvoiceInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<Integer, Boolean> resultUploadDetail = new HashMap<>();
                        Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(id);
                        if (oProformaInvoice.isPresent()) {
                            ProformaInvoice proformaInvoice = oProformaInvoice.get();
                            CommonDataUtil.getModelMapper().map(proformaInvoice, proformaInvoiceDTO);
                            // get max cdc version
                            Optional<ProformaInvoiceDetail> oProformaInvoiceDetail = proformaInvoiceDetailRepository.findTop1CdcVersionByProformaInvoiceOrderByCdcVersionDesc(proformaInvoice);
                            if (!oProformaInvoiceDetail.isPresent()) {
                                throw new BusinessException("Can not find new version detail proforma invoice");
                            }
                            //get new cdcVersion detail
                            Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();

                            //add all detail into detail DTO not current version of detail
                            proformaInvoice.getProformaInvoiceDetail().stream().filter(m -> (!m.getCdcVersion().equals(cdcVersionMax)) || isNewVersion).forEach(item -> {
                                ProformaInvoiceDetailDTO proformaInvoiceDetailDTO;
                                proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetailDTO.class);
                                List<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                                if (detailDTO.get(TITLE_HASHMAP + item.getCdcVersion()) != null) {
                                    proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + item.getCdcVersion());
                                }
                                proformaInvoiceDetailDTOSet.add(proformaInvoiceDetailDTO);
                                detailDTO.put(TITLE_HASHMAP + item.getCdcVersion(), proformaInvoiceDetailDTOSet);
                            });

                            //get details previous
                            Set<ProformaInvoiceDetailDTO> proformaInvoiceDetailsPrevious = proformaInvoice.getProformaInvoiceDetail().parallelStream().filter(k -> k.getCdcVersion().equals(cdcVersionMax)).map(detail -> {
                                ProformaInvoiceDetailDTO proformaInvoiceDetailDTO;
                                proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(detail, ProformaInvoiceDetailDTO.class);
                                return proformaInvoiceDetailDTO;
                            }).collect(Collectors.toSet());

                            // get list Po Number
                            List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                            final int[] index = {1};
                            final long[] idNew = {-1};
                            listPO.forEach(item -> {

                                Object oSO = item.get(COLUMN_SO);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oQuantity = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oPCS = item.get(COLUMN_PCS);

                                Object oNoteAdjust = item.get(COLUMN_NOTE_ADJUST);
                                if (!CommonDataUtil.isInteger(oQuantity.toString())) {
                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Type quantity is integer", ""));
                                }
                                if (!CommonDataUtil.isInteger(oPCS.toString())) {
                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Type pcs is integer", ""));
                                }
                                if (uploadResultDetailsError.isEmpty()) {
                                    int qtyUpload = oQuantity.equals("") ? 0 : Integer.parseInt(oQuantity.toString());
                                    int pcs = oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString());

                                    String noteAdjust = EnumNoteExcel.getValueByKey(oNoteAdjust.toString());


                                    Optional<ProformaInvoiceDetailDTO> oProformaInvoiceDetailDTO = proformaInvoiceDetailsPrevious.stream().filter(k -> k.getCdcVersion().equals(cdcVersionMax) && k.getFromSo().equals(oSO.toString()) && k.getAsin().equals(oASin.toString()) && !k.isDeleted()).findFirst();
                                    if (oProformaInvoiceDetailDTO.isPresent()) {
                                        ProformaInvoiceDetailDTO invoiceDetailDTO = oProformaInvoiceDetailDTO.get();
                                        if (!oSku.toString().equals(invoiceDetailDTO.getSku())) {
                                            //check sku must define at PIMS.
                                            Optional<Product> oProduct = productRepository.findBySku(oSku.toString());
                                            if (oProduct.isPresent()) {
                                                invoiceDetailDTO.setSku(oSku.toString());
                                            } else {
                                                uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Not exist", ""));
                                            }
                                        }


                                        List<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                                        double totalVolumePrevious = invoiceDetailDTO.getTotalVolumePrevious();
                                        int qtyPrevious = invoiceDetailDTO.getQtyPrevious();
                                        double unitPricePrevious = invoiceDetailDTO.getUnitPricePrevious();
                                        double grossWeightPrevious = invoiceDetailDTO.getGrossWeightPrevious();
                                        double netWeightPrevious = invoiceDetailDTO.getNetWeightPrevious();
                                        double pcsPrevious = invoiceDetailDTO.getPcsPrevious();
                                        double amountPrevious = invoiceDetailDTO.getAmountPrevious();
                                        double totalBoxPrevious = invoiceDetailDTO.getTotalBoxPrevious();
                                        // new value
                                        double totalBox = oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString());
                                        double totalVolume = oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString());
                                        double totalGrossWeight = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                        double totalNetWeight = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                        double unitPrice = oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                                        unitPrice = Math.round(unitPrice * 100.0) / 100.0;
                                        totalVolume = Math.round(totalVolume * 1000.0) / 1000.0;
                                        totalGrossWeight = Math.round(totalGrossWeight * 1000.0) / 1000.0;
                                        totalNetWeight = Math.round(totalNetWeight * 1000.0) / 1000.0;
                                        //if user sourcing upload not change quantity
                                        if (finalIsSourcing) {
                                            qtyUpload = invoiceDetailDTO.getQty();
                                        }
                                        //if user sourcing upload not change all column exception quantity
                                        if (finalIsPU) {
                                            totalBox = invoiceDetailDTO.getTotalBox();
                                            totalVolume = invoiceDetailDTO.getTotalVolume();
                                            totalGrossWeight = invoiceDetailDTO.getGrossWeight();
                                            totalNetWeight = invoiceDetailDTO.getNetWeight();
                                            unitPrice = invoiceDetailDTO.getUnitPrice();
                                        }
                                        double amount = qtyUpload * unitPrice;
                                        amount = Math.round(amount * 100.0) / 100.0;
                                        if (qtyUpload != invoiceDetailDTO.getQty() || unitPrice != invoiceDetailDTO.getUnitPrice()
                                            || totalBox != invoiceDetailDTO.getTotalBox() || totalVolume != invoiceDetailDTO.getTotalVolume()
                                            || totalNetWeight != invoiceDetailDTO.getNetWeight() || totalGrossWeight != invoiceDetailDTO.getGrossWeight()
                                            || pcs != invoiceDetailDTO.getPcs()) {
                                            if (Boolean.FALSE.equals(finalIsSupplier)) {
                                                if (qtyUpload == 0 && noteAdjust.length() == 0) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Qty = 0,Please enter the reason for cancellation using the corresponding number numbered from 1 to 15.", noteAdjust));
                                                } else if (qtyUpload > 0 && noteAdjust.length() > 0) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Qty > 0,Please enter notes as text.", noteAdjust));
                                                }
                                            } else {
                                                if (noteAdjust.length() > 0 || (noteAdjust.length() == 0 && oNoteAdjust.toString().length() == 0)) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Please enter notes as text.", noteAdjust));
                                                }
                                            }
                                        } else {
                                            if (oNoteAdjust.toString().length() > 0) {
                                                uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "The data of this SKU has not adjusted, please recheck the Note column.", noteAdjust));
                                            }
                                        }
                                        if (noteAdjust.length() == 0) {
                                            noteAdjust = oNoteAdjust.toString();
                                        }


                                        //Compute value
                                        if (pcs == 0) {
                                            uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "PCS/CTN value can not equal 0.", ""));
                                        }


                                        if (isNewVersion) {
                                            invoiceDetailDTO.setId((int) idNew[0]);
                                            invoiceDetailDTO.setCdcVersion(cdcVersionMax + 1);
                                            invoiceDetailDTO.setQtyPrevious(invoiceDetailDTO.getQty());
                                            invoiceDetailDTO.setUnitPricePrevious(invoiceDetailDTO.getUnitPrice());
                                            invoiceDetailDTO.setAmountPrevious(invoiceDetailDTO.getAmount());
                                            invoiceDetailDTO.setPcsPrevious(invoiceDetailDTO.getPcs());
                                            invoiceDetailDTO.setTotalVolumePrevious(invoiceDetailDTO.getTotalVolume());
                                            invoiceDetailDTO.setGrossWeightPrevious(invoiceDetailDTO.getGrossWeight());
                                            invoiceDetailDTO.setNetWeightPrevious(invoiceDetailDTO.getNetWeight());
                                            invoiceDetailDTO.setTotalBoxPrevious(invoiceDetailDTO.getTotalBox());
                                            invoiceDetailDTO.setProformaInvoiceDetailLog(null);
                                            idNew[0]--;
                                        } else {
                                            invoiceDetailDTO.setQtyPrevious(qtyPrevious);
                                            invoiceDetailDTO.setAmountPrevious(amountPrevious);
                                            invoiceDetailDTO.setPcsPrevious((int) pcsPrevious);
                                            invoiceDetailDTO.setTotalVolumePrevious(totalVolumePrevious);
                                            invoiceDetailDTO.setGrossWeightPrevious(grossWeightPrevious);
                                            invoiceDetailDTO.setNetWeightPrevious(netWeightPrevious);
                                            invoiceDetailDTO.setTotalBoxPrevious(totalBoxPrevious);
                                            invoiceDetailDTO.setUnitPricePrevious(unitPricePrevious);
                                            invoiceDetailDTO.setQtyPrevious(qtyPrevious);
                                        }
                                        //SET DATA
                                        if (finalIsSupplier) {
                                            invoiceDetailDTO.setNoteAdjustSupplier(noteAdjust);
                                        }
                                        if (finalIsPU) {
                                            invoiceDetailDTO.setNoteAdjust(noteAdjust);
                                        }
                                        if (finalIsSourcing) {
                                            invoiceDetailDTO.setNoteAdjustSourcing(noteAdjust);
                                        }
                                        invoiceDetailDTO.setQty(qtyUpload);
                                        invoiceDetailDTO.setAmount(amount);
                                        invoiceDetailDTO.setPcs(pcs);
                                        invoiceDetailDTO.setTotalVolume(totalVolume);
                                        invoiceDetailDTO.setGrossWeight(totalGrossWeight);
                                        invoiceDetailDTO.setNetWeight(totalNetWeight);
                                        invoiceDetailDTO.setTotalBox(totalBox);
                                        invoiceDetailDTO.setUnitPrice(unitPrice);
                                        if (invoiceDetailDTO.getQty() > 0) {
                                            Optional<ProformaInvoiceDetail> proformaInvoiceDetailExists = proformaInvoiceDetailRepository.findOneWithASinSoNewVersion(invoiceDetailDTO.getAsin(), invoiceDetailDTO.getFromSo(), proformaInvoice.getId());
                                            if (proformaInvoiceDetailExists.isPresent()) {
                                                // Save list duplicate in ALL PI with key ASin-SO
                                                uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "the already exists in another Proforma Invoice", ""));
                                            }
                                        }
                                        resultUploadDetail.put(invoiceDetailDTO.getId(), true);
                                        if (detailDTO.get(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion()) != null) {
                                            proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion());
                                        }
                                        proformaInvoiceDetailDTOSet.add(invoiceDetailDTO);
                                        detailDTO.put(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion(), proformaInvoiceDetailDTOSet);
                                    } else {
                                        uploadResultDetailsError.add(new UploadResultDetail(index[0], oSO.toString(), oASin.toString(), oSku.toString(), "Not exists in detail this Purchaser Order!", ""));
                                    }
                                }
                                index[0]++;
                            });
                            if (resultUploadDetail.size() > 0) {
                                // check miss data
                                proformaInvoiceDetailsPrevious.stream().filter(item -> resultUploadDetail.get(item.getId()) == null || !resultUploadDetail.get(item.getId())).forEach(element -> uploadResultDetailsError.add(new UploadResultDetail(index[0], element.getFromSo(), element.getAsin(), element.getSku(), "Not found in file Excel!", "")));
                            }
                            if (uploadResultDetailsError.isEmpty()) {
                                proformaInvoiceDTO.setProformaInvoiceDetail(detailDTO);
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadResultDetail(uploadResultDetailsError);
                logger.info("END ==========");
                if (uploadResultDetailsError.isEmpty()) {
                    resultUploadDTO.setProformaInvoiceDTO(proformaInvoiceDTO);
                }
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPIWH(MultipartFile file, Integer id, String userId, Boolean isNewVersion) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        ProformaInvoiceWHDTO proformaInvoiceDTO = new ProformaInvoiceWHDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> proformaInvoiceInfo = mappingTableSheetDetailPIWH(wb);
                // count row i errors
                final int[] i = {-1};
                List<UploadResultDetail> uploadResultDetailsError = new ArrayList<>();
                Map<String, List<ProformaInvoiceWHDetailDTO>> detailDTO = new HashMap<>();
                //get info user
                Optional<User> oUserAction = userRepository.findOneByLogin(userId);
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
                boolean finalIsSupplier = isSupplier;
                boolean finalIsPU = isPU;
                boolean finalIsSourcing = isSourcing;
                proformaInvoiceInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<Integer, Boolean> resultUploadDetail = new HashMap<>();
                        Optional<ProformaInvoiceWH> oProformaInvoice = proformaInvoiceWHRepository.findById(id);
                        if (oProformaInvoice.isPresent()) {
                            ProformaInvoiceWH proformaInvoice = oProformaInvoice.get();
                            CommonDataUtil.getModelMapper().map(proformaInvoice, proformaInvoiceDTO);
                            // get max cdc version
                            Optional<ProformaInvoiceWHDetail> oProformaInvoiceDetail = proformaInvoiceWHDetailRepository.findTop1CdcVersionByProformaInvoiceWHOrderByCdcVersionDesc(proformaInvoice);
                            if (oProformaInvoiceDetail.isEmpty()) {
                                throw new BusinessException("Can not find new version detail proforma invoice");
                            }
                            //get new cdcVersion detail
                            Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();

                            //add all detail into detail DTO not current version of detail
                            proformaInvoice.getProformaInvoiceWHDetail().stream().filter(m -> (!m.getCdcVersion().equals(cdcVersionMax)) || isNewVersion).forEach(item -> {
                                ProformaInvoiceWHDetailDTO proformaInvoiceDetailDTO;
                                proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceWHDetailDTO.class);
                                List<ProformaInvoiceWHDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                                if (detailDTO.get(TITLE_HASHMAP + item.getCdcVersion()) != null) {
                                    proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + item.getCdcVersion());
                                }
                                proformaInvoiceDetailDTOSet.add(proformaInvoiceDetailDTO);
                                detailDTO.put(TITLE_HASHMAP + item.getCdcVersion(), proformaInvoiceDetailDTOSet);
                            });

                            //get details previous
                            Set<ProformaInvoiceWHDetailDTO> proformaInvoiceDetailsPrevious = proformaInvoice.getProformaInvoiceWHDetail().parallelStream().filter(k -> k.getCdcVersion().equals(cdcVersionMax)).map(detail -> {
                                ProformaInvoiceWHDetailDTO proformaInvoiceDetailDTO;
                                proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(detail, ProformaInvoiceWHDetailDTO.class);
                                return proformaInvoiceDetailDTO;
                            }).collect(Collectors.toSet());

                            // get list Po Number
                            List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                            final int[] index = {1};
                            final long[] idNew = {-1};
                            listPO.forEach(item -> {
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oQuantity = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oPCS = item.get(COLUMN_PCS);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oNoteAdjust = item.get(COLUMN_NOTE_ADJUST);
                                if (!CommonDataUtil.isInteger(oQuantity.toString())) {
                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Type quantity is integer", ""));
                                }
                                if (!CommonDataUtil.isInteger(oPCS.toString())) {
                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Type pcs is integer", ""));
                                }
                                if (uploadResultDetailsError.isEmpty()) {
                                    int qtyUpload = oQuantity.equals("") ? 0 : Integer.parseInt(oQuantity.toString());
                                    int pcs = oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString());
                                    double totalBox = oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString());
                                    String noteAdjust = EnumNoteExcel.getValueByKey(oNoteAdjust.toString());


                                    Optional<ProformaInvoiceWHDetailDTO> oProformaInvoiceDetailDTO = proformaInvoiceDetailsPrevious.stream().filter(k -> k.getCdcVersion().equals(cdcVersionMax) && k.getAsin().equals(oASin.toString()) && !k.isDeleted()).findFirst();
                                    if (oProformaInvoiceDetailDTO.isPresent()) {
                                        ProformaInvoiceWHDetailDTO invoiceDetailDTO = oProformaInvoiceDetailDTO.get();
                                        if (!oSku.toString().equals(invoiceDetailDTO.getSku())) {
                                            //check sku must define at PIMS.
                                            Optional<Product> oProduct = productRepository.findBySku(oSku.toString());
                                            if (oProduct.isPresent()) {
                                                invoiceDetailDTO.setSku(oSku.toString());
                                            } else {
                                                uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Not exist", ""));
                                            }
                                        }


                                        List<ProformaInvoiceWHDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                                        double totalVolumePrevious = invoiceDetailDTO.getTotalVolumePrevious();
                                        int qtyPrevious = invoiceDetailDTO.getQtyPrevious();
                                        double unitPricePrevious = invoiceDetailDTO.getUnitPricePrevious();
                                        double grossWeightPrevious = invoiceDetailDTO.getGrossWeightPrevious();
                                        double netWeightPrevious = invoiceDetailDTO.getNetWeightPrevious();
                                        double pcsPrevious = invoiceDetailDTO.getPcsPrevious();
                                        double amountPrevious = invoiceDetailDTO.getAmountPrevious();
                                        double totalBoxPrevious = invoiceDetailDTO.getTotalBoxPrevious();
                                        // new value
                                        double totalVolume = oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString());
                                        double totalGrossWeight = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                        double totalNetWeight = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                        double unitPrice = oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                                        unitPrice = Math.round(unitPrice * 100.0) / 100.0;
                                        totalVolume = Math.round(totalVolume * 1000.0) / 1000.0;
                                        totalGrossWeight = Math.round(totalGrossWeight * 1000.0) / 1000.0;
                                        //if user sourcing upload not change quantity
                                        if (finalIsSourcing) {
                                            qtyUpload = invoiceDetailDTO.getQty();
                                        }
                                        //if user sourcing upload not change all column exception quantity
                                        if (finalIsPU) {
                                            totalVolume = invoiceDetailDTO.getTotalVolume();
                                            totalGrossWeight = invoiceDetailDTO.getGrossWeight();
                                            unitPrice = invoiceDetailDTO.getUnitPrice();
                                        }
                                        double amount = qtyUpload * unitPrice;
                                        amount = Math.round(amount * 1000.0) / 1000.0;
                                        if (qtyUpload != invoiceDetailDTO.getQty() || unitPrice != invoiceDetailDTO.getUnitPrice()
                                            || totalVolume != invoiceDetailDTO.getTotalVolume()
                                            || totalGrossWeight != invoiceDetailDTO.getGrossWeight()
                                            || pcs != invoiceDetailDTO.getPcs()) {
                                            if (Boolean.FALSE.equals(finalIsSupplier)) {
                                                if (qtyUpload == 0 && noteAdjust.length() == 0) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Qty = 0,Please enter the reason for cancellation using the corresponding number numbered from 1 to 15.", noteAdjust));
                                                } else if (qtyUpload > 0 && noteAdjust.length() > 0) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Qty > 0,Please enter notes as text.", noteAdjust));
                                                }
                                            } else {
                                                if (noteAdjust.length() > 0 || (noteAdjust.length() == 0 && oNoteAdjust.toString().length() == 0)) {
                                                    uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Please enter notes as text.", noteAdjust));
                                                }
                                            }
                                        } else {
                                            if (oNoteAdjust.toString().length() > 0) {
                                                uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "The data of this SKU has not adjusted, please recheck the Note column.", noteAdjust));
                                            }
                                        }
                                        if (noteAdjust.length() == 0) {
                                            noteAdjust = oNoteAdjust.toString();
                                        }


                                        //Compute value
                                        if (pcs == 0) {
                                            uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "PCS/CTN value can not equal 0.", ""));
                                        }


                                        if (isNewVersion) {
                                            invoiceDetailDTO.setId((int) idNew[0]);
                                            invoiceDetailDTO.setCdcVersion(cdcVersionMax + 1);
                                            invoiceDetailDTO.setQtyPrevious(invoiceDetailDTO.getQty());
                                            invoiceDetailDTO.setUnitPricePrevious(invoiceDetailDTO.getUnitPrice());
                                            invoiceDetailDTO.setAmountPrevious(invoiceDetailDTO.getAmount());
                                            invoiceDetailDTO.setPcsPrevious(invoiceDetailDTO.getPcs());
                                            invoiceDetailDTO.setTotalVolumePrevious(invoiceDetailDTO.getTotalVolume());
                                            invoiceDetailDTO.setGrossWeightPrevious(invoiceDetailDTO.getGrossWeight());
                                            invoiceDetailDTO.setNetWeightPrevious(invoiceDetailDTO.getNetWeight());
                                            invoiceDetailDTO.setProformaInvoiceDetailLog(null);
                                            invoiceDetailDTO.setTotalBoxPrevious(invoiceDetailDTO.getTotalBox());
                                            idNew[0]--;
                                        } else {
                                            invoiceDetailDTO.setQtyPrevious(qtyPrevious);
                                            invoiceDetailDTO.setAmountPrevious(amountPrevious);
                                            invoiceDetailDTO.setPcsPrevious((int) pcsPrevious);
                                            invoiceDetailDTO.setTotalVolumePrevious(totalVolumePrevious);
                                            invoiceDetailDTO.setGrossWeightPrevious(grossWeightPrevious);
                                            invoiceDetailDTO.setNetWeightPrevious(netWeightPrevious);
                                            invoiceDetailDTO.setUnitPricePrevious(unitPricePrevious);
                                            invoiceDetailDTO.setQtyPrevious(qtyPrevious);
                                            invoiceDetailDTO.setTotalBoxPrevious(totalBoxPrevious);
                                        }
                                        //SET DATA
                                        if (finalIsSupplier) {
                                            invoiceDetailDTO.setNoteAdjustSupplier(noteAdjust);
                                        }
                                        if (finalIsPU) {
                                            invoiceDetailDTO.setNoteAdjust(noteAdjust);
                                        }
                                        if (finalIsSourcing) {
                                            invoiceDetailDTO.setNoteAdjustSourcing(noteAdjust);
                                        }
                                        invoiceDetailDTO.setQty(qtyUpload);
                                        invoiceDetailDTO.setAmount(amount);
                                        invoiceDetailDTO.setPcs(pcs);
                                        invoiceDetailDTO.setTotalVolume(totalVolume);
                                        invoiceDetailDTO.setGrossWeight(totalGrossWeight);
                                        invoiceDetailDTO.setNetWeight(totalNetWeight);
                                        invoiceDetailDTO.setUnitPrice(unitPrice);
                                        invoiceDetailDTO.setTotalBox(totalBox);
                                        resultUploadDetail.put(invoiceDetailDTO.getId(), true);
                                        if (detailDTO.get(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion()) != null) {
                                            proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion());
                                        }
                                        proformaInvoiceDetailDTOSet.add(invoiceDetailDTO);
                                        detailDTO.put(TITLE_HASHMAP + invoiceDetailDTO.getCdcVersion(), proformaInvoiceDetailDTOSet);
                                    } else {
                                        uploadResultDetailsError.add(new UploadResultDetail(index[0], "", oASin.toString(), oSku.toString(), "Not exists in detail this Purchaser Order!", ""));
                                    }
                                }
                                index[0]++;
                            });
                            if (resultUploadDetail.size() > 0) {
                                // check miss data
                                proformaInvoiceDetailsPrevious.stream().filter(item -> resultUploadDetail.get(item.getId()) == null || !resultUploadDetail.get(item.getId())).forEach(element -> uploadResultDetailsError.add(new UploadResultDetail(index[0], "", element.getAsin(), element.getSku(), "Not found in file Excel!", "")));
                            }
                            if (uploadResultDetailsError.isEmpty()) {
                                proformaInvoiceDTO.setProformaInvoiceDetail(detailDTO);
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadResultDetail(uploadResultDetailsError);
                logger.info("END ==========");
                if (uploadResultDetailsError.isEmpty()) {
                    resultUploadDTO.setProformaInvoiceWHDTO(proformaInvoiceDTO);
                }
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPackingList(MultipartFile file, List<Integer> id) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> packingListInfo = mappingTableSheetPackingList(wb);
                // count row i errors
                final int[] i = {-1};
                List<ResultUploadPackingListDetail> resultUploadPackingListDetail = new ArrayList<>();
                Set<BookingPackingListContainerPalletDTO> bookingPackingListContainerPalletDTOs = new HashSet<>();
                BookingPackingListDTO bookingPackingListDTO = new BookingPackingListDTO();
                List<BookingPackingList> bookingPackingLists = bookingPackingListRepository.findAllById(id);
                Set<BookingPackingListDetailsDTO> bookingPackingListDetailPrevious = new HashSet<>();
                //details new
                Set<BookingPackingListDetailsDTO> bookingPackingListDetailNew = new HashSet<>();
                if (!bookingPackingLists.isEmpty()) {
                    for (BookingPackingList bookingPackingList : bookingPackingLists) {
                        BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
                        //get details previous
                        Set<BookingPackingListDetailsDTO> detail = bookingPackingList.getBookingPackingListDetail().stream().map(element -> {
                            BookingPackingListDetailsDTO bookingPackingListDetailsDTO = new BookingPackingListDetailsDTO();
                            BeanUtils.copyProperties(element, bookingPackingListDetailsDTO);
                            return bookingPackingListDetailsDTO;
                        }).collect(Collectors.toSet());
                        bookingPackingListDetailPrevious.addAll(detail);
                    }
                }
                // read sheet detail PKL
                packingListInfo.entrySet().stream().forEach(packingListEntry -> {
                    try {
                        if (!packingListEntry.getKey().equals(CONTAINER_PALLET_SHEET)) {
                            i[0]++;
                            Map<Integer, Boolean> resultUploadDetail = new HashMap<>();

                            if (!bookingPackingListDetailPrevious.isEmpty()) {
                                // get list Po Number
                                List<Map<String, Object>> listPackingList = packingListEntry.getValue();
                                final int[] index = {1};
                                listPackingList.stream().forEach(item -> {
                                    Object oInvoiceNo = item.get(COLUMN_PROFORMA_INVOICE);
                                    Object oPO = item.get(COLUMN_PO_NUMBER);
                                    Object oASin = item.get(COLUMN_ASIN);
                                    Object oSku = item.get(COLUMN_SKU);
                                    Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                    Object oQtyOfEachCarton = item.get(COLUMN_QTY_OF_EACH_CARTON);
                                    Object oCarTon = item.get(COLUMN_TOTAL_CARTON);
                                    Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                    Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                    Object oCbm = item.get(COLUMN_CBM);
                                    Object oContainer = item.get(COLUMN_CONTAINER);
                                    int qtyOrdered = oQuantityOrdered.equals("") ? 0 : Integer.parseInt(oQuantityOrdered.toString());
                                    double grossWeight = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                    double netWeight = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                    int qtyOfEachCarton = oQtyOfEachCarton.equals("") ? 0 : Integer.parseInt(oQtyOfEachCarton.toString());
                                    double cbm = oCbm.equals("") ? 0 : Double.parseDouble(oCbm.toString());
                                    double carTon = oCarTon.equals("") ? 0 : Double.parseDouble(oCarTon.toString());

                                    //set value for field previous
                                    Optional<BookingPackingListDetailsDTO> bookingPackingListDetail = bookingPackingListDetailPrevious.stream().filter(k -> k.getProformaInvoiceNo().equals(oInvoiceNo.toString()) && k.getASin().equals(oASin.toString()) && k.getSku().equals(oSku.toString()) && k.getPoNumber().equals(oPO.toString())).findFirst();
                                    if (bookingPackingListDetail.isPresent()) {
                                        BookingPackingListDetailsDTO bookingPackingListDetailOld = bookingPackingListDetail.get();
                                        bookingPackingListDetailOld.setQuantityPrevious(bookingPackingListDetailOld.getQuantity());
                                        bookingPackingListDetailOld.setQuantity(qtyOrdered);
                                        bookingPackingListDetailOld.setQtyEachCartonPrevious(bookingPackingListDetailOld.getQtyEachCarton());
                                        bookingPackingListDetailOld.setQtyEachCarton(qtyOfEachCarton);
                                        bookingPackingListDetailOld.setTotalCartonPrevious(bookingPackingListDetailOld.getTotalCarton());
                                        bookingPackingListDetailOld.setTotalCarton(carTon);
                                        bookingPackingListDetailOld.setNetWeightPrevious(bookingPackingListDetailOld.getNetWeight());
                                        bookingPackingListDetailOld.setNetWeight(netWeight);
                                        bookingPackingListDetailOld.setGrossWeightPrevious(bookingPackingListDetailOld.getGrossWeight());
                                        bookingPackingListDetailOld.setGrossWeight(grossWeight);
                                        bookingPackingListDetailOld.setCbmPrevious(bookingPackingListDetailOld.getCbm());
                                        bookingPackingListDetailOld.setCbm(cbm);
                                        bookingPackingListDetailOld.setContainer(oContainer.toString());
                                        bookingPackingListDetailNew.add(bookingPackingListDetailOld);
                                        resultUploadDetail.put(bookingPackingListDetailOld.getId(), true);
                                    } else {
                                        resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], oInvoiceNo.toString(), oPO.toString(), oSku.toString(), oASin.toString(), "Data dose not exist in Proforma Invoice"));
                                    }
                                    index[0]++;
                                });
                                bookingPackingListDetailPrevious.stream().filter(item -> resultUploadDetail.get(item.getId()) == null).forEach(element -> resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], element.getProformaInvoiceNo(), element.getPoNumber(), element.getSku(), element.getASin(), "Missing data!")));
                                if (resultUploadPackingListDetail.isEmpty()) {
                                    bookingPackingListDTO.setBookingPackingListDetailsDTO(bookingPackingListDetailNew);
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                //read sheet container
                packingListInfo.entrySet().stream().forEach(packingListEntry -> {
                    try {
                        if (packingListEntry.getKey().equals(CONTAINER_PALLET_SHEET) && resultUploadPackingListDetail.isEmpty()) {

                            List<Map<String, Object>> listContainerPallet = packingListEntry.getValue();
                            final int[] index = {1};
                            listContainerPallet.stream().forEach(item -> {
                                Object oContainer = item.get(COLUMN_PKL_CONTAINER);
                                Object oPallet = item.get(COLUMN_PKL_PALLET);
                                if (oContainer.toString().trim().length() == 0) {
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", "", "", "Sheet container: Container can not empty."));
                                }
                                if (oPallet.toString().trim().length() == 0) {
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", "", "", "Sheet container: Pallet can not empty."));

                                }
                                String pallet = oPallet.toString();
                                if (!CommonDataUtil.isInteger(pallet)) {
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", "", "", "Sheet container: Pallet must is numeric."));

                                }
                                String container = oContainer.toString();
                                if (!bookingPackingListDetailNew.isEmpty() && (bookingPackingListDetailNew.stream().noneMatch(k -> k.getContainer().equals(container)))) {
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", "", "", "Sheet container:Container not exists in detail PKL."));

                                }
                                BookingPackingListContainerPalletDTO bookingPackingListContainerPalletDTO = new BookingPackingListContainerPalletDTO();
                                bookingPackingListContainerPalletDTO.setContainer(container);
                                bookingPackingListContainerPalletDTO.setTotalPalletQty(Integer.valueOf(pallet));
                                if (bookingPackingListContainerPalletDTOs.stream().anyMatch(k -> k.getContainer().equals(container))) {
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", "", "", "Sheet container:" + String.format("Duplicate container: %s in sheet container.", container)));

                                }
                                bookingPackingListContainerPalletDTOs.add(bookingPackingListContainerPalletDTO);
                            });
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setResultUploadPackingListDetail(resultUploadPackingListDetail);
                bookingPackingListDTO.setBookingPackingListContainerPallet(bookingPackingListContainerPalletDTOs);
                resultUploadDTO.setBookingPackingListDTO(bookingPackingListDTO);
                logger.info("END ==========");
                return resultUploadDTO;
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailShipmentPackingList(MultipartFile file, List<Integer> id) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> packingListInfo = mappingTableSheetShipmentPackingList(wb);
                // count row i errors
                final int[] i = {-1};
                List<ResultUploadPackingListDetail> resultUploadPackingListDetail = new ArrayList<>();
                Set<ShipmentsContPalletDTO> shipmentsContPallets = new HashSet<>();
                ShipmentsPackingListDTO shipmentsPackingListDTO = new ShipmentsPackingListDTO();
                List<ShipmentsPackingList> shipmentsPackingLists = shipmentPackingListRepository.findAllById(id);
                List<ShipmentsPackingListDetailDTO> shipmentsPackingListDetailPrevious = new ArrayList<>();
                //details new
                List<ShipmentsPackingListDetailDTO> shipmentsPackingListDetailNew = new ArrayList<>();
                if (!shipmentsPackingLists.isEmpty()) {
                    for (ShipmentsPackingList shipmentsPackingList : shipmentsPackingLists) {
                        BeanUtils.copyProperties(shipmentsPackingList, shipmentsPackingListDTO);
                        //get details previous
                        Set<ShipmentsPackingListDetailDTO> detail = shipmentsPackingList.getShipmentsPackingListDetail().stream().map(element -> {
                            ShipmentsPackingListDetailDTO shipmentsPackingListDetailDTO = new ShipmentsPackingListDetailDTO();
                            BeanUtils.copyProperties(element, shipmentsPackingListDetailDTO);
                            return shipmentsPackingListDetailDTO;
                        }).collect(Collectors.toSet());
                        shipmentsPackingListDetailPrevious.addAll(detail);
                    }
                }
                // read sheet detail PKL
                packingListInfo.entrySet().stream().forEach(packingListEntry -> {
                    try {
                        if (!packingListEntry.getKey().equals(CONTAINER_PALLET_SHEET)) {
                            i[0]++;
                            Map<Integer, Boolean> resultUploadDetail = new HashMap<>();
                            if (!shipmentsPackingListDetailPrevious.isEmpty()) {
                                // get list Po Number
                                List<Map<String, Object>> listPackingList = packingListEntry.getValue();
                                final int[] index = {1};
                                listPackingList.stream().forEach(item -> {
                                    Object oSku = item.get(COLUMN_SKU);
                                    Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                    Object oQtyOfEachCarton = item.get(COLUMN_QTY_OF_EACH_CARTON);
                                    Object oCarTon = item.get(COLUMN_TOTAL_CARTON);
                                    Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                    Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                    Object oCbm = item.get(COLUMN_CBM);
                                    Object oContainer = item.get(COLUMN_CONTAINER);
                                    Object oContainerType = item.get(COLUMN_CONTAINER_TYPE);
                                    Object oProductName = item.get(COLUMN_PRODUCT_NAME);
                                    Object oProformaInvoice = item.get(COLUMN_PROFORMA_INVOICE);
                                    ShipmentsContPalletDTO shipmentsContPallet = new ShipmentsContPalletDTO();
                                    shipmentsContPallet.setPalletQuantity(0);
                                    shipmentsContPallet.setContainerNumber(oContainer.toString());
                                    if (oContainer.toString().length() > 0) {
                                        shipmentsContPallets.add(shipmentsContPallet);
                                    }
                                    int qtyOrdered = oQuantityOrdered.equals("") ? 0 : Integer.parseInt(oQuantityOrdered.toString());
                                    double grossWeight = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                    double netsWeight = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                    int qtyOfEachCarton = oQtyOfEachCarton.equals("") ? 0 : Integer.parseInt(oQtyOfEachCarton.toString());
                                    double cbm = oCbm.equals("") ? 0 : Double.parseDouble(oCbm.toString());
                                    double carTon = oCarTon.equals("") ? 0 : Double.parseDouble(oCarTon.toString());
                                    //set value for field previous
                                    Optional<ShipmentsPackingListDetailDTO> oShipmentsPackingListDetailDTO = shipmentsPackingListDetailPrevious.stream().filter(k -> k.getSku().equals(oSku.toString()) && k.getProformaInvoiceNo().equals(oProformaInvoice.toString())).findFirst();
                                    if (oShipmentsPackingListDetailDTO.isPresent()) {
                                        ShipmentsPackingListDetailDTO shipmentsPackingListDetailDTO = oShipmentsPackingListDetailDTO.get();
                                        shipmentsPackingListDetailDTO.setQuantity(qtyOrdered);
                                        shipmentsPackingListDetailDTO.setProductName(oProductName.toString());
                                        shipmentsPackingListDetailDTO.setQtyEachCarton(qtyOfEachCarton);
                                        shipmentsPackingListDetailDTO.setTotalCarton(carTon);
                                        shipmentsPackingListDetailDTO.setGrossWeight(grossWeight);
                                        shipmentsPackingListDetailDTO.setNetWeight(netsWeight);
                                        shipmentsPackingListDetailDTO.setTotalVolume(cbm);
                                        shipmentsPackingListDetailDTO.setContainerNumber(oContainer.toString());
                                        shipmentsPackingListDetailDTO.setContainerType(oContainerType.toString());
                                        shipmentsPackingListDetailDTO.setProformaInvoiceNo(oProformaInvoice.toString());
                                        shipmentsPackingListDetailNew.add(shipmentsPackingListDetailDTO);
                                        resultUploadDetail.put(shipmentsPackingListDetailDTO.getId(), true);
                                    } else {
                                        resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], oProformaInvoice.toString(), "", oSku.toString(), "", "Data dose not exist in Packing List"));
                                    }
                                    index[0]++;
                                });
                                shipmentsPackingListDetailPrevious.stream().filter(item -> resultUploadDetail.get(item.getId()) == null).forEach(element -> resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", element.getSku(), "", "Missing data!")));
                                if (resultUploadPackingListDetail.isEmpty()) {
                                    shipmentsPackingListDTO.setShipmentsPackingListDetail(shipmentsPackingListDetailNew);
                                }
                            }
                        }
                            } catch (Exception e) {
                                throw new BusinessException(e.getMessage());
                            }
                        });

                        resultUploadDTO.setResultUploadPackingListDetail(resultUploadPackingListDetail);
                        shipmentsPackingListDTO.setShipmentsContPallet(shipmentsContPallets);
                        resultUploadDTO.setShipmentsPackingListDTO(shipmentsPackingListDTO);
                        logger.info("END ==========");
                        return resultUploadDTO;
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                }
                return null;
            }

            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public ResultUploadDTO mappingToDetailCommercialInvoiceWH(MultipartFile file, List<Integer> id) {
                ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
                if (ExcelHelper.hasExcelFormat(file)) {
                    try {

                        logger.info("START import excel  file");
                        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                        mapper.registerModule(new JavaTimeModule());
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                        Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                        Map<String, List<Map<String, Object>>> packingListInfo = mappingTableSheetCommercialInvoiceWH(wb);
                        // count row i errors
                        final int[] i = {-1};
                        List<ResultUploadPackingListDetail> resultUploadPackingListDetail = new ArrayList<>();
                        CommercialInvoiceWHDTO commercialInvoiceWHDTO = new CommercialInvoiceWHDTO();
                        List<CommercialInvoiceWH> commercialInvoiceWHs = commercialInvoiceWHRepository.findAllById(id);
                        List<CommercialInvoiceWHDetailDTO> commercialInvoiceWHDetailDTOOld = new ArrayList<>();
                        //details new
                        List<CommercialInvoiceWHDetailDTO> commercialInvoiceWHDetailDTONew = new ArrayList<>();
                        if (!commercialInvoiceWHs.isEmpty()) {
                            for (CommercialInvoiceWH commercialInvoiceWH : commercialInvoiceWHs) {
                                BeanUtils.copyProperties(commercialInvoiceWH, commercialInvoiceWHs);
                                //get details previous
                                Set<CommercialInvoiceWHDetailDTO> detail = commercialInvoiceWH.getCommercialInvoiceWHDetail().stream().map(element -> {
                                    CommercialInvoiceWHDetailDTO commercialInvoiceWHDetailDTO = new CommercialInvoiceWHDetailDTO();
                                    BeanUtils.copyProperties(element, commercialInvoiceWHDetailDTO);
                                    return commercialInvoiceWHDetailDTO;
                                }).collect(Collectors.toSet());
                                commercialInvoiceWHDetailDTOOld.addAll(detail);
                            }
                        }
                        // read sheet detail PKL
                        packingListInfo.entrySet().stream().forEach(packingListEntry -> {
                            try {
                                i[0]++;
                                Map<Integer, Boolean> resultUploadDetail = new HashMap<>();

                                if (!commercialInvoiceWHDetailDTOOld.isEmpty()) {
                                    // get list Po Number
                                    List<Map<String, Object>> listPackingList = packingListEntry.getValue();
                                    final int[] index = {1};
                                    listPackingList.stream().forEach(item -> {
                                        Object oSku = item.get(COLUMN_SKU);
                                        Object oProductName = item.get(COLUMN_PRODUCT_NAME);
                                        Object oBarcode = item.get(COLUMN_ASIN);
                                        Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);

                                        double unitPrice = oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                                        //set value for field previous
                                        Optional<CommercialInvoiceWHDetailDTO> oCommercialInvoiceWHDetailDTO = commercialInvoiceWHDetailDTOOld.stream().filter(k -> k.getSku().equals(oSku.toString())).findFirst();
                                        if (oCommercialInvoiceWHDetailDTO.isPresent()) {
                                            CommercialInvoiceWHDetailDTO commercialInvoiceWHDetailDTO = oCommercialInvoiceWHDetailDTO.get();
                                            commercialInvoiceWHDetailDTO.setSku(oSku.toString());
                                            commercialInvoiceWHDetailDTO.setProductTitle(oProductName.toString());
                                            commercialInvoiceWHDetailDTO.setASin(oBarcode.toString());
                                            commercialInvoiceWHDetailDTO.setUnitPrice(unitPrice);
                                            double amount=Math.round(unitPrice*commercialInvoiceWHDetailDTO.getQty()*100)/100;
                                            commercialInvoiceWHDetailDTO.setAmount(amount);
                                            commercialInvoiceWHDetailDTONew.add(commercialInvoiceWHDetailDTO);
                                            resultUploadDetail.put(commercialInvoiceWHDetailDTO.getId(), true);
                                        } else {
                                            resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", oSku.toString(), "", "Data dose not exist in Packing List"));
                                        }
                                        index[0]++;
                                    });
                                    commercialInvoiceWHDetailDTOOld.stream().filter(item -> resultUploadDetail.get(item.getId()) == null).forEach(element -> resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0], "", "", element.getSku(), "", "Missing data!")));
                                    if (resultUploadPackingListDetail.isEmpty()) {
                                        commercialInvoiceWHDTO.setCommercialInvoiceWHDetail(commercialInvoiceWHDetailDTONew);
                                    }
                                }
                            } catch (Exception e) {
                                throw new BusinessException(e.getMessage());
                            }
                        });
                        resultUploadDTO.setResultUploadPackingListDetail(resultUploadPackingListDetail);
                        resultUploadDTO.setCommercialInvoiceWHDTO(commercialInvoiceWHDTO);

                        logger.info("END ==========");
                        return resultUploadDTO;
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                }
                return null;
            }

            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public ResultUploadDTO mappingToPOSplit(MultipartFile file) {
                ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
                String filename = file.getOriginalFilename();
                if (filename == null) {
                    return null;
                }
                filename = filename.replace(".xlsx", "").replace(".xls", "");
                Optional<PurchaseOrdersSplit> purchaseOrdersSplit = purchaseOrdersSplitRepository.findByRootFile(filename);
                List<UploadPurchaseOrderSplitStatus> listUploadPurchaseOrderStatus = new ArrayList<>();
                try {
                    if (purchaseOrdersSplit.isPresent()) {
                        throw new BusinessException("File name already exists in system.");
                    }
                    if (ExcelHelper.hasExcelFormat(file)) {

                        logger.info("START import excel  file");
                        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                        mapper.registerModule(new JavaTimeModule());
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                        Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                        List<Map<String, Object>> purchaseOrdersSplitInfo = mappingTableSheetSplitPO(wb.getSheetAt(0));
                        // count row i errors
                        final int[] i = {-1};
                        List<PurchaseOrdersSplitData> purchaseOrdersSplitDataSet = new ArrayList<>();
                        Map<String, List<PurchaseOrdersSplitData>> mapPurchaseOrdersSplitData = new HashMap<>();
                        List<String> duplicateSoSku = new ArrayList<>();
                        if (purchaseOrdersSplitInfo.isEmpty()) {
                            throw new BusinessException("The file import no data.");
                        }
                        purchaseOrdersSplitInfo.stream().filter(m -> m.size() > 0).forEach(item -> {
                            try {
                                i[0]++;

                                PurchaseOrdersSplitData purchaseOrdersSplitData = new PurchaseOrdersSplitData();
                                Object oShipDate = item.get(COLUMN_SHIPDATE);
                                Object oFulfillmentCenter = item.get(COLUMN_FULFILLMENT_CENTER);
                                Object oVendor = item.get(COLUMN_VENDOR);
                                Object oSO = item.get(COLUMN_SO);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oProductName = item.get(COLUMN_PRODUCT_NAME);
                                Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oMTS = item.get(COLUMN_MTS);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oAmount = item.get(COLUMN_AMOUNT);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oPCS = item.get(COLUMN_PCS);
                                Object oVendorCode = item.get(COLUMN_VENDOR_CODE);
                                String country = "";
                                Optional<VendorCountry> oVendorCountry = vendorCountryRepository.findByVendorCode(oVendorCode.toString().trim());
                                if (oVendorCountry.isPresent()) {
                                    country = oVendorCountry.get().getCountry();
                                } else {
                                    listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(oSku.toString(), "errors", "Can't not found country by vendor code " + oVendorCode.toString()));
                                }
                                if (oVendor.toString().trim().length() == 0 || oShipDate.toString().trim().length() == 0 || oFulfillmentCenter.toString().trim().length() == 0) {
                                    listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(oSku.toString(), "errors", "Vendor or Fulfillment center or Ship date  cannot empty."));
                                }
                                if (duplicateSoSku.contains(oSO.toString() + KEY_UPLOAD + oSku.toString())) {
                                    listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(oSku.toString(), "errors", String.format("{Sku=%s ,Po Number=%s} cannot duplicate.", oSku, oSO)));
                                } else {
                                    duplicateSoSku.add(oSO + KEY_UPLOAD + oSku);
                                }
                                boolean isDateValid = CommonDataUtil.isDateValidSplitPO(oShipDate.toString());
                                if (!isDateValid) {
                                    listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(oSku.toString(), "errors", "Format Ship Date must dd-MMM-yy"));
                                }
                                purchaseOrdersSplitData.setShipDate(DateUtils.convertStringLocalDate(oShipDate.toString()));
                                purchaseOrdersSplitData.setFulfillmentCenter(oFulfillmentCenter.toString());
                                purchaseOrdersSplitData.setVendor(oVendor.toString());
                                purchaseOrdersSplitData.setVendorCode(oVendorCode.toString().trim());
                                purchaseOrdersSplitData.setSaleOrder(oSO.toString());
                                purchaseOrdersSplitData.setCountry(country);
                                purchaseOrdersSplitData.setSku(oSku.toString());
                                purchaseOrdersSplitData.setaSin(oASin.toString());
                                purchaseOrdersSplitData.setProductName(oProductName.toString());
                                purchaseOrdersSplitData.setQtyOrdered(oQuantityOrdered.equals("") ? 0 : Long.parseLong(oQuantityOrdered.toString()));
                                purchaseOrdersSplitData.setMakeToStock(oMTS.toString());
                                double unitPrice = oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                                unitPrice = Math.round(unitPrice * 100.0) / 100.0;
                                purchaseOrdersSplitData.setUnitCost(unitPrice);
                                double dblAmount = oAmount.equals("") ? 0 : Double.parseDouble(oAmount.toString());
                                dblAmount = Math.round(dblAmount * 100.0) / 100.0;
                                purchaseOrdersSplitData.setAmount(dblAmount);
                                double cbm = oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString());
                                cbm = Math.round(cbm * 1000.0) / 1000.0;
                                purchaseOrdersSplitData.setCbm(cbm);
                                double nw = oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                nw = Math.round(nw * 1000.0) / 1000.0;
                                purchaseOrdersSplitData.setNetWeight(nw);
                                double gw = oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                gw = Math.round(gw * 1000.0) / 1000.0;
                                purchaseOrdersSplitData.setGrossWeight(gw);
                                purchaseOrdersSplitData.setTotalBox(oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString()));
                                purchaseOrdersSplitData.setPcs(oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString()));
                                purchaseOrdersSplitDataSet.add(purchaseOrdersSplitData);

                            } catch (Exception e) {
                                throw new BusinessException(e.getMessage());
                            }
                        });

                        resultUploadDTO.setUploadPurchaseOrderSplitStatus(listUploadPurchaseOrderStatus);
                        mapPurchaseOrdersSplitData.put(filename, purchaseOrdersSplitDataSet);
                        resultUploadDTO.setPurchaseOrdersSplit(mapPurchaseOrdersSplitData);
                        logger.info("END ==========");
                        return resultUploadDTO;

                    }
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }
                resultUploadDTO.setUploadPurchaseOrderSplitStatus(listUploadPurchaseOrderStatus);
                return resultUploadDTO;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheet(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                if (sheetNames.isEmpty()) {
                    return Collections.emptyMap();
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            //get key with index column
                            String key = getKeyPO(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData == null) {
                                rowData.put(key, "");
                            } else {
                                if (cellData.getCellType() == CellType.FORMULA) {
                                    switch (cellData.getCachedFormulaResultType()) {
                                        case BOOLEAN:
                                            value = String.valueOf(cellData.getBooleanCellValue());
                                            break;
                                        case NUMERIC:
                                            value = String.valueOf(cellData.getNumericCellValue());
                                            break;
                                        case STRING:
                                            value = String.valueOf(cellData.getRichStringCellValue());
                                            break;
                                        default:
                                    }
                                } else {
                                    value = dataFormatter.formatCellValue(cellData);
                                }
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetPOWH(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                if (sheetNames.isEmpty()) {
                    return Collections.emptyMap();
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            //get key with index column
                            String key = getKeyPOWH(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData == null) {
                                rowData.put(key, "");
                            } else {
                                if (cellData.getCellType() == CellType.FORMULA) {
                                    switch (cellData.getCachedFormulaResultType()) {
                                        case BOOLEAN:
                                            value = String.valueOf(cellData.getBooleanCellValue());
                                            break;
                                        case NUMERIC:
                                            value = String.valueOf(cellData.getNumericCellValue());
                                            break;
                                        case STRING:
                                            value = String.valueOf(cellData.getRichStringCellValue());
                                            break;
                                        default:
                                    }
                                } else {
                                    value = dataFormatter.formatCellValue(cellData);
                                }
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private List<Map<String, Object>> mappingTableSheetSplitPO(Sheet sheet) {
                if (sheet == null) {
                    return Collections.emptyList();
                }
                Iterator<Row> rows = sheet.iterator();
                DataFormatter dataFormatter = new DataFormatter();
                Row keyRow = null;
                int colNum = 0;
                List<Map<String, Object>> data = new ArrayList<>();

                int rowNumber = 0;

                while (rows.hasNext()) {
                    Row currentRow = rows.next();
                    Map<String, Object> rowData = new HashMap<>();
                    if (rowNumber < 1) {
                        if (rowNumber == 0) {
                            keyRow = currentRow;
                            colNum = keyRow.getLastCellNum();
                        }
                        rowNumber++;
                        continue;
                    }

                    for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                        Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        Cell keyCell = keyRow.getCell(cellIdx);
                        if (Objects.isNull(keyCell)) {
                            break;
                        }

                        if (cellIdx == 0) {
                            String value = dataFormatter.formatCellValue(cellData);
                            if (value == null || value.trim().length() == 0) {
                                break;
                            }
                        }
                        String value = "";
                        if (cellData != null) {
                            if (cellData.getCellType() == CellType.FORMULA) {
                                switch (cellData.getCachedFormulaResultType()) {
                                    case BOOLEAN:
                                        value = String.valueOf(cellData.getBooleanCellValue());
                                        break;
                                    case NUMERIC:
                                        value = String.valueOf(cellData.getNumericCellValue());
                                        break;
                                    case STRING:
                                        value = String.valueOf(cellData.getRichStringCellValue());
                                        break;
                                    default:
                                }
                            } else {
                                value = dataFormatter.formatCellValue(cellData);
                            }
                        }
                        String key = getKeySplitPO(cellIdx);
                        rowData.put(key, value);
                    }

                    data.add(rowData);
                    rowNumber++;
                }
                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetDetailPO(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            //get key with index column
                            String key = getKeyDetailPO(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData != null) {
                                if (cellData.getCellType() == CellType.FORMULA) {
                                    switch (cellData.getCachedFormulaResultType()) {
                                        case BOOLEAN:
                                            value = String.valueOf(cellData.getBooleanCellValue());
                                            break;
                                        case NUMERIC:
                                            value = String.valueOf(cellData.getNumericCellValue());
                                            break;
                                        case STRING:
                                            value = String.valueOf(cellData.getRichStringCellValue());
                                            break;
                                        default:
                                    }
                                } else {
                                    value = dataFormatter.formatCellValue(cellData);
                                }
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetDetailPI(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            //get key with index column
                            String key = getKeyDetailPI(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData != null) {
                                if (cellData.getCellType() == CellType.FORMULA) {
                                    switch (cellData.getCachedFormulaResultType()) {
                                        case BOOLEAN:
                                            value = String.valueOf(cellData.getBooleanCellValue());
                                            break;
                                        case NUMERIC:
                                            value = String.valueOf(cellData.getNumericCellValue());
                                            break;
                                        case STRING:
                                            value = String.valueOf(cellData.getRichStringCellValue());
                                            break;
                                        default:
                                    }
                                } else {
                                    value = dataFormatter.formatCellValue(cellData);
                                }
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetDetailPIWH(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            //get key with index column
                            String key = getKeyDetailPIWH(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData != null) {
                                if (cellData.getCellType() == CellType.FORMULA) {
                                    switch (cellData.getCachedFormulaResultType()) {
                                        case BOOLEAN:
                                            value = String.valueOf(cellData.getBooleanCellValue());
                                            break;
                                        case NUMERIC:
                                            value = String.valueOf(cellData.getNumericCellValue());
                                            break;
                                        case STRING:
                                            value = String.valueOf(cellData.getRichStringCellValue());
                                            break;
                                        default:
                                    }
                                } else {
                                    value = dataFormatter.formatCellValue(cellData);
                                }
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetPackingList(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            String key = "";
                            //get key with index column
                            if (item.equals(CONTAINER_PALLET_SHEET)) {
                                key = getKeyContainerPallet(cellIdx);
                            } else {
                                key = getKeyDetailPackingList(cellIdx);
                            }
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData.getCellType() == CellType.FORMULA) {
                                switch (cellData.getCachedFormulaResultType()) {
                                    case BOOLEAN:
                                        value = String.valueOf(cellData.getBooleanCellValue());
                                        break;
                                    case NUMERIC:
                                        value = String.valueOf(cellData.getNumericCellValue());
                                        break;
                                    case STRING:
                                        value = String.valueOf(cellData.getRichStringCellValue());
                                        break;
                                    default:
                                }
                            } else {
                                value = dataFormatter.formatCellValue(cellData);
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetShipmentPackingList(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            String key = "";
                            //get key with index column
                            key = getKeyDetailShipmentPackingList(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData.getCellType() == CellType.FORMULA) {
                                switch (cellData.getCachedFormulaResultType()) {
                                    case BOOLEAN:
                                        value = String.valueOf(cellData.getBooleanCellValue());
                                        break;
                                    case NUMERIC:
                                        value = String.valueOf(cellData.getNumericCellValue());
                                        break;
                                    case STRING:
                                        value = String.valueOf(cellData.getRichStringCellValue());
                                        break;
                                    default:
                                }
                            } else {
                                value = dataFormatter.formatCellValue(cellData);
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            private Map<String, List<Map<String, Object>>> mappingTableSheetCommercialInvoiceWH(Workbook wb) {
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet firstSheet = wb.getSheetAt(i);
                    sheetNames.add(firstSheet.getSheetName());
                }
                Map<String, List<Map<String, Object>>> data = new HashMap<>();
                sheetNames.forEach(item -> {
                    Sheet sheet = wb.getSheet(item);
                    Iterator<Row> rows = sheet.iterator();
                    DataFormatter dataFormatter = new DataFormatter();
                    Row keyRow = null;
                    String nameSheet = item;
                    int colNum = 0;
                    int rowNumber = 0;
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();
                        Map<String, Object> rowData = new HashMap<>();
                        if (rowNumber < 1) {
                            if (rowNumber == 0) {
                                keyRow = currentRow;
                                colNum = keyRow.getLastCellNum();
                            }
                            rowNumber++;
                            continue;
                        }

                        for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                            Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Cell keyCell = keyRow.getCell(cellIdx);
                            if (Objects.isNull(keyCell)) {
                                break;
                            }
                            if (cellIdx == 0 && Objects.isNull(cellData)) {
                                break;
                            }
                            String key = "";
                            //get key with index column
                            key = getKeyDetailConmercialInvoiceWH(cellIdx);
                            data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                            String value = "";
                            if (cellData.getCellType() == CellType.FORMULA) {
                                switch (cellData.getCachedFormulaResultType()) {
                                    case BOOLEAN:
                                        value = String.valueOf(cellData.getBooleanCellValue());
                                        break;
                                    case NUMERIC:
                                        value = String.valueOf(cellData.getNumericCellValue());
                                        break;
                                    case STRING:
                                        value = String.valueOf(cellData.getRichStringCellValue());
                                        break;
                                    default:
                                }
                            } else {
                                value = dataFormatter.formatCellValue(cellData);
                            }
                            rowData.put(key, value);
                        }
                        if (rowData.size() > 0) {
                            data.get(nameSheet).add(rowData);
                        } else {
                            break;
                        }
                        rowNumber++;
                    }
                });

                return data;
            }

            public String getKeySplitPO(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SO;
                        break;
                    case 1:
                        key = COLUMN_SKU;
                        break;
                    case 2:
                        key = COLUMN_ASIN;
                        break;
                    case 3:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 4:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 5:
                        key = COLUMN_SHIPDATE;
                        break;
                    case 6:
                        key = COLUMN_FULFILLMENT_CENTER;
                        break;
                    case 7:
                        key = COLUMN_VENDOR;
                        break;
                    case 8:
                        key = COLUMN_MTS;
                        break;
                    case 9:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 10:
                        key = COLUMN_AMOUNT;
                        break;
                    case 11:
                        key = COLUMN_CBM;
                        break;
                    case 12:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 13:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 14:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 15:
                        key = COLUMN_PCS;
                        break;
                    case 16:
                        key = COLUMN_VENDOR_CODE;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyPO(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SO;
                        break;
                    case 1:
                        key = COLUMN_SKU;
                        break;
                    case 2:
                        key = COLUMN_ASIN;
                        break;
                    case 3:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 4:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 5:
                        key = COLUMN_SHIPDATE;
                        break;
                    case 6:
                        key = COLUMN_FULFILLMENT_CENTER;
                        break;
                    case 7:
                        key = COLUMN_VENDOR;
                        break;
                    case 8:
                        key = COLUMN_MTS;
                        break;
                    case 9:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 10:
                        key = COLUMN_AMOUNT;
                        break;
                    case 11:
                        key = COLUMN_CBM;
                        break;
                    case 12:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 13:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 14:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 15:
                        key = COLUMN_PCS;
                        break;
                    case 16:
                        key = COLUMN_VENDOR_CODE;
                        break;
                    case 17:
                        key = COLUMN_DEMAND;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyPOWH(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_PL_ORDER;
                        break;
                    case 1:
                        key = COLUMN_VENDOR;
                        break;
                    case 2:
                        key = COLUMN_SKU;
                        break;
                    case 3:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 4:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 5:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 6:
                        key = COLUMN_AMOUNT;
                        break;
                    case 7:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 8:
                        key = COLUMN_PCS ;
                        break;
                    case 9:
                        key = COLUMN_CBM;
                        break;
                    case 10:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 11:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 12:
                        key = COLUMN_ASIN;
                        break;
                    case 13:
                        key = COLUMN_PALLET_QUANTITY;
                        break;
                    case 14:
                        key = COLUMN_MTS;
                        break;
                    case 15:
                        key = COLUMN_NOTE;
                        break;
                    case 16:
                        key = COLUMN_CONTAINER_NO;
                        break;
                    case 17:
                        key = COLUMN_CONTAINER_TYPE;
                        break;
                    case 18:
                        key = COLUMN_SHIPDATE;
                        break;
                    case 19:
                        key = COLUMN_ETD;
                        break;
                    case 20:
                        key = COLUMN_POL;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailPO(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SO;
                        break;
                    case 1:
                        key = COLUMN_SKU;
                        break;
                    case 2:
                        key = COLUMN_ASIN;
                        break;
                    case 3:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 4:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 5:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 6:
                        key = COLUMN_AMOUNT;
                        break;
                    case 7:
                        key = COLUMN_PCS;
                        break;
                    case 8:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 9:
                        key = COLUMN_CBM;
                        break;
                    case 10:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 11:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 12:
                        key = COLUMN_MTS;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailPI(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SO;
                        break;
                    case 1:
                        key = COLUMN_SKU;
                        break;
                    case 2:
                        key = COLUMN_ASIN;
                        break;
                    case 3:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 4:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 5:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 6:
                        key = COLUMN_AMOUNT;
                        break;
                    case 7:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 8:
                        key = COLUMN_PCS;
                        break;
                    case 9:
                        key = COLUMN_CBM;
                        break;
                    case 10:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 11:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 12:
                        key = COLUMN_MTS;
                        break;
                    case 13:
                        key = COLUMN_NOTE_ADJUST;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailPIWH(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SKU;
                        break;
                    case 1:
                        key = COLUMN_ASIN;
                        break;
                    case 2:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 3:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 4:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 5:
                        key = COLUMN_AMOUNT;
                        break;
                    case 6:
                        key = COLUMN_TOTAL_BOX;
                        break;
                    case 7:
                        key = COLUMN_PCS;
                        break;
                    case 8:
                        key = COLUMN_CBM;
                        break;
                    case 9:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 10:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 11:
                        key = COLUMN_MTS;
                        break;
                    case 12:
                        key = COLUMN_NOTE_ADJUST;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailPackingList(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_PROFORMA_INVOICE;
                        break;
                    case 1:
                        key = COLUMN_PO_NUMBER;
                        break;
                    case 2:
                        key = COLUMN_SKU;
                        break;
                    case 3:
                        key = COLUMN_ASIN;
                        break;
                    case 4:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 5:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 6:
                        key = COLUMN_QTY_OF_EACH_CARTON;
                        break;
                    case 7:
                        key = COLUMN_TOTAL_CARTON;
                        break;
                    case 8:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 9:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 10:
                        key = COLUMN_CBM;
                        break;
                    case 11:
                        key = COLUMN_CONTAINER;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailShipmentPackingList(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SKU;
                        break;
                    case 1:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 2:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 3:
                        key = COLUMN_QTY_OF_EACH_CARTON;
                        break;
                    case 4:
                        key = COLUMN_TOTAL_CARTON;
                        break;
                    case 5:
                        key = COLUMN_CBM;
                        break;
                    case 6:
                        key = COLUMN_NET_WEIGHT;
                        break;
                    case 7:
                        key = COLUMN_GROSS_WEIGHT;
                        break;
                    case 8:
                        key = COLUMN_CONTAINER;
                        break;
                    case 9:
                        key = COLUMN_CONTAINER_TYPE;
                        break;
                    case 10:
                        key = COLUMN_PROFORMA_INVOICE;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyDetailConmercialInvoiceWH(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_SKU;
                        break;
                    case 1:
                        key = COLUMN_ASIN;
                        break;
                    case 2:
                        key = COLUMN_PRODUCT_NAME;
                        break;
                    case 3:
                        key = COLUMN_QUANTITY_ORDERED;
                        break;
                    case 4:
                        key = COLUMN_UNIT_PRICE;
                        break;
                    case 5:
                        key = COLUMN_AMOUNT;
                        break;
                    default:
                }
                return key;
            }

            public String getKeyContainerPallet(int indexColNum) {
                String key = "";
                switch (indexColNum) {
                    case 0:
                        key = COLUMN_PKL_CONTAINER;
                        break;
                    case 1:
                        key = COLUMN_PKL_PALLET;
                        break;
                    default:
                }
                return key;
            }
        }
