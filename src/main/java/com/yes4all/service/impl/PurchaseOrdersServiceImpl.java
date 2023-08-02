package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.PurchaseOrdersService;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.common.utils.ExcelHelper.createCell;
import static com.yes4all.constants.GlobalConstant.*;


/**
 * Service Implementation for managing {@link PurchaseOrders}.
 */
@Service
@Transactional
public class PurchaseOrdersServiceImpl implements PurchaseOrdersService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersServiceImpl.class);
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;


    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;

    @Value("${attribute.link.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PO = "/purchase-order/detail/";
    private static final String TITLE_HASHMAP = "detail_";
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;

    @Autowired
    private BookingProformaInvoiceRepository bookingProformaInvoiceRepository;

    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private PurchaseOrdersCommonService purchaseOrdersCommonService;


    @Autowired
    SupplierCountryRepository supplierCountryRepository;

    @Override
    public boolean removePurchaseOrders(List<Integer> listPurchaseOrderId, String userName) {
        try {
            listPurchaseOrderId.forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() == 3) {
                        throw new BusinessException("Can't Deleted with status Confirmed");
                    }
                    Optional<SupplierCountry> oSupplierCountry = supplierCountryRepository.findBySupplierAndCountry(purchaseOrders.getVendorId(), purchaseOrders.getCountry());
                    if (oSupplierCountry.isEmpty()) {
                        throw new BusinessException("Supplier not found.");
                    }
                    SupplierCountry supplierCountry = oSupplierCountry.get();
                    Integer numberOrderNo = oSupplierCountry.get().getOrderNumber();
                    supplierCountry.setOrderNumber(numberOrderNo - 1);
                    supplierCountryRepository.save(supplierCountry);
                    purchaseOrdersRepository.delete(purchaseOrders);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

//    @Override
//    public boolean confirmedPurchaseOrders(List<Integer> listPurchaseOrderId, String userId) {
//        try {
//            List<String> listPO = new ArrayList<>();
//            listPurchaseOrderId.stream().forEach(i -> {
//                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
//                if (oPurchaseOrder.isPresent()) {
//                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
//                    if (purchaseOrders.getStatus() == 3) {
//                        throw new BusinessException(String.format("Can't Deleted with status Confirmed"));
//                    }
//                    if (purchaseOrders.getStatus() != 0 && purchaseOrders.getStatus() != 1 && purchaseOrders.getStatus() != 2) {
//                        listPO.add(purchaseOrders.getPoNumber());
//                    } else {
//                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_CONFIRMED);
//                        purchaseOrdersRepository.saveAndFlush(purchaseOrders);
//                        List<String> listEmail = new ArrayList<>();
//                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
//                        boolean isSupplier = false;
//                        List<User> listEmailYes4all = userRepository.findAllByIsYes4all(true);
//                        if (listEmailYes4all.isEmpty()) {
//                            throw new BusinessException(String.format("Can not find any user Yes4all in the system."));
//                        }
//                        if (!userEmailsSupplier.isPresent()) {
//                            throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", purchaseOrders.getVendorId()));
//                        }
//                        User userVendor = userEmailsSupplier.get();
//                        if (userId.equals(userVendor.getLogin())) {
//                            isSupplier = true;
//                        }
//                            if (isSupplier) {
//                                listEmail.addAll(listEmailYes4all.stream().map(k->k.getEmail()).collect(Collectors.toList()));
//                                String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
//                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(), supplier, "The purchase order", "confirmed", "Confirmed");
//                                sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order has been confirmed by supplier " + supplier + "", content, listEmail);
//                            } else {
//                                listEmail.add(userVendor.getEmail());
//                                String supplier = "Yes4all";
//                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(), supplier, "The purchase order adjustment", "confirmed", "ConfirmedAdjustment");
//                                sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order adjustment has been confirmed by " + supplier + "", content, listEmail);
//                            }
//
//
//                    }
//                }
//            });
//            if (listPO.size() > 0) {
//                throw new BusinessException(String.format("Purchase Orders { %s } were confirmed by Supplier. Please check again."), listPO.stream().collect(Collectors.joining(",")));
//            }
//            return true;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new BusinessException(e.getMessage());
//        }
//    }

    @Override
    public boolean sendPurchaseOrders(List<Integer> listPurchaseOrderId, String userId) {
        try {
            List<String> listPO = new ArrayList<>();
            listPurchaseOrderId.forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (!Objects.equals(purchaseOrders.getStatus(), STATUS_PO_NEW)) {
                        listPO.add(purchaseOrders.getPoNumber());
                    } else {

                        List<String> listEmail = new ArrayList<>();
                        Optional<User> oUserSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
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
                        purchaseOrders.setOrderedDate(new Date().toInstant());
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_SENT);
                        purchaseOrders.setUserSend(userId);
                        purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                        if (Boolean.FALSE.equals(user.getSupplier())) {
                            String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                            String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?type=DI", purchaseOrders.getPoNumber(), supplier, "The purchase order", "NEW", "NEW");
                            List<String> listMailSC = getListUserSC(userVendor);
                            List<String> listMailPU = getListUserPU(userVendor);
                            List<String> listMailCC = new ArrayList<>();
                            listMailCC.addAll(listMailSC);
                            listMailCC.addAll(listMailPU);
                            String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), userVendor);
                            sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                        }
                    }
                }
            });
            if (!listPO.isEmpty()) {
                throw new BusinessException(String.format("Purchase Orders { %s } were sent to Supplier. Please check again.", String.join(",", listPO)));
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
    public boolean updateShipWindow(PurchaseOrdersShipWindowRequestDTO dto) {
        try {
            List<PurchaseOrders> purchaseOrdersList = purchaseOrdersRepository.findAllByFromSo(dto.getListSO());
            for (PurchaseOrders purchaseOrders : purchaseOrdersList) {
                List<String> fromSoList = purchaseOrders.getPurchaseOrdersDetail().stream().map(PurchaseOrdersDetail::getFromSo).distinct().collect(Collectors.toList());
                String lastShipWindow = purchaseOrdersCommonService.getLastShipWindow(fromSoList);
                if (!CommonDataUtil.isEmpty(lastShipWindow)) {

                    LocalDate dateAfter = DateUtils.convertStringLocalDateBooking((lastShipWindow));
                    purchaseOrders.setShipWindowStart(dateAfter);

                    boolean update = false;
                    Set<PurchaseOrdersDate> setPurchaseOrdersDate = purchaseOrders.getPurchaseOrdersDate();
                    PurchaseOrdersDate newestOrderDateBefore = getNewestOrderDateBefore(setPurchaseOrdersDate, GlobalConstant.PO_SHIP_WINDOW);
                    if (newestOrderDateBefore != null) {
                        LocalDate dateBefore = newestOrderDateBefore.getDateAfter();
                        if (!dateBefore.isEqual(dateAfter)) {
                            PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                            purchaseOrdersDate.setDateBefore(dateBefore);
                            purchaseOrdersDate.setDateAfter(dateAfter);
                            purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_WINDOW);
                            purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                            purchaseOrdersDate.setCreatedBy(newestOrderDateBefore.getCreatedBy());
                            setPurchaseOrdersDate.add(purchaseOrdersDate);
                            purchaseOrders.setPurchaseOrdersDate(setPurchaseOrdersDate);
                            update = true;
                        }
                    } else {
                        PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                        purchaseOrdersDate.setDateBefore(null);
                        purchaseOrdersDate.setDateAfter(dateAfter);
                        purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_WINDOW);
                        purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                        setPurchaseOrdersDate.add(purchaseOrdersDate);
                        purchaseOrders.setPurchaseOrdersDate(setPurchaseOrdersDate);
                        update = true;
                    }

                    if (update) {
                        // Update
                        purchaseOrdersRepository.save(purchaseOrders);

                        // List email Y4A
                        List<String> listEmail = getListMailY4A();
                        // Send mail
                        if (CommonDataUtil.isNotEmpty(listEmail)) {
                            String subjectMail = purchaseOrders.getPoNumber() + " - Update Deadline submit booking";
                            String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?type=DI", purchaseOrders.getPoNumber(),
                                null, null, null, GlobalConstant.PO_SHIP_WINDOW);
                            sendMailService.sendMail(subjectMail, content, listEmail, null, null, null);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public ProformaInvoiceDTO createProformaInvoice(Integer purchaseOrderId, String userName) {
        try {
            Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(purchaseOrderId);
            if (oPurchaseOrder.isPresent()) {
                PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                if (!Objects.equals(purchaseOrders.getStatus(), STATUS_PO_SENT)) {
                    throw new BusinessException(String.format("Purchase Orders { %s } were sent to Supplier. Please check again.", purchaseOrderId));
                } else {
                    ProformaInvoice proformaInvoice = new ProformaInvoice();
                    proformaInvoice.setFulfillmentCenter(purchaseOrders.getFulfillmentCenter());
                    proformaInvoice.setOrderNo(purchaseOrders.getPoNumber());
                    proformaInvoice.setDate(LocalDate.now());
                    proformaInvoice.setShipDate(purchaseOrders.getExpectedShipDate());
                    proformaInvoice.setVendorCode(purchaseOrders.getVendorCode());
                    proformaInvoice.setStatus(GlobalConstant.STATUS_PI_NEW);
                    proformaInvoice.setCreatedBy(userName);
                    proformaInvoice.setUpdatedBy(userName);
                    proformaInvoice.setCreatedDate(new Date().toInstant());
                    proformaInvoice.setUpdatedDate(new Date().toInstant());
                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet;
                    Set<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrders.getPurchaseOrdersDetail();
                    proformaInvoiceDetailSet = purchaseOrdersDetail.stream().filter(k -> !k.getIsDeleted()).map(item -> {
                        ProformaInvoiceDetail proformaInvoiceDetail = new ProformaInvoiceDetail();
                        proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                        proformaInvoiceDetail.setId(null);
                        proformaInvoiceDetail.setCdcVersion(0L);
                        proformaInvoiceDetail.setUnitPrice(item.getUnitPrice());
                        proformaInvoiceDetail.setQtyPrevious(proformaInvoiceDetail.getQty());
                        proformaInvoiceDetail.setAmountPrevious(proformaInvoiceDetail.getAmount());
                        proformaInvoiceDetail.setUnitPricePrevious(proformaInvoiceDetail.getUnitPrice());
                        proformaInvoiceDetail.setPcsPrevious(proformaInvoiceDetail.getPcs());
                        proformaInvoiceDetail.setTotalBoxPrevious(proformaInvoiceDetail.getTotalBox());
                        proformaInvoiceDetail.setTotalVolumePrevious(proformaInvoiceDetail.getTotalVolume());
                        proformaInvoiceDetail.setGrossWeightPrevious(proformaInvoiceDetail.getGrossWeight());
                        proformaInvoiceDetail.setNetWeightPrevious(proformaInvoiceDetail.getNetWeight());
                        proformaInvoiceDetail.setUpdatedBy(proformaInvoice.getUpdatedBy());
                        proformaInvoiceDetail.setUpdatedDate(new Date().toInstant());

                        return proformaInvoiceDetail;
                    }).collect(Collectors.toSet());
                    proformaInvoice.setProformaInvoiceDetail(proformaInvoiceDetailSet);
                    proformaInvoice.setUserUpdatedLatest(GlobalConstant.USER_UPDATED_SUPPLIER);
                    proformaInvoice.setStepActionBy(STEP_ACTION_BY_SOURCING);
                    proformaInvoice.setAmount(proformaInvoiceDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                    proformaInvoice.setCtn(proformaInvoiceDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalBox()) ? 0 : x.getTotalBox()).reduce(0.0, Double::sum));
                    proformaInvoice.setGrossWeight(proformaInvoiceDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
                    proformaInvoice.setCbmTotal(proformaInvoiceDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(purchaseOrders.getVendorId());
//                    if (oVendor.isPresent()) {
//                        Vendor vendor = oVendor.get();
//                        proformaInvoice.setSeller(vendor.getVendorName() + " \n " + vendor.getFactoryAddress());
//                        if (vendor.getBankInformation() != null) {
//                            proformaInvoice.setCompanyName(vendor.getBankInformation().getCompanyName());
//                            proformaInvoice.setAcNumber(vendor.getBankInformation().getAcNumber());
//                            proformaInvoice.setBeneficiaryBank(vendor.getBankInformation().getBeneficiaryBank());
//                            proformaInvoice.setSwiftCode(vendor.getBankInformation().getSwiftCode());
//                        }
//                    } else {
//                        throw new BusinessException("Vendor code not exists!");
//                    }
                    proformaInvoice.setSupplier(purchaseOrders.getVendorId());
                    //proformaInvoice.setPurchaseOrders(purchaseOrders);
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PROCESSING);
                    purchaseOrders.setProformaInvoice(proformaInvoice);
                    proformaInvoice.setIsSupplier(true);
                    proformaInvoice.setUserPUPrimary(purchaseOrders.getUserSend());
                    proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                    return mappingEntityToDtoPI(proformaInvoice, ProformaInvoiceDTO.class);
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean cancelPurchaseOrders(List<Integer> listPurchaseOrderId, String userName, String reason) {
        try {
            listPurchaseOrderId.forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() != GlobalConstant.STATUS_PO_NEW) {
                        throw new BusinessException("Can not cancel status other New.");
                    }
//                    List<String> listEmail = new ArrayList<>();
//                    Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
//                    Optional<User> oUserNames = userRepository.findOneByLogin(userName);
//                    if (!userEmailsSupplier.isPresent()) {
//                        throw new BusinessException("Can not find user Supplier in the system.");
//                    }
//                    User userVendor = userEmailsSupplier.get();
//                    User userNames = oUserNames.get();
//                    listEmail.add(userVendor.getEmail());
                    purchaseOrdersRepository.deleteById(purchaseOrders.getId());
//                    if (!userNames.getSupplier()) {
//                        String supplier = "Yes4all";
//                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId(), purchaseOrders.getPoNumber(), supplier, "The purchase order", "cancelled", "Cancelled");
//                        ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
//                        emailExecutor.execute(() -> sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order has been cancelled by supplier " + supplier + "", content, listEmail));
//                        emailExecutor.shutdown();
//                    }
                }
            });

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

//    @Override
//    public boolean removeSkuFromDetails(Integer purchaseOrderId, List<Integer> listIdDetails, String userName) {
//
//        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(purchaseOrderId);
//        if (oPurchaseOrder.isPresent()) {
//            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
//            Long cdcVersionMax = purchaseOrdersDetailRepository.findMaxCdcVersion(purchaseOrders.getId());
//            Set<PurchaseOrdersDetail> purchaseOrdersDetail = new HashSet<>();
//            purchaseOrders.getPurchaseOrdersDetail().stream().forEach(item -> {
//                if (listIdDetails.contains(item.getId())) {
//                    item.setDeleted(true);
//                    item.setDeletedDate(new Date().toInstant());
//                    item.setDeletedBy(userName);
//                    item.setCdcVersion(cdcVersionMax + 1);
//                }
//                purchaseOrdersDetail.add(item);
//            });
//            purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetail);
//            purchaseOrders.setTotalItem(purchaseOrdersDetail.stream().filter(i -> !i.getDeleted()).count());
//            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
//            return true;
//        }
//        return false;
//
//    }


    @Override
    public PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilter(BodyGetDetailDTO request) {
        try {
            Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(request.getId());
            if (oPurchaseOrder.isPresent()) {
                PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                if (request.getVendor().length() > 0 && (!request.getVendor().equals(purchaseOrders.getVendorId()) || Objects.equals(purchaseOrders.getStatus(), STATUS_PO_NEW))) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDetailPageDTO.class);
                // find details with value search is sku/aSin/productName
                List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition(purchaseOrders);
                // convert page entity to page entity dto
                // Page<PurchaseOrderDetailDTO> pagePurchaseOrdersDetailDto = pagePurchaseOrdersDetail.map(this::convertToObjectDto);
                List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                    PurchaseOrderDetailDTO purchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
                    BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                    return purchaseOrderDetailDTO;
                }).collect(Collectors.toList());
                data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
                data.setFromPurchaseOrderId(purchaseOrders.getId());
                Set<PurchaseOrdersDate> purchaseOrdersDateSet = purchaseOrders.getPurchaseOrdersDate();
                data.setDeadlineDateDetails(getHistoryDate(purchaseOrdersDateSet, GlobalConstant.PO_SHIP_WINDOW));
                data.setExpectedDateDetails(getHistoryDate(purchaseOrdersDateSet, GlobalConstant.PO_SHIP_DATE));
                if (purchaseOrders.getProformaInvoice() != null) {
                    data.setProformaInvoiceId(purchaseOrders.getProformaInvoice().getId());
                }
                return data;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilterOrderNo(String orderNo, Integer page, Integer limit) {
        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findByPoNumberAndIsDeleted(orderNo, false);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            if (Objects.equals(purchaseOrders.getStatus(), STATUS_PO_NEW)) {
                PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDetailPageDTO.class);
                // find details with value search is sku/aSin/productName
                List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition(purchaseOrders);
                // convert page entity to page entity dto
                List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                    PurchaseOrderDetailDTO purchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
                    BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                    return purchaseOrderDetailDTO;
                }).collect(Collectors.toList());
                data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
                return data;
            }
        }
        return null;
    }

    public boolean updateStatus(PurchaseOrders purchaseOrders, Integer status) {
        try {
            purchaseOrders.setStatus(status);
            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

//    @Override
//    public PurchaseOrderDetailPageDTO editSkuPurchaseOrder(Set<PurchaseOrderDetailDTO> purchaseOrderDetailDTO, Integer id) {
//        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
//        if (oPurchaseOrder.isPresent()) {
//            PurchaseOrders purchaseOrder = oPurchaseOrder.get();
//            Set<PurchaseOrdersDetail> purchaseOrdersDetailPrevious = oPurchaseOrder.get().getPurchaseOrdersDetail();
//            Set<PurchaseOrdersDetail> purchaseOrdersDetailNew = purchaseOrdersDetailPrevious.stream().map(i -> {
//                PurchaseOrdersDetail purchaseOrderDetail = new PurchaseOrdersDetail();
//                purchaseOrderDetailDTO.stream().filter(e -> e.getId().equals(i.getId())).forEach(
//                    k -> {
//                        BeanUtils.copyProperties(k, purchaseOrderDetail);
//                        if (i.getAmount() == (k.getAmount())) {
//                            purchaseOrderDetail.setAmountPrevious(i.getAmountPrevious());
//                        } else {
//                            purchaseOrderDetail.setAmountPrevious(i.getAmount());
//                        }
//                        if (i.getUnitPrice() == (k.getUnitCost())) {
//                            purchaseOrderDetail.setUnitCostPrevious(i.getUnitCostPrevious());
//                        } else {
//                            purchaseOrderDetail.setUnitCostPrevious(i.getUnitPrice());
//                        }
//                        if (i.getQty() == (k.getQtyOrdered())) {
//                            purchaseOrderDetail.setQtyOrderedPrevious(i.getQtyOrderedPrevious());
//                        } else {
//                            purchaseOrderDetail.setQtyOrderedPrevious(i.getQtyOrdered());
//                        }
//                        if (i.getPcs() == (k.getPcs())) {
//                            purchaseOrderDetail.setPcsPrevious(i.getPcsPrevious());
//                        } else {
//                            purchaseOrderDetail.setPcsPrevious(i.getPcs());
//                        }
//                        if (i.getTotalBoxNetWeight() == (k.getTotalBoxNetWeight())) {
//                            purchaseOrderDetail.setTotalBoxNetWeight(i.getTotalBoxNetWeightPrevious());
//                        } else {
//                            purchaseOrderDetail.setTotalBoxNetWeightPrevious(i.getTotalBoxNetWeight());
//                        }
//                        if (i.getTotalBox() == (k.getTotalBox())) {
//                            purchaseOrderDetail.setTotalBoxPrevious(i.getTotalBoxPrevious());
//                        } else {
//                            purchaseOrderDetail.setTotalBoxPrevious(i.getTotalBox());
//                        }
//                        if (i.getTotalVolume() == (k.getTotalVolume())) {
//                            purchaseOrderDetail.setTotalVolumePrevious(i.getTotalVolumePrevious());
//                        } else {
//                            purchaseOrderDetail.setTotalVolumePrevious(i.getTotalVolume());
//                        }
//                        if (i.getTotalBoxCrossWeight() == (k.getTotalBoxCrossWeight())) {
//                            purchaseOrderDetail.setTotalBoxCrossWeightPrevious(i.getTotalBoxCrossWeightPrevious());
//                        } else {
//                            purchaseOrderDetail.setTotalBoxCrossWeightPrevious(i.getTotalBoxCrossWeight());
//                        }
//                    }
//                );
//                return purchaseOrderDetail;
//            }).collect(Collectors.toSet());
//            purchaseOrder.setPurchaseOrdersDetail(purchaseOrdersDetailNew);
//            purchaseOrder.setTotalItem(purchaseOrdersDetailNew.stream().filter(k -> !k.isDeleted()).map(x -> Objects.isNull(x.getQtyOrdered()) ? 0 : x.getQtyOrdered()).reduce(0L, Long::sum));
//            purchaseOrder.setTotalCost(purchaseOrdersDetailNew.stream().filter(k -> !k.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
//            purchaseOrdersRepository.saveAndFlush(purchaseOrder);
//            PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrder, PurchaseOrderDetailPageDTO.class);
//            // find details with value search is sku/aSin/productName
//            List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition(purchaseOrder);
//            // convert page entity to page entity dto
//            List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
//                PurchaseOrderDetailDTO ePurchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
//                BeanUtils.copyProperties(item, ePurchaseOrderDetailDTO);
//                return ePurchaseOrderDetailDTO;
//            }).collect(Collectors.toList());
//            data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
//            return data;
//        }
//        return null;
//
//    }

    @Override
    public PurchaseOrdersMainDTO editPurchaseOrder(PurchaseOrdersMainDTO purchaseOrdersMainDTO, Integer id, String userId) {
        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
        if (oPurchaseOrder.isPresent()) {
            //create new purchaser order
            PurchaseOrders purchaseOrdersNew = new PurchaseOrders();
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            List<PurchaseOrders> purchaseOrdersList = purchaseOrdersRepository.findAllByPoNumber(purchaseOrders.getPoNumber());
            //find max cdc version update for po old
            List<Long> cdcVersion = purchaseOrdersList.stream().map(i -> i.getCdcVersion() == null ? 0 : i.getCdcVersion()).collect(Collectors.toList());
            Long cdcVersionMax = Collections.max(cdcVersion, null);
            Set<PurchaseOrdersDetail> purchaseOrdersDetails = purchaseOrders.getPurchaseOrdersDetail();
            BeanUtils.copyProperties(purchaseOrdersMainDTO, purchaseOrders);
            purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetails);
            purchaseOrders.setId(id);
            purchaseOrders.setCdcVersion(cdcVersionMax + 1);
            purchaseOrders.setDeleted(true);
            purchaseOrders.setProformaInvoice(null);
            //get proforma invoice
            ProformaInvoice proformaInvoice = purchaseOrders.getProformaInvoice();
            purchaseOrders.setProformaInvoice(null);
            // save PO old
            purchaseOrdersRepository.save(purchaseOrders);
            // copy info from po old to po new
            BeanUtils.copyProperties(purchaseOrders, purchaseOrdersNew);
            purchaseOrdersNew.setCdcVersion(null);
            purchaseOrdersNew.setId(null);
            purchaseOrdersNew.setDeleted(false);
            purchaseOrdersDetails = purchaseOrdersDetails.stream().map(i -> {
                i.setPurchaseOrders(null);
                return i;
            }).collect(Collectors.toSet());
            purchaseOrdersNew.setPurchaseOrdersDetail(purchaseOrdersDetails);
            purchaseOrdersNew.setProformaInvoice(proformaInvoice);
            purchaseOrdersNew.setFromId(purchaseOrders.getId());
            // save PO new
            List<String> listEmail;
            Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
            List<User> listEmailYes4all = userRepository.findAllByIsYes4all(true);
            if (listEmailYes4all.isEmpty()) {
                throw new BusinessException("Can not find any user Yes4all in the system.");
            } else {
                listEmail = new ArrayList<>(listEmailYes4all.stream().map(User::getEmail).collect(Collectors.toList()));
            }
            if (userEmailsSupplier.isEmpty()) {
                throw new BusinessException("Can not send mail.");
            }
            purchaseOrdersRepository.saveAndFlush(purchaseOrdersNew);
            //username is supplier can send mail
            User userVendor = userEmailsSupplier.get();
            if (userId.equals(userVendor.getLogin())) {
                String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?type=DI", purchaseOrders.getPoNumber(), supplier, "The purchase order", "adjusted", "Adjusted");
                List<String> listMailPU = getListUserPU(userVendor);
                List<String> listMailCC = new ArrayList<>(listMailPU);
                String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), userVendor);
                sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
            }
            return mappingEntityToDto(purchaseOrdersNew, PurchaseOrdersMainDTO.class);
        }
        return null;
    }

    @Override
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO request) {
        //create new purchaser order
        try {
            PurchaseOrders purchaseOrders = new PurchaseOrders();
            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findByPoNumberAndIsDeleted(request.getPoNumber(), false);
            if (oPurchaseOrders.isPresent()) {
                throw new BusinessException("PO Number already in another Purchase Order");
            }
            request.setId(null);
            purchaseOrders.setCreatedBy(request.getCreatedBy());
            purchaseOrders.setCreatedDate(new Date().toInstant());
            BeanUtils.copyProperties(request, purchaseOrders);
            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEW);
            purchaseOrders.setIsSendmail(false);
            //define Map check data duplicate
            List<String> checkDataMap = new ArrayList<>();
            Set<PurchaseOrdersDetail> purchaseOrdersDetailSet = request.getPurchaseOrdersDetail().stream().map(i -> {
                PurchaseOrdersDetail purchaseOrdersDetail = new PurchaseOrdersDetail();
                String key = i.getAsin() + "_" + i.getFromSo();
                if (checkDataMap.contains(key)) {
                    throw new BusinessException(String.format("Can not create Purchase Order with duplicate {ASin= %s ,FromSo= %s }", i.getAsin(), i.getFromSo()));
                } else {
                    checkDataMap.add(key);
                }
                Optional<PurchaseOrdersDetail> purchaseOrdersDetailExists = purchaseOrdersDetailRepository.findByFromSoAndAsinAndIsDeleted(purchaseOrdersDetail.getFromSo(), purchaseOrdersDetail.getAsin(), false);
                if (purchaseOrdersDetailExists.isPresent()) {
                    throw new BusinessException(String.format("{ASin= %s ,FromSo= %s } already exists in another Purchase Order.", purchaseOrdersDetail.getAsin(), purchaseOrdersDetail.getFromSo()));
                }
                BeanUtils.copyProperties(i, purchaseOrdersDetail);
                return purchaseOrdersDetail;
            }).collect(Collectors.toSet());

            purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetailSet);
            purchaseOrders.setTotalItem(purchaseOrdersDetailSet.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum));
            purchaseOrders.setTotalCost(purchaseOrdersDetailSet.stream().filter(k -> !k.getIsDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
            return mappingEntityToDto(purchaseOrders, PurchaseOrderDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public Page<PurchaseOrdersMainDTO> listingPurchaseOrdersWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<PurchaseOrders> data = purchaseOrdersRepository.findByCondition(
            filterParams.get("poNumber"),
            filterParams.get("poAmazon"), filterParams.get("bookingNumber"),
            filterParams.get("updatedBy"), filterParams.get("country"),
            filterParams.get("fulfillmentCenter"), filterParams.get("status"),
            filterParams.get("supplier"), filterParams.get("supplierSearch"),
            filterParams.get("updatedDateFrom"), filterParams.get("updatedDateTo"),
            filterParams.get("expectedShipDateFrom"), filterParams.get("expectedShipDateTo"),
            filterParams.get("actualShipDateFrom"), filterParams.get("actualShipDateTo"),
            filterParams.get("etdFrom"), filterParams.get("etdTo"),
            filterParams.get("etaFrom"), filterParams.get("etaTo"),
            filterParams.get("atdFrom"), filterParams.get("atdTo"),
            filterParams.get("ataFrom"), filterParams.get("ataTo"),
            filterParams.get("deadlineSubmitBookingFrom"), filterParams.get("deadlineSubmitBookingTo"),
            pageable);
        return data.map(item -> mappingEntityToDto(item, PurchaseOrdersMainDTO.class));
    }

    public ListDetailPODTO getListSkuFromPO(List<Integer> id) {
        try {
            ListDetailPODTO listDetailPODTO = new ListDetailPODTO();
            final String[] purchaseOrderNo = {""};
            final Integer[] purchaseOrderId = {null};
            Map<String, Set<ProformaInvoiceDetailDTO>> detailDTO = new HashMap<>();
            final double[] totalAmount = {0};
            final Set[] detailPISet = new Set[]{new HashSet<>()};

            id.forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrders.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrders.get();
                    listDetailPODTO.setFulfillmentCenter(purchaseOrders.getFulfillmentCenter());
                    listDetailPODTO.setShipDate(purchaseOrders.getExpectedShipDate());
                    listDetailPODTO.setVendorCode(purchaseOrders.getVendorCode());
                    purchaseOrderNo[0] = purchaseOrders.getPoNumber();
                    purchaseOrderId[0] = purchaseOrders.getId();
                    Set<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrders.getPurchaseOrdersDetail();
                    detailPISet[0] = purchaseOrdersDetail.stream().filter(k -> !k.getIsDeleted()).map(item -> {
                        ProformaInvoiceDetailDTO proformaInvoiceDetailDTO = new ProformaInvoiceDetailDTO();
                        proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetailDTO.class);
                        proformaInvoiceDetailDTO.setId(null);
                        proformaInvoiceDetailDTO.setKey(proformaInvoiceDetailDTO.getAsin() + "@" + proformaInvoiceDetailDTO.getFromSo());
                        totalAmount[0] += item.getAmount();
                        return proformaInvoiceDetailDTO;
                    }).collect(Collectors.toSet());
                    detailDTO.put("detail_0", detailPISet[0]);
//                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(purchaseOrders.getVendorId());
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

    private <T> T mappingEntityToDto(PurchaseOrders purchaseOrders, Class<T> clazz) {
        try {

            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(purchaseOrders, dto);
            ProformaInvoice proformaInvoice = purchaseOrders.getProformaInvoice();
            Integer proformaInvoiceId = null;
            Integer commercialInvoiceId = null;
            String proformaInvoiceNo = null;
            String commercialInvoiceNo = null;
            final String[] bookingNumber = {null};
            final Integer[] bookingId = {null};
            if (proformaInvoice != null) {
                proformaInvoiceId = proformaInvoice.getId();
                proformaInvoiceNo = proformaInvoice.getOrderNo();
                final CommercialInvoice[] commercialInvoice = {new CommercialInvoice()};
                List<BookingProformaInvoice> bookingProformaInvoiceList = bookingProformaInvoiceRepository.findAllByProformaInvoiceNo(proformaInvoice.getOrderNo());
                bookingProformaInvoiceList.stream().filter(k -> !Objects.equals(k.getBooking().getStatus(), STATUS_BOOKING_CANCEL)).forEach(bookingProformaInvoice -> {
                    if (bookingProformaInvoice.getBookingPackingList() != null) {
                        if (bookingProformaInvoice.getBookingPackingList().getCommercialInvoice() != null) {
                            commercialInvoice[0] = bookingProformaInvoice.getBookingPackingList().getCommercialInvoice();
                        }
                        if (!Objects.equals(bookingProformaInvoice.getBooking().getStatus(), STATUS_BOOKING_CANCEL) && !Objects.equals(bookingProformaInvoice.getBooking().getStatus(), STATUS_BOOKING_UPLOAD)) {
                            bookingNumber[0] = bookingProformaInvoice.getBooking().getBookingConfirmation();
                            bookingId[0] = bookingProformaInvoice.getBooking().getId();
                        }
                    }
                });
                if (commercialInvoice[0] != null) {
                    commercialInvoiceId = commercialInvoice[0].getId();
                    commercialInvoiceNo = commercialInvoice[0].getInvoiceNo();
                }
            }
            if (dto instanceof PurchaseOrdersMainDTO) {
                Optional<User> oUser = userRepository.findOneByLogin(purchaseOrders.getCreatedBy());
                String createdBy = "";
                if (oUser.isPresent()) {
                    createdBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                }

                String fromSoStr = purchaseOrders.getPurchaseOrdersDetail().stream().map(PurchaseOrdersDetail::getFromSo).distinct().collect(Collectors.joining(", "));
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setCreatedBy", String.class).invoke(dto, createdBy);
                clazz.getMethod("setProformaInvoiceId", Integer.class).invoke(dto, proformaInvoiceId);
                clazz.getMethod("setCommercialInvoiceId", Integer.class).invoke(dto, commercialInvoiceId);
                clazz.getMethod("setProformaInvoiceNo", String.class).invoke(dto, proformaInvoiceNo);
                clazz.getMethod("setCommercialInvoiceNo", String.class).invoke(dto, commercialInvoiceNo);
                clazz.getMethod("setBookingNumber", String.class).invoke(dto, bookingNumber[0]);
                clazz.getMethod("setBookingId", Integer.class).invoke(dto, bookingId[0]);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    private <T> T mappingEntityToDtoPI(ProformaInvoice proformaInvoice, Class<T> clazz) {
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
            Set<ProformaInvoiceDetail> detailSet = objectModel.getProformaInvoiceDetail().stream().filter(i -> !i.isDeleted()).collect(Collectors.toSet());
            List<BookingProformaInvoice> bookingProformaInvoiceList = bookingProformaInvoiceRepository.findAllByProformaInvoiceNo(proformaInvoice.getOrderNo());
            bookingProformaInvoiceList.stream().forEach(bookingProformaInvoice -> {
                if (bookingProformaInvoice.getBookingPackingList() != null && (bookingProformaInvoice.getBooking().getStatus() != GlobalConstant.STATUS_BOOKING_CANCEL)) {
                    bookingNumber[0] = bookingProformaInvoice.getBooking().getBookingConfirmation();
                    bookingId[0] = bookingProformaInvoice.getBooking().getId();

                }
            });
            String fromSoStr = detailSet.stream().map(i -> i.getFromSo()).distinct().collect(Collectors.joining(", "));
            Optional<User> oUser = userRepository.findOneByLogin(proformaInvoice.getUpdatedBy());
            String updatedBy = "";
            if (oUser.isPresent()) {
                updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
            }

            if (dto instanceof ProformaInvoiceDTO) {
                Map<String, List<ProformaInvoiceDetailDTO>> detailDTO = new HashMap<>();
                detailSet.parallelStream().forEach(item -> {
                    ProformaInvoiceDetailDTO proformaInvoiceDetailDTO;
                    proformaInvoiceDetailDTO = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetailDTO.class);
                    List<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOSet = new ArrayList<>();
                    if (detailDTO.get(TITLE_HASHMAP + item.getCdcVersion()) != null) {
                        proformaInvoiceDetailDTOSet = detailDTO.get(TITLE_HASHMAP + item.getCdcVersion());
                    }
                    proformaInvoiceDetailDTOSet.add(proformaInvoiceDetailDTO);
                    detailDTO.put(TITLE_HASHMAP + item.getCdcVersion(), proformaInvoiceDetailDTOSet);
                });
                clazz.getMethod("setProformaInvoiceDetail", Map.class).invoke(dto, detailDTO);
            } else if (dto instanceof ProformaInvoiceMainDTO) {
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setBookingNumber", String.class).invoke(dto, bookingNumber[0]);
                clazz.getMethod("setBookingId", Integer.class).invoke(dto, bookingId[0]);
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
        createCell(sheet, row, 0, "PO number", style);
        createCell(sheet, row, 1, "SKU", style);
        createCell(sheet, row, 2, "ASIN", style);
        createCell(sheet, row, 3, "Product Name", style);
        createCell(sheet, row, 4, "Quantity Ordered", style);
        createCell(sheet, row, 5, "Unit Price", style);
        createCell(sheet, row, 6, "Amount", style);
        createCell(sheet, row, 7, "PCS/CARTON", style);
        createCell(sheet, row, 8, "TOTAL BOX", style);
        createCell(sheet, row, 9, "TOTAL VOLUME (CBM)", style);
        createCell(sheet, row, 10, "TOTAL NET WEIGHT", style);
        createCell(sheet, row, 11, "TOTAL GROSS WEIGHT", style);
        createCell(sheet, row, 12, "MAKE-TO-STOCK", style);

    }


    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) {
        try {

            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(id);
            PurchaseOrders purchaseOrders;
            if (!oPurchaseOrders.isPresent()) {
                throw new BusinessException("Purchase Order not found.");
            }
            purchaseOrders = oPurchaseOrders.get();
            XSSFSheet sheet = workbook.createSheet(purchaseOrders.getPoNumber());
            int rowCount = 1;
            writeHeaderLine(workbook, sheet);
            CellStyle style = workbook.createCellStyle();
            XSSFCellStyle styleDate = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
            Set<PurchaseOrdersDetail> purchaseOrdersDetailSet = purchaseOrders.getPurchaseOrdersDetail().stream().filter(k -> !k.getIsDeleted()).collect(Collectors.toSet());
            for (PurchaseOrdersDetail detail : purchaseOrdersDetailSet) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(sheet, row, columnCount++, detail.getFromSo(), style);
                createCell(sheet, row, columnCount++, detail.getSku(), style);
                createCell(sheet, row, columnCount++, detail.getAsin(), style);
                createCell(sheet, row, columnCount++, detail.getProductName(), style);
                createCell(sheet, row, columnCount++, detail.getQty(), style);
                createCell(sheet, row, columnCount++, detail.getUnitPrice(), style);
                createCell(sheet, row, columnCount++, detail.getAmount(), style);
                createCell(sheet, row, columnCount++, detail.getPcs(), style);
                createCell(sheet, row, columnCount++, detail.getTotalBox(), style);
                createCell(sheet, row, columnCount++, detail.getTotalVolume(), style);
                createCell(sheet, row, columnCount++, detail.getGrossWeight(), style);
                createCell(sheet, row, columnCount++, detail.getNetWeight(), style);
                createCell(sheet, row, columnCount, detail.getMakeToStock(), style);
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
    public boolean updateShipDate(Integer id, String date, String userId) {
        try {
            Optional<PurchaseOrders> optPurchaseOrders = purchaseOrdersRepository.findByIdAndActive(id, GlobalConstant.STATUS_PO_CANCEL);
            if (optPurchaseOrders.isPresent()) {
                PurchaseOrders purchaseOrders = optPurchaseOrders.get();
                LocalDate dateAfter = DateUtils.convertStringLocalDateBooking(date);
                purchaseOrders.setExpectedShipDate(dateAfter);

                Set<PurchaseOrdersDate> setPurchaseOrdersDate = purchaseOrders.getPurchaseOrdersDate();
                PurchaseOrdersDate newestOrderDateBefore = getNewestOrderDateBefore(setPurchaseOrdersDate, GlobalConstant.PO_SHIP_DATE);
                if (newestOrderDateBefore != null) {
                    LocalDate dateBefore = newestOrderDateBefore.getDateAfter();
                    if (!dateBefore.isEqual(dateAfter)) {
                        PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                        purchaseOrdersDate.setDateBefore(dateBefore);
                        purchaseOrdersDate.setDateAfter(dateAfter);
                        purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_DATE);
                        purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                        purchaseOrdersDate.setCreatedBy(newestOrderDateBefore.getCreatedBy());
                        purchaseOrdersDate.setUpdatedBy(userId);
                        setPurchaseOrdersDate.add(purchaseOrdersDate);
                        purchaseOrders.setPurchaseOrdersDate(setPurchaseOrdersDate);
                    } else {
                        throw new BusinessException("The ship date is the same day before and after the update.");
                    }
                } else {
                    PurchaseOrdersDate purchaseOrdersDate = new PurchaseOrdersDate();
                    purchaseOrdersDate.setDateBefore(null);
                    purchaseOrdersDate.setDateAfter(dateAfter);
                    purchaseOrdersDate.setTypeDate(GlobalConstant.PO_SHIP_DATE);
                    purchaseOrdersDate.setPurchaseOrders(purchaseOrders);
                    purchaseOrdersDate.setCreatedBy(userId);
                    purchaseOrdersDate.setUpdatedBy(userId);
                    setPurchaseOrdersDate.add(purchaseOrdersDate);
                    purchaseOrders.setPurchaseOrdersDate(setPurchaseOrdersDate);
                }

                ProformaInvoice proformaInvoice = purchaseOrders.getProformaInvoice();
                if (CommonDataUtil.isNotNull(proformaInvoice)) {
                    // ProformaInvoice
                    proformaInvoice.setShipDate(dateAfter);
                    purchaseOrders.setProformaInvoice(proformaInvoice);

                    // BookingProformaInvoice
                    List<BookingProformaInvoice> listBookingProformaInvoice = bookingProformaInvoiceRepository.findAllByProformaInvoiceNo(proformaInvoice.getOrderNo());
                    listBookingProformaInvoice.forEach(item -> {
                        item.setShipDate(dateAfter);
                        bookingProformaInvoiceRepository.saveAndFlush(item);
                    });
                }
                // Update
                purchaseOrdersRepository.save(purchaseOrders);

                if (!GlobalConstant.STATUS_PO_NEW.equals(purchaseOrders.getStatus())) {
                    // List email Y4A
                    List<String> listEmailCC = getListMailY4A();
                    //list mail cc
                    List<String> listMailCC = new ArrayList<>();
                    // List email Supplier
                    List<String> listMailReceive = new ArrayList<>();
                    Optional<User> optUser = userRepository.findOneByVendor(purchaseOrders.getVendorId());
                    if (optUser.isPresent()) {
                        listMailReceive.add(optUser.get().getEmail());
                        List<String> listMailPU = getListUserPU(optUser.get());
                        listMailCC.addAll(listMailPU);
                    } else {
                        throw new BusinessException("Can not find user Vendor");
                    }

                    // Send mail
                    if (CommonDataUtil.isNotEmpty(listMailReceive)) {
                        String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), optUser.get());
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?type=DI", purchaseOrders.getPoNumber(),
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

    private List<LogUpdateDateDTO> getHistoryDate(Set<PurchaseOrdersDate> purchaseOrdersDateSet, String typeDate) {
        List<LogUpdateDateDTO> result;
        result = purchaseOrdersDateSet.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate()))
            .sorted(Comparator.comparing(PurchaseOrdersDate::getCreatedDate).reversed())
            .map(item -> {
                User user = userRepository.findOneByLogin(item.getUpdatedBy()).orElse(null);
                String updateBy = CommonDataUtil.getUserFullName(user);

                LogUpdateDateDTO dto = new LogUpdateDateDTO();
                BeanUtils.copyProperties(item, dto);
                dto.setUpdatedBy(updateBy);
                return dto;
            })
            .collect(Collectors.toList());
        return result;
    }

    private PurchaseOrdersDate getNewestOrderDateBefore(Set<PurchaseOrdersDate> purchaseOrdersDateSet, String typeDate) {
        return purchaseOrdersDateSet.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate()))
            .sorted(Comparator.comparing(PurchaseOrdersDate::getCreatedDate).reversed())
            .findFirst().orElse(null);
    }

    private List<String> getListMailY4A() {
        List<String> listEmail = new ArrayList<>();
        List<User> listEmailYes4all = userRepository.findAllByIsYes4all(true);
        if (CommonDataUtil.isNotEmpty(listEmailYes4all)) {
            listEmail.addAll(listEmailYes4all.stream().map(User::getEmail).collect(Collectors.toList()));
        }
        return listEmail;
    }

}
