package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.PurchaseOrdersWHService;
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

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.utils.CommonDataUtil.getSubjectMailWH;
import static com.yes4all.common.utils.ExcelHelper.createCell;
import static com.yes4all.constants.GlobalConstant.*;


/**
 * Service Implementation for managing {@link PurchaseOrdersWH}.
 */
@Service
@Transactional
public class PurchaseOrdersWHServiceImpl implements PurchaseOrdersWHService {

    @Autowired
    private ShipmentsPurchaseOrdersRepository shipmentsPurchaseOrdersRepository;




    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersWHServiceImpl.class);
    @Autowired
    private ProformaInvoiceWHRepository proformaInvoiceWHRepository;

    @Autowired
    private ProformaInvoiceWHDetailRepository proformaInvoiceWHDetailRepository;

    @Value("${attribute.link.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PO = "/purchase-order/detail/";
    private static final String TITLE_HASHMAP = "detail_";
    @Autowired
    private PurchaseOrdersWHRepository purchaseOrdersWHRepository;
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseOrdersWHDetailRepository purchaseOrdersWHDetailRepository;
    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private PurchaseOrdersCommonService purchaseOrdersCommonService;

    @Autowired
    SupplierCountryRepository supplierCountryRepository;

    @Override
    public boolean removePurchaseOrders(List<Integer> listPurchaseOrderId, String userName) {
        try {
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrdersWH> oPurchaseOrder = purchaseOrdersWHRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrdersWH purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() == 3) {
                        throw new BusinessException("Can't Deleted with status Confirmed");
                    }
                    Optional<SupplierCountry> oSupplierCountry = supplierCountryRepository.findBySupplierAndCountry(purchaseOrders.getVendorId(), purchaseOrders.getCountry());
                    if (oSupplierCountry.isEmpty()) {

                        throw new BusinessException("Supplier not found.");
                    }
                    SupplierCountry supplierCountry = oSupplierCountry.get();
                    Integer numberOrderNo = oSupplierCountry.get().getOrderNumberWh();
                    supplierCountry.setOrderNumberWh(numberOrderNo - 1);
                    supplierCountryRepository.save(supplierCountry);
                    purchaseOrdersWHRepository.delete(purchaseOrders);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean sendPurchaseOrdersWH(List<Integer> listPurchaseOrderId, String userId) {
        try {
            List<String> listPO = new ArrayList<>();
            listPurchaseOrderId.forEach(i -> {
                Optional<PurchaseOrdersWH> oPurchaseOrder = purchaseOrdersWHRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrder.get();
                    if (purchaseOrdersWH.getStatus() != GlobalConstant.STATUS_PO_NEW) {
                        listPO.add(purchaseOrdersWH.getPoNumber());
                    } else {

                        List<String> listEmail = new ArrayList<>();
                        Optional<User> oUserSupplier = userRepository.findOneByVendor(purchaseOrdersWH.getVendorId());
                        Optional<User> oUser = userRepository.findOneByLogin(userId);
                        if (oUserSupplier.isEmpty()) {
                            throw new BusinessException("Can not find user Supplier in the system.");
                        }
                        if (oUser.isEmpty()) {
                            throw new BusinessException("Can not find user login in the system.");
                        }
                        User userVendor = oUserSupplier.get();
                        User user = oUser.get();
                        listEmail.add(userVendor.getEmail());
                        if (userVendor.getEmail().length() == 0) {
                            throw new BusinessException("User supplier not define email.");
                        }
                        if (userVendor.getListUserPu() == null || userVendor.getListUserPu().trim().length() == 0) {
                            throw new BusinessException("User send not list PU of Supplier ");

                        }
                        String[] listUserPU = userVendor.getListUserPu().split(";");
                        if (Arrays.stream(listUserPU).noneMatch(userId::equals)) {
                            throw new BusinessException("User send not list PU of Supplier ");
                        }
                        purchaseOrdersWH.setOrderedDate(new Date().toInstant());
                        purchaseOrdersWH.setStatus(GlobalConstant.STATUS_PO_SENT);
                        purchaseOrdersWH.setUserSend(userId);
                        purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                        if (Boolean.FALSE.equals(user.getSupplier())) {
                            String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                            String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrdersWH.getId() + "?type=WAREHOUSE", purchaseOrdersWH.getPoNumber(), supplier, "The purchase order", "NEW", "NEW");
                            List<String> listMailSC = getListUserSC(userVendor);
                            List<String> listMailPU = getListUserPU(userVendor);
                            List<String> listMailCC = new ArrayList<>();
                            listMailCC.addAll(listMailSC);
                            listMailCC.addAll(listMailPU);
                            String subject = getSubjectMailWH(purchaseOrdersWH.getPoNumber(), userVendor, purchaseOrdersWH.getEtdOriginal());

                            sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                        }
                    }
                }
            });
            if (!listPO.isEmpty()) {
                throw new BusinessException(String.format("Purchase Orders { %s } were sent to Supplier. Please check again.", listPO.stream().collect(Collectors.joining(","))));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
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


    @Override
    public ProformaInvoiceWHDTO createProformaInvoiceWH(Integer purchaseOrderId, String userName) {
        try {
            Optional<PurchaseOrdersWH> oPurchaseOrder = purchaseOrdersWHRepository.findById(purchaseOrderId);
            if (oPurchaseOrder.isPresent()) {
                PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrder.get();
                if (purchaseOrdersWH.getStatus() != GlobalConstant.STATUS_PO_SENT) {
                    throw new BusinessException(String.format("Purchase Orders { %s } were sent to Supplier. Please check again.", purchaseOrderId));
                } else {
                    ProformaInvoiceWH proformaInvoiceWH = new ProformaInvoiceWH();
                    proformaInvoiceWH.setOrderNo(purchaseOrdersWH.getPoNumber());
                    proformaInvoiceWH.setDate(LocalDate.now());
                    proformaInvoiceWH.setShipDate(purchaseOrdersWH.getExpectedShipDate());
                    proformaInvoiceWH.setStatus(GlobalConstant.STATUS_PI_NEW);
                    proformaInvoiceWH.setCreatedBy(userName);
                    proformaInvoiceWH.setUpdatedBy(userName);
                    TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
                    TimeZone.setDefault(timeZone);
                    Date date = new Date();
                    proformaInvoiceWH.setCreatedDate(date.toInstant().atZone(ZoneId.systemDefault()).toInstant());
                    proformaInvoiceWH.setUpdatedDate(new Date().toInstant());
                    Set<ProformaInvoiceWHDetail> proformaInvoiceWHDetailSet;
                    Set<PurchaseOrdersWHDetail> purchaseOrdersWHDetail = purchaseOrdersWH.getPurchaseOrdersWHDetail();
                    proformaInvoiceWHDetailSet = purchaseOrdersWHDetail.stream().map(item -> {
                        ProformaInvoiceWHDetail proformaInvoiceWHDetail = new ProformaInvoiceWHDetail();
                        proformaInvoiceWHDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceWHDetail.class);
                        proformaInvoiceWHDetail.setId(null);
                        proformaInvoiceWHDetail.setCdcVersion(0L);
                        proformaInvoiceWHDetail.setUnitPrice(item.getUnitPrice());
                        proformaInvoiceWHDetail.setQtyPrevious(proformaInvoiceWHDetail.getQty());
                        proformaInvoiceWHDetail.setAmountPrevious(proformaInvoiceWHDetail.getAmount());
                        proformaInvoiceWHDetail.setUnitPricePrevious(proformaInvoiceWHDetail.getUnitPrice());
                        proformaInvoiceWHDetail.setPcsPrevious(proformaInvoiceWHDetail.getPcs());
                        proformaInvoiceWHDetail.setTotalBoxPrevious(proformaInvoiceWHDetail.getTotalBox());
                        proformaInvoiceWHDetail.setTotalVolumePrevious(proformaInvoiceWHDetail.getTotalVolume());
                        proformaInvoiceWHDetail.setGrossWeightPrevious(proformaInvoiceWHDetail.getGrossWeight());
                        proformaInvoiceWHDetail.setNetWeightPrevious(proformaInvoiceWHDetail.getNetWeight());
                        proformaInvoiceWHDetail.setUpdatedBy(proformaInvoiceWH.getUpdatedBy());
                        proformaInvoiceWHDetail.setUpdatedDate(new Date().toInstant());
                        proformaInvoiceWHDetail.setCreatedBy(proformaInvoiceWH.getUpdatedBy());
                        proformaInvoiceWHDetail.setCreatedDate(new Date().toInstant());
                        proformaInvoiceWHDetail.setContainerNo(item.getContainerNo());
                        proformaInvoiceWHDetail.setContainerType(item.getContainerType());

                        return proformaInvoiceWHDetail;
                    }).collect(Collectors.toSet());
                    proformaInvoiceWH.setProformaInvoiceWHDetail(proformaInvoiceWHDetailSet);
                    proformaInvoiceWH.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_SUPPLIER);
                    proformaInvoiceWH.setStepActionBy(STEP_ACTION_BY_SOURCING);
                    proformaInvoiceWH.setTotalQuantity(proformaInvoiceWHDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum));
                    proformaInvoiceWH.setCtn(proformaInvoiceWHDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalBox()) ? 0 : x.getTotalBox()).reduce(0.0, Double::sum));
                    proformaInvoiceWH.setAmount(proformaInvoiceWHDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                    proformaInvoiceWH.setGrossWeight(proformaInvoiceWHDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
                    proformaInvoiceWH.setCbmTotal(proformaInvoiceWHDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
//                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(purchaseOrdersWH.getVendorId());
//                    if (oVendor.isPresent()) {
//                        Vendor vendor = oVendor.get();
//                        proformaInvoiceWH.setSeller(vendor.getVendorName() + " \n " + vendor.getFactoryAddress());
//                        if (vendor.getBankInformation() != null) {
//                            proformaInvoiceWH.setCompanyName(vendor.getBankInformation().getCompanyName());
//                            proformaInvoiceWH.setAcNumber(vendor.getBankInformation().getAcNumber());
//                            proformaInvoiceWH.setBeneficiaryBank(vendor.getBankInformation().getBeneficiaryBank());
//                            proformaInvoiceWH.setSwiftCode(vendor.getBankInformation().getSwiftCode());
//                        }
//                    } else {
//                        throw new BusinessException("Vendor code not exists!");
//                    }
                    proformaInvoiceWH.setSupplier(purchaseOrdersWH.getVendorId());
                    //proformaInvoiceWH.setpurchaseOrdersWH(purchaseOrdersWH);
                    purchaseOrdersWH.setStatus(GlobalConstant.STATUS_PO_PROCESSING);
                    purchaseOrdersWH.setProformaInvoiceWH(proformaInvoiceWH);
                    proformaInvoiceWH.setIsSupplier(true);
                    proformaInvoiceWH.setUserPUPrimary(purchaseOrdersWH.getUserSend());
                    proformaInvoiceWHRepository.save(proformaInvoiceWH);
                    return mappingEntityToDtoPI(proformaInvoiceWH, ProformaInvoiceWHDTO.class);
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }


    public boolean updateStatus(PurchaseOrdersWH purchaseOrdersWH, Integer status) {
        try {
            purchaseOrdersWH.setStatus(status);
            purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    @Override
    public Page<PurchaseOrdersWHMainDTO> listingPurchaseOrdersWHWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<PurchaseOrdersWH> data = purchaseOrdersWHRepository.findByCondition(
            filterParams.get("poNumber"),
            filterParams.get("updatedBy"), filterParams.get("country"),
            filterParams.get("status"),
            filterParams.get("supplier")
            , filterParams.get("supplierSearch"),
            filterParams.get("updatedDateFrom"), filterParams.get("updatedDateTo"),
            filterParams.get("expectedShipDateFrom"), filterParams.get("expectedShipDateTo"),
            filterParams.get("actualShipDateFrom"), filterParams.get("actualShipDateTo"),
            filterParams.get("etdFrom"), filterParams.get("etdTo"),
            filterParams.get("etaFrom"), filterParams.get("etaTo"),
            filterParams.get("atdFrom"), filterParams.get("atdTo"),
            filterParams.get("ataFrom"), filterParams.get("ataTo"),
            filterParams.get("shipmentId"), filterParams.get("orderedDateFrom"),
            filterParams.get("orderedDateTo"),
            pageable);
        return data.map(item -> mappingEntityToDto(item, PurchaseOrdersWHMainDTO.class));
    }

    public ListDetailPOWHDTO getListSkuFromPO(List<Integer> id) {
        try {
            ListDetailPOWHDTO listDetailPODTO = new ListDetailPOWHDTO();
            final String[] purchaseOrderNo = {""};
            final Integer[] purchaseOrderId = {null};
            Map<String, Set<ProformaInvoiceWHDetailDTO>> detailDTO = new HashMap<>();
            final double[] totalAmount = {0};
            final Set<ProformaInvoiceWHDetailDTO>[] detailPISet = new Set[]{new HashSet<>()};

            id.forEach(i -> {
                Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(i);
                if (oPurchaseOrdersWH.isPresent()) {
                    PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                    listDetailPODTO.setShipDate(purchaseOrdersWH.getExpectedShipDate());
                    purchaseOrderNo[0] = purchaseOrdersWH.getPoNumber();
                    purchaseOrderId[0] = purchaseOrdersWH.getId();
                    Set<PurchaseOrdersWHDetail> purchaseOrdersWHDetail = purchaseOrdersWH.getPurchaseOrdersWHDetail();
                    detailPISet[0] = purchaseOrdersWHDetail.stream().map(item -> {
                        ProformaInvoiceWHDetailDTO proformaInvoiceWHDetailDTO = new ProformaInvoiceWHDetailDTO();
                        proformaInvoiceWHDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceWHDetailDTO.class);
                        proformaInvoiceWHDetailDTO.setId(null);
                        proformaInvoiceWHDetailDTO.setKey(proformaInvoiceWHDetailDTO.getSku());
                        totalAmount[0] += item.getAmount();
                        return proformaInvoiceWHDetailDTO;
                    }).collect(Collectors.toSet());
                    detailDTO.put("detail_0", detailPISet[0]);
//                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(purchaseOrdersWH.getVendorId());
//                    if (oVendor.isPresent()) {
//                        Vendor vendor = oVendor.get();
//                        listDetailPODTO.setSeller(vendor.getVendorName() + " \n " + vendor.getFactoryAddress());
//                        if (vendor.getBankInformation() != null) {
//                            listDetailPODTO.setCompanyName(vendor.getBankInformation().getCompanyName());
//                            listDetailPODTO.setAcNumber(vendor.getBankInformation().getAcNumber());
//                            listDetailPODTO.setBeneficiaryBank(vendor.getBankInformation().getBeneficiaryBank());
//                            listDetailPODTO.setSwiftCode(vendor.getBankInformation().getSwiftCode());
//                        }
//                    } else {
//                        throw new BusinessException("Vendor code not exists!");
//                    }
                }
            });
            listDetailPODTO.setDetails(detailDTO);
            listDetailPODTO.setPurchaserOrderNo(purchaseOrderNo[0]);
            listDetailPODTO.setPurchaserOrderId(purchaseOrderId[0]);
            listDetailPODTO.setTotalAmount(totalAmount[0]);
            return listDetailPODTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    private <T> T mappingEntityToDto(PurchaseOrdersWH purchaseOrdersWH, Class<T> clazz) {
        try {

            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(purchaseOrdersWH, dto);
            ProformaInvoiceWH proformaInvoiceWH = purchaseOrdersWH.getProformaInvoiceWH();
            Integer proformaInvoiceWHId = null;
            String proformaInvoiceWHNo = null;
            if (proformaInvoiceWH != null) {
                proformaInvoiceWHId = proformaInvoiceWH.getId();
                proformaInvoiceWHNo = proformaInvoiceWH.getOrderNo();
            }
            if (dto instanceof PurchaseOrdersWHMainDTO) {
                Optional<User> oUser = userRepository.findOneByLogin(purchaseOrdersWH.getCreatedBy());
                Optional<ShipmentsPurchaseOrders> oShipmentsPurchaseOrders = shipmentsPurchaseOrdersRepository.findOneByPurchaseOrderId(purchaseOrdersWH.getId());
                Integer shipmentId = null;
                String shipmentNo = null;
                if (oShipmentsPurchaseOrders.isPresent()) {
                    shipmentNo = oShipmentsPurchaseOrders.get().getShipment().getShipmentId();
                    shipmentId = oShipmentsPurchaseOrders.get().getShipment().getId();
                }
                String createdBy = "";
                if (oUser.isPresent()) {
                    createdBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                }
                clazz.getMethod("setShipmentId", Integer.class).invoke(dto, shipmentId);
                clazz.getMethod("setShipmentNo", String.class).invoke(dto, shipmentNo);
                clazz.getMethod("setCreatedBy", String.class).invoke(dto, createdBy);
                clazz.getMethod("setProformaInvoiceId", Integer.class).invoke(dto, proformaInvoiceWHId);
                clazz.getMethod("setProformaInvoiceNo", String.class).invoke(dto, proformaInvoiceWHNo);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    private <T> T mappingEntityToDtoPI(ProformaInvoiceWH proformaInvoiceWH, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(proformaInvoiceWH, dto);
            ProformaInvoiceWH objectModel = null;
            if (proformaInvoiceWH.getProformaInvoiceWHDetail().isEmpty()) {
                Optional<ProformaInvoiceWH> oproformaInvoiceWH = proformaInvoiceWHRepository.findById(proformaInvoiceWH.getId());
                if (oproformaInvoiceWH.isPresent()) {
                    objectModel = oproformaInvoiceWH.get();
                }
            } else {
                objectModel = proformaInvoiceWH;
            }
            if (objectModel == null) {
                objectModel = proformaInvoiceWH;
            }

            Set<ProformaInvoiceWHDetail> detailSet = objectModel.getProformaInvoiceWHDetail().stream().filter(i -> !i.isDeleted()).collect(Collectors.toSet());

            Optional<User> oUser = userRepository.findOneByLogin(proformaInvoiceWH.getUpdatedBy());
            String updatedBy = "";
            if (oUser.isPresent()) {
                updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
            }

            if (dto instanceof ProformaInvoiceWHDTO) {
                Map<String, List<ProformaInvoiceWHDetailDTO>> detailDTO = new HashMap<>();
                detailSet.parallelStream().forEach(item -> {
                    ProformaInvoiceWHDetailDTO proformaInvoiceWHDetailDTO;
                    proformaInvoiceWHDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceWHDetailDTO.class);
                    List<ProformaInvoiceWHDetailDTO> proformaInvoiceWHDetailDTOSet = new ArrayList<>();
                    if (detailDTO.get(TITLE_HASHMAP + item.getCdcVersion()) != null) {
                        proformaInvoiceWHDetailDTOSet = detailDTO.get(TITLE_HASHMAP + item.getCdcVersion());
                    }
                    proformaInvoiceWHDetailDTOSet.add(proformaInvoiceWHDetailDTO);
                    detailDTO.put(TITLE_HASHMAP + item.getCdcVersion(), proformaInvoiceWHDetailDTOSet);
                });
                clazz.getMethod("setProformaInvoiceDetail", Map.class).invoke(dto, detailDTO);
            } else if (dto instanceof ProformaInvoiceWHMainDTO) {
                clazz.getMethod("setUpdatedBy", String.class).invoke(dto, updatedBy);
            }

            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
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
        createCell(sheet, row, i++, "SKU", style);
        createCell(sheet, row, i++, "Barcode", style);
        createCell(sheet, row, i++, "Product Name", style);
        createCell(sheet, row, i++, "Quantity Ordered", style);
        createCell(sheet, row, i++, "MTS", style);
        createCell(sheet, row, i++, "Unit Price", style);
        createCell(sheet, row, i++, "Amount", style);
        createCell(sheet, row, i++, "QTY/CTN", style);
        createCell(sheet, row, i++, "IB/MB", style);
        createCell(sheet, row, i++, "CBM(M3)", style);
        createCell(sheet, row, i++, "G.W(KG)", style);
        createCell(sheet, row, i++, "PALLET QUANTITY", style);
        createCell(sheet, row, i++, "CONT NO", style);
        createCell(sheet, row, i++, "CONT TYPE", style);
        createCell(sheet, row, i, "NOTE", style);

    }


    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) {
        try {

            Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(id);
            PurchaseOrdersWH purchaseOrdersWH;
            if (oPurchaseOrdersWH.isEmpty()) {
                throw new BusinessException("Purchase Order not found.");
            }
            purchaseOrdersWH = oPurchaseOrdersWH.get();
            XSSFSheet sheet = workbook.createSheet(purchaseOrdersWH.getPoNumber());
            int rowCount = 1;
            writeHeaderLine(workbook, sheet);
            CellStyle style = workbook.createCellStyle();
            Set<PurchaseOrdersWHDetail> purchaseOrdersWHDetailSet = new HashSet<>(purchaseOrdersWH.getPurchaseOrdersWHDetail());
            for (PurchaseOrdersWHDetail detail : purchaseOrdersWHDetailSet) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(sheet, row, columnCount++, detail.getSku(), style);
                createCell(sheet, row, columnCount++, detail.getAsin(), style);
                createCell(sheet, row, columnCount++, detail.getProductName(), style);
                createCell(sheet, row, columnCount++, detail.getQty(), style);
                createCell(sheet, row, columnCount++, detail.getMakeToStock(), style);
                createCell(sheet, row, columnCount++, detail.getUnitPrice(), style);
                createCell(sheet, row, columnCount++, detail.getAmount(), style);
                createCell(sheet, row, columnCount++, detail.getPcs(), style);
                createCell(sheet, row, columnCount++, detail.getTotalBox(), style);
                createCell(sheet, row, columnCount++, detail.getTotalVolume(), style);
                createCell(sheet, row, columnCount++, detail.getGrossWeight(), style);
                createCell(sheet, row, columnCount++, detail.getPalletQuantity(), style);
                createCell(sheet, row, columnCount++, detail.getContainerNo(), style);
                createCell(sheet, row, columnCount++, detail.getContainerType(), style);
                createCell(sheet, row, columnCount, detail.getNote(), style);
            }
            return workbook;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void export(String filename, Integer id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFile(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }

    @Override
    public boolean updateShipDate(LogUpdateDateRequestDTO request) {
        try {
            Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findByIdAndActive(request.getId(), GlobalConstant.STATUS_PO_CANCEL);
            if (oPurchaseOrdersWH.isPresent()) {
                PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                LocalDate dateAfter = DateUtils.convertStringLocalDateBooking(request.getDate());
                if (request.getTypeUpdateDate().equals(PO_SHIP_DATE)) {
                    purchaseOrdersWH.setExpectedShipDate(dateAfter);
                } else if (request.getTypeUpdateDate().equals(PO_ACTUAL_SHIP_DATE)) {
                    purchaseOrdersWH.setActualShipDate(dateAfter);
                } else if (request.getTypeUpdateDate().equals(PO_ETD)) {
                    purchaseOrdersWH.setEtd(dateAfter);
                } else if (request.getTypeUpdateDate().equals(PO_ETA)) {
                    purchaseOrdersWH.setEta(dateAfter);
                } else if (request.getTypeUpdateDate().equals(PO_ATA)) {
                    purchaseOrdersWH.setAta(dateAfter);
                }

                Set<PurchaseOrdersWHDate> setPurchaseOrdersWHDate = purchaseOrdersWH.getPurchaseOrdersWHDate();
                PurchaseOrdersWHDate newestOrderDateBefore = getNewestOrderDateBefore(setPurchaseOrdersWHDate, request.getTypeUpdateDate());
                if (newestOrderDateBefore != null) {
                    LocalDate dateBefore = newestOrderDateBefore.getDateAfter();
                    if (!dateBefore.isEqual(dateAfter)) {
                        PurchaseOrdersWHDate purchaseOrdersWHDate = new PurchaseOrdersWHDate();
                        purchaseOrdersWHDate.setDateBefore(dateBefore);
                        purchaseOrdersWHDate.setDateAfter(dateAfter);
                        purchaseOrdersWHDate.setTypeDate(request.getTypeUpdateDate());
                        purchaseOrdersWHDate.setPurchaseOrdersWH(purchaseOrdersWH);
                        purchaseOrdersWHDate.setCreatedBy(newestOrderDateBefore.getCreatedBy());
                        purchaseOrdersWHDate.setUpdatedBy(request.getUserId());
                        setPurchaseOrdersWHDate.add(purchaseOrdersWHDate);
                        purchaseOrdersWH.setPurchaseOrdersWHDate(setPurchaseOrdersWHDate);
                    } else {
                        throw new BusinessException("The ship date is the same day before and after the update.");
                    }
                } else {
                    PurchaseOrdersWHDate purchaseOrdersWHDate = new PurchaseOrdersWHDate();
                    purchaseOrdersWHDate.setDateBefore(null);
                    purchaseOrdersWHDate.setDateAfter(dateAfter);
                    purchaseOrdersWHDate.setTypeDate(request.getTypeUpdateDate());
                    purchaseOrdersWHDate.setPurchaseOrdersWH(purchaseOrdersWH);
                    purchaseOrdersWHDate.setCreatedBy(request.getUserId());
                    purchaseOrdersWHDate.setUpdatedBy(request.getUserId());
                    setPurchaseOrdersWHDate.add(purchaseOrdersWHDate);
                    purchaseOrdersWH.setPurchaseOrdersWHDate(setPurchaseOrdersWHDate);
                }

                ProformaInvoiceWH proformaInvoiceWH = purchaseOrdersWH.getProformaInvoiceWH();
                if (CommonDataUtil.isNotNull(proformaInvoiceWH)) {
                    // proformaInvoiceWH
                    if (request.getTypeUpdateDate().equals(PO_SHIP_DATE)) {
                        proformaInvoiceWH.setShipDate(dateAfter);
                        purchaseOrdersWH.setProformaInvoiceWH(proformaInvoiceWH);
                    }
                }
                // Update
                purchaseOrdersWHRepository.save(purchaseOrdersWH);

                if (!GlobalConstant.STATUS_PO_NEW.equals(purchaseOrdersWH.getStatus())) {
                    // List email Y4A
                    List<String> listEmailCC = getListMailY4A();
                    //list mail cc
                    List<String> listMailCC = new ArrayList<>();
                    // List email Supplier
                    List<String> listMailReceive = new ArrayList<>();
                    Optional<User> optUser = userRepository.findOneByVendor(purchaseOrdersWH.getVendorId());
                    if (optUser.isPresent()) {
                        listMailReceive.add(optUser.get().getEmail());
                        List<String> listMailPU = getListUserPU(optUser.get());
                        listMailCC.addAll(listMailPU);
                    } else {
                        throw new BusinessException("Can not find user Vendor");
                    }

                    // Send mail
                    if (CommonDataUtil.isNotEmpty(listMailReceive)) {
                        String subject = getSubjectMailWH(purchaseOrdersWH.getPoNumber(), optUser.get(), purchaseOrdersWH.getEtdOriginal());
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrdersWH.getId() + "?type=WAREHOUSE", purchaseOrdersWH.getPoNumber(),
                            null, null, null, GlobalConstant.PO_SHIP_DATE);
                        sendMailService.sendMail(subject, content, listMailReceive, listEmailCC, listMailCC, null);
                    }
                }

                return true;
            } else {
                throw new BusinessException("Purchase Order not found.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    private List<PurchaseOrdersWHDateDTO> getHistoryDate(Set<PurchaseOrdersWHDate> purchaseOrdersWHDateSet, String typeDate) {
        List<PurchaseOrdersWHDateDTO> result;
        result = purchaseOrdersWHDateSet.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate()))
            .sorted(Comparator.comparing(PurchaseOrdersWHDate::getCreatedDate).reversed())
            .map(item -> {
                User user = userRepository.findOneByLogin(item.getUpdatedBy()).orElse(null);
                String updateBy = CommonDataUtil.getUserFullName(user);

                PurchaseOrdersWHDateDTO dto = new PurchaseOrdersWHDateDTO();
                BeanUtils.copyProperties(item, dto);
                dto.setUpdatedBy(updateBy);
                return dto;
            })
            .collect(Collectors.toList());
        return result;
    }

    private PurchaseOrdersWHDate getNewestOrderDateBefore(Set<PurchaseOrdersWHDate> purchaseOrdersWHDateSet, String typeDate) {
        return purchaseOrdersWHDateSet.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate())).max(Comparator.comparing(PurchaseOrdersWHDate::getCreatedDate)).orElse(null);
    }

    private List<String> getListMailY4A() {
        List<String> listEmail = new ArrayList<>();
        List<User> listEmailYes4all = userRepository.findAllByIsYes4all(true);
        if (CommonDataUtil.isNotEmpty(listEmailYes4all)) {
            listEmail.addAll(listEmailYes4all.stream().map(User::getEmail).collect(Collectors.toList()));
        }
        return listEmail;
    }
    @Override
    public PurchaseOrderWHDetailPageDTO getPurchaseOrdersDetailWithFilter(BodyGetDetailDTO request) {
        try {
            Optional<PurchaseOrdersWH> oPurchaseOrder = purchaseOrdersWHRepository.findById(request.getId());
            if (oPurchaseOrder.isPresent()) {
                PurchaseOrdersWH purchaseOrders = oPurchaseOrder.get();
                if (request.getVendor().length() > 0 && (!request.getVendor().equals(purchaseOrders.getVendorId()) || Objects.equals(purchaseOrders.getStatus(), GlobalConstant.STATUS_PO_NEW))) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                PurchaseOrderWHDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderWHDetailPageDTO.class);
                // find details with value search is sku/aSin/productName
                List<PurchaseOrdersWHDetail> purchaseOrdersDetail = purchaseOrdersWHDetailRepository.findByCondition(purchaseOrders);
                // convert page entity to page entity dto
                // Page<PurchaseOrderDetailDTO> pagePurchaseOrdersDetailDto = pagePurchaseOrdersDetail.map(this::convertToObjectDto);
                List<PurchaseOrderWHDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                    PurchaseOrderWHDetailDTO purchaseOrderDetailDTO = new PurchaseOrderWHDetailDTO();
                    BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                    return purchaseOrderDetailDTO;
                }).collect(Collectors.toList());
                data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
                Set<PurchaseOrdersWHDate> purchaseOrdersDateSet = purchaseOrders.getPurchaseOrdersWHDate();
                data.setExpectedDateDetails(getHistoryDate(purchaseOrdersDateSet, GlobalConstant.PO_SHIP_DATE));
                if (purchaseOrders.getProformaInvoiceWH() != null) {
                    data.setProformaInvoiceId(purchaseOrders.getProformaInvoiceWH().getId());
                }
                Optional<User> oUser = userRepository.findOneByLogin(purchaseOrders.getCreatedBy());
                String createdNameBy = "";
                if (oUser.isPresent()) {
                    createdNameBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                }
                data.setCreatedNameBy(createdNameBy);
                Optional<User> oUserUpdate = userRepository.findOneByLogin(purchaseOrders.getUpdatedBy());
                String updatedNameBy = "";
                if (oUserUpdate.isPresent()) {
                    updatedNameBy = oUserUpdate.get().getLastName() + " " + oUserUpdate.get().getFirstName();
                }
                data.setUpdatedNameBy(updatedNameBy);
                Optional<ShipmentsPurchaseOrders> oShipmentsPurchaseOrders = shipmentsPurchaseOrdersRepository.findOneByPurchaseOrderId(purchaseOrders.getId());
                Integer shipmentId;
                String shipmentNo;
                if (oShipmentsPurchaseOrders.isPresent()) {
                    shipmentNo = oShipmentsPurchaseOrders.get().getShipment().getShipmentId();
                    shipmentId = oShipmentsPurchaseOrders.get().getShipment().getId();
                    data.setShipmentId(shipmentId);
                    data.setShipmentNo(shipmentNo);
                }

                return data;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }


    @Override
    public PurchaseOrderWHDetailPageDTO getPurchaseOrdersDetailWithFilterOrderNo(String orderNo, Integer page, Integer limit) {
        Optional<PurchaseOrdersWH> oPurchaseOrder = purchaseOrdersWHRepository.findAllByPoNumber(orderNo);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrdersWH purchaseOrders = oPurchaseOrder.get();
            if (Objects.equals(purchaseOrders.getStatus(), GlobalConstant.STATUS_PO_NEW)) {
                PurchaseOrderWHDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderWHDetailPageDTO.class);
                // find details with value search is sku/aSin/productName
                List<PurchaseOrdersWHDetail> purchaseOrdersDetail = purchaseOrdersWHDetailRepository.findByCondition(purchaseOrders);
                // convert page entity to page entity dto
                List<PurchaseOrderWHDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                    PurchaseOrderWHDetailDTO purchaseOrderDetailDTO = new PurchaseOrderWHDetailDTO();
                    BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                    return purchaseOrderDetailDTO;
                }).collect(Collectors.toList());
                data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
                return data;
            }
        }
        return null;
    }

}
