package com.yes4all.service.impl;

import com.yes4all.common.enums.*;
import com.yes4all.common.errors.BusinessException;


import com.yes4all.common.utils.*;
import com.yes4all.constants.GlobalConstant;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.PageRequestUtil;


import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.*;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.yes4all.common.constants.Constant.*;
import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.constants.GlobalConstant.*;
import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;


/**
 * Service Implementation for managing {@link CommercialInvoice}.
 */
@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private static final String LINK_DETAIL_SHIPMENT = "/shipment/detail/";
    private final Logger log = LoggerFactory.getLogger(ShipmentServiceImpl.class);

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ShipmentsPurchaseOrdersRepository shipmentsPurchaseOrdersRepository;

    @Autowired
    private ShipmentsContainerPalletRepository shipmentsContainerPalletRepository;
    @Autowired
    private CommercialInvoiceWHService commercialInvoiceWHService;

    @Autowired
    private ShipmentsContainersRepository shipmentsContainersRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    private static final String LINK_DETAIL_BOL = "/bill-of-lading/detail/";

    @Value("${attribute.link.url}")
    private String linkPOMS;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private PurchaseOrdersWHRepository purchaseOrdersWHRepository;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private ProformaInvoiceWHRepository proformaInvoiceWHRepository;

    @Autowired
    private ProformaInvoiceWHDetailRepository proformaInvoiceWHDetailRepository;
    @Autowired
    private ShipmentPackingListRepository shipmentPackingListRepository;

    @Autowired
    private ShipmentsProformaInvoicePKLRepository shipmentsProformaInvoicePKLRepository;
    @Autowired
    private WareHouseRepository wareHouseRepository;
    @Autowired
    private PortsRepository portsRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private LogServiceImpl logServiceImpl;

    @Autowired
    private CommercialInvoiceWHRepository commercialInvoiceWHRepository;

    @Override
    public Page<ShipmentMainDTO> listingWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        try {
            Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
            Page<Shipment> data = shipmentRepository.findByCondition(
                filterParams.get("shipmentId"), filterParams.get("status")
                , filterParams.get("poNumber"), filterParams.get("etdFrom"), filterParams.get("etdTo")
                , filterParams.get("etaFrom"), filterParams.get("etaTo"), filterParams.get("atdFrom"), filterParams.get("atdTo")
                , filterParams.get("ataFrom"), filterParams.get("ataTo"), filterParams.get("createdBy")
                , filterParams.get("updatedBy"), filterParams.get("createdDateFrom"), filterParams.get("createdDateTo")
                , filterParams.get("updatedDateFrom"), filterParams.get("updatedDateTo")
                , pageable);
            return data.map(item -> mappingEntityToDto(item, ShipmentMainDTO.class));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public List<IShipmentProformaInvoiceDTO> searchPI(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (supplier != null && supplier.trim().length() > 0 && request.getId() != null) {
                    return shipmentRepository.findAllProformaInvoiceByCondition(supplier, request.getId() == null ? 0 : request.getId());
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean confirmPKL(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    Set<String> authorities = userService.checkUserWithRole(oUser.get());
                    if (!authorities.contains(POMS_USER)) {
                        throw new BusinessException("The only Purchasing must be confirm Packing List");
                    }
                    Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
                    if (oShipmentsPackingList.isPresent()) {
                        ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                        if (!shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_SUBMIT)) {
                            throw new BusinessException("The status not valid.");
                        }
                        shipmentsPackingList.setStatus(STATUS_PACKING_LIST_WH_CONFIRMED);
                        Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKLSet = shipmentsPackingList.getShipmentProformaInvoicePKL();
                        Optional<Shipment> oShipment = shipmentProformaInvoicePKLSet.parallelStream().map(ShipmentProformaInvoicePKL::getShipment).findFirst();
                        Shipment shipment = oShipment.get();
                        //get all packing list status not confirmed to compare
                        if (shipment.getShipmentProformaInvoicePKL().parallelStream().noneMatch(i -> !i.getShipmentsPackingList().getId().equals(shipmentsPackingList.getId())
                            && !i.getShipmentsPackingList().getStatus().equals(STATUS_PACKING_LIST_WH_CONFIRMED))) {
                            shipment.getShipmentProformaInvoicePKL().parallelStream().forEach(item -> {
                                Optional<ProformaInvoiceWH> oProformaInvoiceWH = proformaInvoiceWHRepository.findByOrderNo(item.getProformaInvoiceNo());
                                PurchaseOrdersWH purchaseOrdersWH = oProformaInvoiceWH.get().getPurchaseOrdersWH();
                                purchaseOrdersWH.setStatus(STATUS_PO_WH_PIPELINE);
                                purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                            });
                            shipment.getShipmentsContainers().parallelStream().forEach(i -> {
                                i.setStatus(STATUS_CONTAINER_PIPELINE);
                                shipmentsContainersRepository.saveAndFlush(i);
                            });
                            shipment.setStatus(STATUS_SHIPMENT_PIPELINE);
                            shipmentRepository.saveAndFlush(shipment);
                        }
                        return true;
                    } else {
                        throw new BusinessException("Can not find Shipment.");
                    }
                } else {
                    throw new BusinessException("The only Purchasing must be confirm Packing List");
                }
            } else {
                throw new BusinessException("Can not find user.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public boolean rejectPKL(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    Set<String> authorities = userService.checkUserWithRole(oUser.get());
                    if (!authorities.contains(POMS_USER)) {
                        throw new BusinessException("The only Purchasing must be confirm Packing List");
                    }
                    Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
                    if (oShipmentsPackingList.isPresent()) {
                        ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                        if (!shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_SUBMIT)) {
                            throw new BusinessException("The status not valid.");
                        }
                        shipmentsPackingList.setStatus(STATUS_PACKING_LIST_WH_REJECTED);
                        Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKLSet = shipmentsPackingList.getShipmentProformaInvoicePKL();
                        Optional<Shipment> oShipment = shipmentProformaInvoicePKLSet.parallelStream().map(ShipmentProformaInvoicePKL::getShipment).findFirst();
                        Shipment shipment = oShipment.get();
                        //revert status all
                        shipment.getShipmentProformaInvoicePKL().parallelStream().forEach(item -> {
                            Optional<ProformaInvoiceWH> oProformaInvoiceWH = proformaInvoiceWHRepository.findByOrderNo(item.getProformaInvoiceNo());
                            PurchaseOrdersWH purchaseOrdersWH = oProformaInvoiceWH.get().getPurchaseOrdersWH();
                            purchaseOrdersWH.setStatus(STATUS_PO_WH_PI_CI_ADJUSTING);
                            purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                        });
                        shipment.getShipmentsContainers().parallelStream().forEach(i -> {
                            i.setStatus(STATUS_CONTAINER_ADJUSTING);
                            shipmentsContainersRepository.saveAndFlush(i);
                        });
                        shipment.setStatus(STATUS_SHIPMENT_REVIEW_PL_CI);
                        shipmentRepository.saveAndFlush(shipment);
                        return true;
                    } else {
                        throw new BusinessException("Can not find Shipment.");
                    }
                } else {
                    throw new BusinessException("The only Purchasing must be confirm Packing List");
                }
            } else {
                throw new BusinessException("Can not find user.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public boolean confirmUSBroker(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    Set<String> authorities = userService.checkUserWithRole(oUser.get());
                    if (!authorities.contains(POMS_BROKER)) {
                        throw new BusinessException("The only US Broker must be confirm.");
                    }
                    Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
                    if (oShipment.isPresent()) {
                        Shipment shipment = oShipment.get();
                        if (!shipment.getStatus().equals(STATUS_SHIPMENT_REVIEW_BROKER)) {
                            throw new BusinessException("The status not valid.");
                        }
                        shipment.setStatus(STATUS_SHIPMENT_CONFIRMED_BROKER);
                        shipment.setStatusUsBroker(STATUS_SHIPMENT_US_BROKER_CONFIRMED);
                        shipmentRepository.saveAndFlush(shipment);
                        return true;
                    } else {
                        throw new BusinessException("Can not find Shipment.");
                    }
                } else {
                    throw new BusinessException("The only US Broker must be confirm.");
                }
            } else {
                throw new BusinessException("Can not find user.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean rejectUSBroker(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    Set<String> authorities = userService.checkUserWithRole(oUser.get());
                    if (!authorities.contains(POMS_LOCAL_BROKER)) {
                        throw new BusinessException("The only US Broker must be reject.");
                    }
                    Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
                    if (oShipment.isPresent()) {
                        Shipment shipment = oShipment.get();
                        if (!shipment.getStatus().equals(STATUS_SHIPMENT_REVIEW_BROKER)) {
                            throw new BusinessException("The status not valid.");
                        }
                        shipment.setStatus(STATUS_SHIPMENT_REVIEW_BROKER);
                        shipment.setStatusUsBroker(STATUS_SHIPMENT_US_BROKER_REJECTED);
                        shipmentRepository.saveAndFlush(shipment);
                        return true;
                    } else {
                        throw new BusinessException("Can not find Shipment.");
                    }
                } else {
                    throw new BusinessException("The only US Broker must be confirm.");
                }
            } else {
                throw new BusinessException("Can not find user.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean sendRequest(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    Set<String> authorities = userService.checkUserWithRole(oUser.get());
                    if (!authorities.contains(POMS_LOCAL_BROKER)) {
                        throw new BusinessException("The only US Broker must be reject.");
                    }
                    Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
                    if (oShipment.isPresent()) {
                        Shipment shipment = oShipment.get();
                        if (!shipment.getIsRequest()) {
                            throw new BusinessException("You haven't ticked button isRequest so you can't send.");
                        }

                        Set<String> listMailCC = new HashSet<>();
                        shipment.getShipmentsPurchaseOrders().parallelStream().forEach(item -> {
                            Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(item.getPurchaseOrderId());
                            if (oPurchaseOrdersWH.isEmpty()) {
                                throw new BusinessException("Can not find purchase order.");
                            }
                            PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                            Optional<User> oUserSupplier = userRepository.findOneByVendor(purchaseOrdersWH.getVendorId());
                            User user;
                            if (oUserSupplier.isEmpty()) {
                                throw new BusinessException("Can not find user supplier.");
                            }
                            user = oUserSupplier.get();
                            List<String> listMailSC = userService.getListUserSC(user);
                            List<String> listMailPU = userService.getListUserPU(user);
                            listMailCC.addAll(listMailSC);
                            listMailCC.addAll(listMailPU);
                        });
                        List<User> userLogistics = userRepository.findAllWithRole(POMS_LOGISTIC);
                        if (userLogistics.isEmpty()) {
                            throw new BusinessException("Can not find user logistics.");
                        }
                        List<String> listMail = new ArrayList<>(userLogistics.parallelStream().map(User::getEmail).distinct().collect(Collectors.toList()));
                        String poNumber = shipment.getShipmentsPurchaseOrders().parallelStream().map(ShipmentsPurchaseOrders::getPurchaseOrderNo).collect(Collectors.joining(", "));
                        String subject = "" + shipment.getShipmentId() + " Shipment Process - ETD: " + shipment.getEtd() + "";
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_SHIPMENT + shipment.getId(), shipment.getShipmentId() + "@" + poNumber, "", "Shipment", "sendRequest", SEND_REQUEST_SHIPMENT);
                        sendMailService.sendMail(subject, content, listMail, new ArrayList<>(listMailCC), null, null);
                        return true;
                    } else {
                        throw new BusinessException("Can not find Shipment.");
                    }
                } else {
                    throw new BusinessException("The only US Broker must be confirm.");
                }
            } else {
                throw new BusinessException("Can not find user.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean actionInbound(RequestInboundDTO request) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findShipmentWithContainerId(request.getContainerId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                Set<ShipmentsContainers> shipmentsContainers = shipment.getShipmentsContainers();
                if (request.getAction().equals("CREATE")) {
                    shipmentsContainers = shipmentsContainers.parallelStream().map(i -> {
                        if (i.getId().equals(request.getContainerId())) {
                            if (i.getStatus().equals(STATUS_CONTAINER_PIPELINE)) {
                                i.setStatus(STATUS_CONTAINER_IMPORTING);
                            } else {
                                throw new BusinessException("The status not valid.");
                            }
                        }
                        return i;
                    }).collect(Collectors.toSet());
                    shipment.setStatus(STATUS_SHIPMENT_IMPORTING);
                } else if (request.getAction().equals("CANCEL")) {
                    shipmentsContainers = shipmentsContainers.parallelStream().map(i -> {
                        if (i.getId().equals(request.getContainerId())) {
                            i.setStatus(STATUS_CONTAINER_PIPELINE);
                        }
                        return i;
                    }).collect(Collectors.toSet());
                } else if (request.getAction().equals("COMPLETE")) {
                    final int[] countContainerComplete = {0};
                    shipmentsContainers = shipmentsContainers.parallelStream().map(i -> {
                        if (i.getId().equals(request.getContainerId())) {
                            if (i.getStatus().equals(STATUS_CONTAINER_IMPORTING)) {
                                i.setStatus(STATUS_CONTAINER_COMPLETED);
                            } else {
                                throw new BusinessException("The status not valid.");
                            }
                            Map<String, String> skuPONoMap = request.getShipmentContainerDetailToIMSDTOS().stream()
                                .collect(Collectors.toMap(
                                    scd -> scd.getSku() + scd.getPurchaseOrderNo(),
                                    ShipmentContainerDetailToIMSDTO::getReceivedQuantity // Change getSomeValue to the actual field you want to use for updating
                                ));
                            List<ShipmentsContainersDetail> shipmentsContainersDetails = i.getShipmentsContainersDetail().stream().map(scd -> {
                                String key = scd.getSku() + scd.getProformaInvoiceNo();
                                if (skuPONoMap.containsKey(key)) {
                                    scd.setImportedQuantity(Integer.valueOf(skuPONoMap.get(key)));
                                } else {
                                    throw new BusinessException("Missing data.");
                                }
                                return scd;
                            }).collect(Collectors.toList());
                            i.setShipmentsContainersDetail(shipmentsContainersDetails);
                        }
                        if (i.getStatus().equals(STATUS_CONTAINER_COMPLETED)) {
                            countContainerComplete[0]++;
                        }
                        return i;
                    }).collect(Collectors.toSet());
                    if (countContainerComplete[0] == (shipmentsContainers.size())) {
                        shipment.setStatus(STATUS_SHIPMENT_COMPLETED);
                    }
                    Set<Integer> proformaInvoiceList = shipmentsContainers.parallelStream().flatMap(i -> i.getShipmentsContainersDetail().parallelStream().map(ShipmentsContainersDetail::getProformaInvoiceId)).collect(Collectors.toSet());
                    proformaInvoiceList.parallelStream().forEach(id -> {
                        Optional<ProformaInvoiceWH> oProformaInvoiceWH = proformaInvoiceWHRepository.findById(id);
                        if (oProformaInvoiceWH.isPresent()) {
                            PurchaseOrdersWH purchaseOrdersWH = oProformaInvoiceWH.get().getPurchaseOrdersWH();
                            if (shipment.getStatus().equals(STATUS_SHIPMENT_COMPLETED)) {
                                purchaseOrdersWH.setStatus(STATUS_PO_WH_IMPORTING);
                            } else {
                                purchaseOrdersWH.setStatus(STATUS_PO_WH_IMPORTED);
                            }
                            purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                        } else {
                            throw new BusinessException("Can not find Purchaser Order.");
                        }
                    });
                } else {
                    throw new BusinessException("The action not valid.");
                }
                logServiceImpl.writeLog(EnumLogFunctionDescription.IMS_TO_CONTAINER.getCode(), EnumLogFunctionCode.CONTAINER.getCode(),
                    request.getContainerId() + "", request, EnumLogProject.IMS.getCode(), request.getAction());
                shipment.setShipmentsContainers(shipmentsContainers);
                shipmentRepository.saveAndFlush(shipment);
                return true;
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public List<IShipmentPurchaseOrdersDTO> searchPO(ListSearchALLPOShipmentDTO request) {
        if (request.getIsSearch() == 1) {
            if (request.getInvoiceNo().trim().length() > 0 || request.getDemand().trim().length() > 0 || request.getContainer().trim().length() > 0 ||
                request.getSupplierSearch().trim().length() > 0 || request.getPortOfLoading().trim().length() > 0 || request.getPortOfDischarge().trim().length() > 0 ||
                request.getEtdShipment().trim().length() > 0 || request.getEtaShipment().trim().length() > 0) {
                return shipmentRepository.findAllPurchaseOrderByCondition(request.getInvoiceNo(), request.getDemand(), request.getContainer()
                    , request.getSupplierSearch(), request.getPortOfLoading(), request.getPortOfDischarge(), request.getEtdShipment(), request.getEtaShipment(), request.getId() == null ? 0 : request.getId());

            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public ShipmentsPurchaseOrdersMainDTO searchPODetail(ListSearchPOShipmentDTO request) {
        ShipmentsPurchaseOrdersMainDTO shipmentsPurchaseOrdersMainDTO = new ShipmentsPurchaseOrdersMainDTO();
        List<ShipmentsPurchaseOrdersDTO> shipmentsPurchaseOrdersDTOList = new ArrayList<>();
        List<ListContainerDTO> containerDTOS = new ArrayList<>();
        Set<String> consolidators = new HashSet<>();
        try {
            request.getSearch().forEach(search -> {
                ShipmentsPurchaseOrdersDTO shipmentsPurchaseOrdersDTO = new ShipmentsPurchaseOrdersDTO();
                Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(search.getPurchaseOrderId());
                shipmentsPurchaseOrdersDTO.setId(search.getId());
                if (oPurchaseOrdersWH.isPresent()) {
                    PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                    consolidators.add(purchaseOrdersWH.getVendorId());
                    shipmentsPurchaseOrdersDTO.setPurchaseOrderNo(purchaseOrdersWH.getPoNumber());
                    shipmentsPurchaseOrdersDTO.setPurchaseOrderId(purchaseOrdersWH.getId());
                    List<ShipmentsPurchaseOrdersDetailDTO> shipmentsPurchaseOrdersDetailDTOS = new ArrayList<>();
                    ProformaInvoiceWH proformaInvoiceWH = purchaseOrdersWH.getProformaInvoiceWH();
                    Optional<ProformaInvoiceWHDetail> oProformaInvoiceDetail = proformaInvoiceWHDetailRepository.findTop1CdcVersionByProformaInvoiceWHOrderByCdcVersionDesc(proformaInvoiceWH);
                    if (oProformaInvoiceDetail.isEmpty()) {
                        throw new BusinessException("Can not find new version detail proforma invoice");
                    }
                    //get new cdcVersion detail
                    Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();
                    proformaInvoiceWH.getProformaInvoiceWHDetail().stream().filter(i -> i.getCdcVersion().equals(cdcVersionMax)).forEach(element -> {
                        ShipmentsPurchaseOrdersDetailDTO shipmentsPurchaseOrdersDetailDTO = new ShipmentsPurchaseOrdersDetailDTO();
                        shipmentsPurchaseOrdersDetailDTO.setAmount(element.getAmount());
                        shipmentsPurchaseOrdersDetailDTO.setSku(element.getSku());
                        shipmentsPurchaseOrdersDetailDTO.setProductName(element.getProductName());
                        shipmentsPurchaseOrdersDetailDTO.setQuantity(element.getQty());
                        shipmentsPurchaseOrdersDetailDTO.setUnitPrice(element.getUnitPrice());
                        shipmentsPurchaseOrdersDetailDTO.setTotalVolume(element.getTotalVolume());
                        shipmentsPurchaseOrdersDetailDTO.setGrossWeight(element.getGrossWeight());
                        shipmentsPurchaseOrdersDetailDTO.setNetWeight(element.getNetWeight());
                        shipmentsPurchaseOrdersDetailDTOS.add(shipmentsPurchaseOrdersDetailDTO);

                    });
                    shipmentsPurchaseOrdersDTO.setShipmentsPurchaseOrdersDetail(shipmentsPurchaseOrdersDetailDTOS);
                    purchaseOrdersWH.getPurchaseOrdersWHDetail().parallelStream().forEach(i -> {
                        ListContainerDTO listContainerDTO = new ListContainerDTO();
                        listContainerDTO.setContainerNo(i.getContainerNo());
                        listContainerDTO.setContainerType(i.getContainerType());
                        containerDTOS.add(listContainerDTO);
                    });
                }
                shipmentsPurchaseOrdersDTOList.add(shipmentsPurchaseOrdersDTO);
            });
            Map<String, Long> distinctCounts = containerDTOS.stream()
                .collect(Collectors.groupingBy(ListContainerDTO::getContainerType, Collectors.mapping(ListContainerDTO::getContainerNo, Collectors.counting())));
            List<ShipmentsContQtyDTO> shipmentsContQtyDTOS = new ArrayList<>();
            ShipmentsContQtyDTO shipmentsContQtyDTO20 = new ShipmentsContQtyDTO();
            shipmentsContQtyDTO20.setContainerType(CONTAINER_TYPE_20);
            shipmentsContQtyDTO20.setQuantity(0);
            ShipmentsContQtyDTO shipmentsContQtyDTO40 = new ShipmentsContQtyDTO();
            shipmentsContQtyDTO40.setContainerType(CONTAINER_TYPE_40);
            shipmentsContQtyDTO40.setQuantity(0);
            ShipmentsContQtyDTO shipmentsContQtyDTO45 = new ShipmentsContQtyDTO();
            shipmentsContQtyDTO45.setContainerType(CONTAINER_TYPE_45);
            shipmentsContQtyDTO45.setQuantity(0);
            distinctCounts.entrySet().forEach(item -> {
                if (item.getKey().equals(CONTAINER_TYPE_20)) {
                    shipmentsContQtyDTO20.setQuantity(Math.toIntExact(item.getValue()));
                } else if (item.getKey().equals(CONTAINER_TYPE_40)) {
                    shipmentsContQtyDTO40.setQuantity(Math.toIntExact(item.getValue()));
                } else if (item.getKey().equals(CONTAINER_TYPE_45)) {
                    shipmentsContQtyDTO45.setQuantity(Math.toIntExact(item.getValue()));
                }

            });
            shipmentsContQtyDTOS.add(shipmentsContQtyDTO20);
            shipmentsContQtyDTOS.add(shipmentsContQtyDTO40);
            shipmentsContQtyDTOS.add(shipmentsContQtyDTO45);
            shipmentsContQtyDTOS = shipmentsContQtyDTOS.stream().sorted(Comparator.comparing(ShipmentsContQtyDTO::getContainerType)).collect(Collectors.toList());
            shipmentsPurchaseOrdersMainDTO.setShipmentsPurchaseOrdersDTOList(shipmentsPurchaseOrdersDTOList);
            shipmentsPurchaseOrdersMainDTO.setConsolidatorList(consolidators);
            shipmentsPurchaseOrdersMainDTO.setShipmentsContQtyDTO(shipmentsContQtyDTOS);
            return shipmentsPurchaseOrdersMainDTO;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public ShipmentDTO createPKL(ListSearchPIShipmentDTO request) {
        Optional<Shipment> oShipment = shipmentRepository.findById(request.getShipmentId());
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    throw new BusinessException("The only supplier must be update Packing List");
                }
            }
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                if (!shipment.getStatus().equals(STATUS_SHIPMENT_STARTUP)) {
                    throw new BusinessException("The state must be Start Up to be create Packing List.");
                }
                StringBuilder invoice = new StringBuilder();
                ShipmentsPackingList shipmentsPackingList = new ShipmentsPackingList();
                Set<ShipmentsPackingListDetail> shipmentsPackingListDetails = new HashSet<>();
                Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKLSet = new HashSet<>();
                request.getSearch().forEach(search -> {
                    ShipmentProformaInvoicePKL shipmentProformaInvoicePKL = new ShipmentProformaInvoicePKL();
                    List<Shipment> shipments = shipmentRepository.findAllByProformaInvoiceId(search.getProformaInvoiceId());
                    if (!shipments.isEmpty()) {
                        throw new BusinessException(String.format("Proforma Invoice %s exist in another Packing List", search.getProformaInvoiceId()));
                    }
                    Optional<ProformaInvoiceWH> oProformaInvoiceWH = proformaInvoiceWHRepository.findById(search.getProformaInvoiceId());
                    if (oProformaInvoiceWH.isPresent()) {
                        ProformaInvoiceWH proformaInvoiceWH = oProformaInvoiceWH.get();
                        Optional<ProformaInvoiceWHDetail> oProformaInvoiceDetail = proformaInvoiceWHDetailRepository.findTop1CdcVersionByProformaInvoiceWHOrderByCdcVersionDesc(proformaInvoiceWH);
                        if (oProformaInvoiceDetail.isEmpty()) {
                            throw new BusinessException("Can not find new version detail proforma invoice");
                        }
                        shipmentProformaInvoicePKL.setProformaInvoiceNo(proformaInvoiceWH.getOrderNo());
                        shipmentProformaInvoicePKL.setProformaInvoiceId(proformaInvoiceWH.getId());
                        shipmentProformaInvoicePKL.setSupplier(proformaInvoiceWH.getSupplier());
                        shipmentProformaInvoicePKL.setShipment(shipment);
                        //get new cdcVersion detail
                        Long cdcVersionMax = oProformaInvoiceDetail.get().getCdcVersion();
                        shipmentsPackingList.setSupplier(proformaInvoiceWH.getSupplier());
                        if (invoice.length() == 0) {
                            invoice.append(proformaInvoiceWH.getOrderNo());
                        } else {
                            invoice.append(" - ").append(proformaInvoiceWH.getOrderNo());
                        }
                        proformaInvoiceWH.getProformaInvoiceWHDetail().stream().filter(i -> i.getCdcVersion().equals(cdcVersionMax)).forEach(element -> {
                            ShipmentsPackingListDetail shipmentsPackingListDetail = new ShipmentsPackingListDetail();
                            shipmentsPackingListDetail.setSku(element.getSku());
                            shipmentsPackingListDetail.setProductName(element.getProductName());
                            shipmentsPackingListDetail.setQuantity(element.getQty());
                            shipmentsPackingListDetail.setTotalVolume(element.getTotalVolume());
                            shipmentsPackingListDetail.setGrossWeight(element.getGrossWeight());
                            shipmentsPackingListDetail.setNetWeight(element.getNetWeight());
                            shipmentsPackingListDetail.setQtyEachCarton(element.getPcs());
                            shipmentsPackingListDetail.setTotalCarton(element.getTotalBox());
                            shipmentsPackingListDetail.setTotalVolume(element.getTotalVolume());
                            shipmentsPackingListDetail.setBarcode(element.getAsin());
                            shipmentsPackingListDetail.setUnitPrice(element.getUnitPrice());
                            shipmentsPackingListDetail.setProformaInvoiceNo(proformaInvoiceWH.getOrderNo());
                            shipmentsPackingListDetail.setProformaInvoiceId(proformaInvoiceWH.getId());
                            //shipmentsPackingListDetail.setNote(element.getn);
                            shipmentsPackingListDetails.add(shipmentsPackingListDetail);
                        });
                        PurchaseOrdersWH purchaseOrdersWH = proformaInvoiceWH.getPurchaseOrdersWH();
                        purchaseOrdersWH.setStatus(STATUS_PO_WH_PI_CI_CREATED);
                        purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                    }
                    shipmentProformaInvoicePKLSet.add(shipmentProformaInvoicePKL);
                });
                shipmentsPackingList.setShipmentsPackingListDetail(shipmentsPackingListDetails);
                shipmentsPackingList.setInvoice(invoice.toString());
                shipmentsPackingList.setShipmentProformaInvoicePKL(shipmentProformaInvoicePKLSet);
                shipmentsPackingList.setStatus(STATUS_PACKING_LIST_WH_NEW);
                Set<ShipmentProformaInvoicePKL> detailSet = shipment.getShipmentProformaInvoicePKL();
                detailSet.addAll(shipmentProformaInvoicePKLSet);
                shipment.setShipmentProformaInvoicePKL(detailSet);
                Set<ShipmentsContainers> shipmentsContainers = getListContainers(shipment);
                shipmentsContainers = shipmentsContainers.stream().map(i -> {
                    i.setStatus(STATUS_CONTAINER_STARTUP);
                    return i;
                }).collect(Collectors.toSet());
                shipment.setShipmentsContainers(shipmentsContainers);
                shipmentRepository.saveAndFlush(shipment);
                return mappingEntityToDto(shipment, ShipmentDTO.class);
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentsPackingListDTO updatePKL(ShipmentsPackingListDTO request) {
        Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
        Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
        if (oUser.isPresent()) {
            String supplier = oUser.get().getVendor();
            if (CommonDataUtil.isEmpty(supplier)) {
                throw new BusinessException("The only supplier must be update Packing List");
            }
        }
        try {
            if (oShipmentsPackingList.isPresent()) {
                ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                CommercialInvoiceWH commercialInvoiceWH = new CommercialInvoiceWH();
                Optional<Shipment> oShipment = shipmentsPackingList.getShipmentProformaInvoicePKL().parallelStream().map(ShipmentProformaInvoicePKL::getShipment).findFirst();
                if (oShipment.isEmpty()) {
                    throw new BusinessException("Can not find Shipment");
                }
                Shipment shipment = oShipment.get();
                BeanUtils.copyProperties(request, shipmentsPackingList);
                Set<ShipmentsPackingListDetail> details = request.getShipmentsPackingListDetail().parallelStream().map(i -> {
                    ShipmentsPackingListDetail shipmentsPackingListDetail = new ShipmentsPackingListDetail();
                    CommonDataUtil.getModelMapper().map(i, shipmentsPackingListDetail);
                    return shipmentsPackingListDetail;
                }).collect(Collectors.toSet());
                if (request.getCommercialInvoiceWH() != null) {
                    BeanUtils.copyProperties(request.getCommercialInvoiceWH(), commercialInvoiceWH);
                    Set<CommercialInvoiceWHDetail> commercialInvoiceWHDetails = request.getCommercialInvoiceWH().getCommercialInvoiceWHDetail().parallelStream().map(item -> {
                        CommercialInvoiceWHDetail commercialInvoiceWHDetail = new CommercialInvoiceWHDetail();
                        CommonDataUtil.getModelMapper().map(item, commercialInvoiceWHDetail);
                        return commercialInvoiceWHDetail;
                    }).collect(Collectors.toSet());
                    if (request.getCommercialInvoiceWH().getCommercialInvoiceToTalAmountLog() != null) {
                        Set<CommercialInvoiceWHTotalAmountLog> commercialInvoiceWHTotalAmountLogs = request.getCommercialInvoiceWH().getCommercialInvoiceToTalAmountLog().parallelStream().map(item -> {
                            CommercialInvoiceWHTotalAmountLog commercialInvoiceWHTotalAmountLog = new CommercialInvoiceWHTotalAmountLog();
                            CommonDataUtil.getModelMapper().map(item, commercialInvoiceWHTotalAmountLog);
                            return commercialInvoiceWHTotalAmountLog;
                        }).collect(Collectors.toSet());
                        commercialInvoiceWH.setCommercialInvoiceWHTotalAmountLog(commercialInvoiceWHTotalAmountLogs);
                    }
                    commercialInvoiceWH.setCommercialInvoiceWHDetail(commercialInvoiceWHDetails);
                    shipmentsPackingList.setCommercialInvoiceWH(commercialInvoiceWH);
                }
                if (shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_REJECTED)) {
                    shipmentsPackingList.setStatus(STATUS_PACKING_LIST_WH_ADJUSTED);
                }
                shipmentsPackingList.setShipmentsPackingListDetail(details);
                shipmentPackingListRepository.saveAndFlush(shipmentsPackingList);
                Set<ShipmentsContainers> shipmentsContainersOld = shipment.getShipmentsContainers();
                if (CommonDataUtil.isNotEmpty(shipmentsContainersOld)) {
                    shipmentsContainersOld = shipmentsContainersOld.parallelStream().map(i -> {
                        i.setShipment(null);
                        return i;
                    }).collect(Collectors.toSet());
                }
                Set<ShipmentsContainers> shipmentsContainers = getListContainers(shipment);
                shipment.setShipmentsContainers(shipmentsContainers);
//                List<Resource> resourcesListing = resourceRepository.findByFileTypeAndPackingListWhId(GlobalConstant.FILE_UPLOAD, request.getId());
//                if (CommonDataUtil.isNotEmpty(resourcesListing)) {
//                    resourcesListing.parallelStream().forEach(item -> {
//                        try {
//                            if (item.getModule().equals(MODULE_PACKING_LIST_WH_TELEX_RELEASE) ||
//                                item.getModule().equals(MODULE_PACKING_LIST_WH_TCSA_FORM) ||
//                                item.getModule().equals(MODULE_PACKING_LIST_WH_LACEY_ACT) ||
//                                item.getModule().equals(MODULE_PACKING_LIST_WH_FUMIGATION_CERTIFICATE)) {
//                                resourceService.deleteFileUpload(item.getId());
//                            }
//                        } catch (IOException e) {
//                            throw new BusinessException("Can not delete file upload.");
//                        }
//                    });
//                }
                shipmentRepository.saveAndFlush(shipment);
                if (CommonDataUtil.isNotEmpty(shipmentsContainersOld)) {
                    shipmentsContainersRepository.deleteAll(shipmentsContainersOld);
                }
                return CommonDataUtil.getModelMapper().map(shipmentsPackingList, ShipmentsPackingListDTO.class);
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }


    @Override
    public ShipmentsContainersDTO updateContainers(ShipmentsContainersDTO request) {
        Optional<ShipmentsContainers> oShipmentsContainers = shipmentsContainersRepository.findById(request.getId());
        try {
            if (oShipmentsContainers.isPresent()) {
                ShipmentsContainers shipmentsContainers = oShipmentsContainers.get();
                shipmentsContainers.setWhDelivered(request.getWhDelivered());
                shipmentsContainers.setWhRelease(request.getWhRelease());
                shipmentsContainers.setWhGateOut(request.getWhGateOut());
                shipmentsContainers.setWhLocation(request.getWhLocation());
                shipmentsContainers.setNote(request.getNote());
                shipmentsContainers.setUnloadingType(request.getUnloadingType());
                shipmentsContainers.setGateOutPort(request.getGateOutPort());
                shipmentsContainers.setContainerDischargeDate(request.getContainerDischargeDate());
                shipmentsContainersRepository.saveAndFlush(shipmentsContainers);
                return CommonDataUtil.getModelMapper().map(shipmentsContainers, ShipmentsContainersDTO.class);
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentsPackingListDTO generateCommercialInvoice(Integer id) {
        Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(id);
        try {
            if (oShipmentsPackingList.isPresent()) {
                ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                if (shipmentsPackingList.getCommercialInvoiceWH() != null) {
                    throw new BusinessException("Commercial Invoice already exists.");
                }
                CommercialInvoiceWH commercialInvoiceWH = new CommercialInvoiceWH();
                Set<CommercialInvoiceWHDetail> details = new HashSet<>();
                shipmentsPackingList.getShipmentsPackingListDetail().forEach(item -> {
                    CommercialInvoiceWHDetail commercialInvoiceWHDetail = new CommercialInvoiceWHDetail();
                    commercialInvoiceWHDetail.setSku(item.getSku());
                    commercialInvoiceWHDetail.setProductTitle(item.getProductName());
                    commercialInvoiceWHDetail.setUnitPrice(item.getUnitPrice());
                    commercialInvoiceWHDetail.setQty(item.getQuantity());
                    commercialInvoiceWHDetail.setaSin(item.getBarcode());
                    double amount = Math.round(item.getUnitPrice() * item.getQuantity() * 1000.0) / 1000.0;
                    commercialInvoiceWHDetail.setAmount(amount);
                    details.add(commercialInvoiceWHDetail);
                });
                commercialInvoiceWH.setCommercialInvoiceWHDetail(details);
                commercialInvoiceWH.setInvoiceNo(shipmentsPackingList.getInvoice());
                commercialInvoiceWH.setSupplier(shipmentsPackingList.getSupplier());
                commercialInvoiceWH.setStatus(STATUS_CI_NEW);
                commercialInvoiceWH.setShipmentsPackingList(shipmentsPackingList);
                shipmentsPackingList.setCommercialInvoiceWH(commercialInvoiceWH);
                shipmentPackingListRepository.saveAndFlush(shipmentsPackingList);
                return CommonDataUtil.getModelMapper().map(shipmentsPackingList, ShipmentsPackingListDTO.class);
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean deleteCommercialInvoice(ActionSingleIdDTO request) {
        Optional<CommercialInvoiceWH> oCommercialInvoiceWH = commercialInvoiceWHRepository.findById(request.getId());
        try {
            if (oCommercialInvoiceWH.isPresent()) {
                CommercialInvoiceWH commercialInvoiceWH = oCommercialInvoiceWH.get();
                ShipmentsPackingList shipmentsPackingList = commercialInvoiceWH.getShipmentsPackingList();
                if (!shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_NEW) && !shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_REJECTED)) {
                    throw new BusinessException("The status not valid.");
                }
                shipmentsPackingList.setCommercialInvoiceWH(null);
                commercialInvoiceWH.setShipmentsPackingList(null);
                resourceService.deleteResource(commercialInvoiceWH);
                shipmentPackingListRepository.saveAndFlush(shipmentsPackingList);
                commercialInvoiceWHRepository.saveAndFlush(commercialInvoiceWH);
                commercialInvoiceWHRepository.deleteById(request.getId());
                return true;
            } else {
                throw new BusinessException("Can not find Shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }


    @Override
    public List<ShipmentToIMSDTO> getAllShipment(InputGetShipmentDTO request) {
        List<Shipment> shipments = shipmentRepository.findAllByToWarehouseAndStatus(request.getWareHouseCode(), STATUS_SHIPMENT_PIPELINE);
        if (!shipments.isEmpty()) {
            return shipments.stream().map(i -> mappingEntity(i, request.getWareHouseCode())).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ShipmentContainerDetailToIMSDTO> getDetailShipmentContainer(InputGetContainerShipmentDTO request) {
        Optional<ShipmentsContainers> oShipmentContainer = shipmentsContainersRepository.findById(request.getContainerId());
        if (!oShipmentContainer.isEmpty()) {
            ShipmentsContainers shipmentsContainers = oShipmentContainer.get();
            return shipmentsContainers.getShipmentsContainersDetail().stream().map(i -> mappingEntityDetail(i, shipmentsContainers.getId())).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public ShipmentContainerDetailToIMSDTO mappingEntityDetail(ShipmentsContainersDetail shipmentsContainersDetail, Integer containerId) {
        ShipmentContainerDetailToIMSDTO shipmentContainerDetailToIMSDTO = new ShipmentContainerDetailToIMSDTO();
        shipmentContainerDetailToIMSDTO.setContainerId(String.valueOf(containerId));
        CommonDataUtil.getModelMapper().map(shipmentsContainersDetail, shipmentContainerDetailToIMSDTO);
        shipmentContainerDetailToIMSDTO.setContainerId(String.valueOf(containerId));
        return shipmentContainerDetailToIMSDTO;
    }

    public ShipmentToIMSDTO mappingEntity(Shipment shipment, String wareHouseCode) {
        Set<ShipmentsContainers> shipmentsContainers = shipment.getShipmentsContainers().parallelStream().filter(i -> i.getWhLocation() != null && i.getWhLocation().equals(wareHouseCode)).collect(Collectors.toSet());
        if (!shipmentsContainers.isEmpty()) {
            List<ShipmentContainerToIMSDTO> containerToIMSDTOS = new ArrayList<>();
            String shipmentInv = shipmentsContainers.stream().map(i -> i.getShipmentsContainersDetail().stream().map(ShipmentsContainersDetail::getProformaInvoiceNo).distinct().collect(Collectors.joining(" - "))).distinct().collect(Collectors.joining(" - "));
            ShipmentToIMSDTO shipmentToIMSDTO = new ShipmentToIMSDTO();
            shipmentToIMSDTO.setId(shipment.getId());
            shipmentToIMSDTO.setShipmentCtrlId(shipment.getShipmentId());
            shipmentToIMSDTO.setStatus("3");
            shipmentToIMSDTO.setShipmentInv(shipmentInv);
            containerToIMSDTOS = shipmentsContainers.parallelStream().map(i -> {
                ShipmentContainerToIMSDTO shipmentContainerToIMSDTO = new ShipmentContainerToIMSDTO();
                String type = "";
                if (i.getType().equals(CONTAINER_TYPE_20)) {
                    type = "20";
                } else if (i.getType().equals(CONTAINER_TYPE_40)) {
                    type = "40";
                } else if (i.getType().equals(CONTAINER_TYPE_45)) {
                    type = "45";
                }
                shipmentContainerToIMSDTO.setType(type);
                shipmentContainerToIMSDTO.setId(i.getId());
                shipmentContainerToIMSDTO.setWarehouseCode(i.getWhLocation());
                shipmentContainerToIMSDTO.setTitle(i.getContainerNumber());
                shipmentContainerToIMSDTO.setStatus("8");
                return shipmentContainerToIMSDTO;
            }).collect(Collectors.toList());
            shipmentToIMSDTO.setShipmentContainerToIMS(containerToIMSDTOS);
            return shipmentToIMSDTO;
        }
        return null;
    }

    @Override
    public boolean deleteShipment(ListIdDTO request) {
        request.getId().forEach(id -> {
            try {
                Optional<Shipment> oShipment = shipmentRepository.findById(id);
                if (oShipment.isPresent()) {
                    Shipment shipment = oShipment.get();
                    if (!Objects.equals(shipment.getStatus(), STATUS_SHIPMENT_STARTUP)) {
                        throw new BusinessException("The status not valid.");
                    }
                    resourceService.deleteResource(shipment);
                    shipmentRepository.deleteById(shipment.getId());

                } else {
                    throw new BusinessException("The shipment not exists.");
                }
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean deleteDetailPurchaseOrder(ListIdDTO request) {
        request.getId().forEach(id -> {
            try {
                Optional<ShipmentsPurchaseOrders> oShipmentsPurchaseOrders = shipmentsPurchaseOrdersRepository.findById(id);
                if (oShipmentsPurchaseOrders.isPresent()) {
                    ShipmentsPurchaseOrders shipmentsPurchaseOrders = oShipmentsPurchaseOrders.get();
                    Shipment shipment = shipmentsPurchaseOrders.getShipment();
                    if (!Objects.equals(shipment.getStatus(), STATUS_SHIPMENT_STARTUP)) {
                        throw new BusinessException("The status not valid.");
                    }
                    if (shipment.getShipmentProformaInvoicePKL() != null) {
                        long count = shipment.getShipmentProformaInvoicePKL().parallelStream().filter(i -> i.getProformaInvoiceNo().equals(shipmentsPurchaseOrders.getPurchaseOrderNo())).count();
                        if (count > 0) {
                            throw new BusinessException("Already the packing list with this PO.Can not delete this PO.");
                        }
                    }
                    shipmentsPurchaseOrders.setShipment(null);
                    shipmentsPurchaseOrdersRepository.deleteById(id);
                } else {
                    throw new BusinessException("The shipment not exists.");
                }
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean deletePackingList(ListIdDTO request) {
        request.getId().forEach(id -> {
            try {

                Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(id);
                if (oShipmentsPackingList.isEmpty()) {
                    throw new BusinessException("Can not find Shipment PackingList.");
                }
                ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                if (!shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_NEW)) {
                    throw new BusinessException("The status not valid.");
                }
                Set<ShipmentProformaInvoicePKL> shipmentProformaInvoicePKLSet = shipmentsPackingList.getShipmentProformaInvoicePKL().stream().map(i -> {
                    i.setShipment(null);
                    return i;
                }).collect(Collectors.toSet());
                resourceService.deleteResource(shipmentsPackingList);
                shipmentsPackingList.setShipmentProformaInvoicePKL(shipmentProformaInvoicePKLSet);
                shipmentPackingListRepository.delete(shipmentsPackingList);
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean sendPackingList(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                String supplier = oUser.get().getVendor();
                if (CommonDataUtil.isEmpty(supplier)) {
                    throw new BusinessException("The only Supplier must be send Packing List");
                }
                Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
                if (oShipmentsPackingList.isPresent()) {
                    ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                    if (!shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_NEW) && !shipmentsPackingList.getStatus().equals(STATUS_PACKING_LIST_WH_ADJUSTED)) {
                        throw new BusinessException("The status not valid.");
                    }
                    shipmentsPackingList.setStatus(STATUS_PACKING_LIST_WH_SUBMIT);
                    shipmentPackingListRepository.saveAndFlush(shipmentsPackingList);
                }
                return true;
            } else {
                throw new BusinessException("The only Supplier must be confirm Packing List");
            }
        } catch (
            Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public ShipmentMainDTO getShipmentDetail(DetailObjectDTO request) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Set<String> consolidators = new HashSet<>();
                Shipment shipment = oShipment.get();
                ShipmentDTO dto = new ShipmentDTO();
                dto = CommonDataUtil.getModelMapper().map(shipment, ShipmentDTO.class);
                dto.getShipmentsPurchaseOrders().parallelStream().forEach(item -> {

                    Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(item.getPurchaseOrderId());
                    if (oPurchaseOrdersWH.isPresent()) {
                        PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                        consolidators.add(purchaseOrdersWH.getVendorId());
                    }
                });
                Set<ShipmentsQuantityDTO> shipmentsQuantityDTOS = getListOrderedQuantity(shipment);
                dto.setShipmentsQuantities(shipmentsQuantityDTOS);
                dto.setShipmentsContQty(dto.getShipmentsContQty().stream().sorted(Comparator.comparing(ShipmentsContQty::getContainerType)).collect(Collectors.toList()));
                Set<ShipmentLogUpdateDate> shipmentLogUpdateDateSet = shipment.getShipmentLogUpdateDates();
                Set<ShipmentLogUpdateField> shipmentLogUpdateField = shipment.getShipmentLogUpdateField();

                dto.setEtdLogUpdates(getHistoryDate(shipmentLogUpdateDateSet, SM_ETD));
                dto.setLogChangeFieldCont(getHistoryField(shipmentLogUpdateField, SM_FILED_CONT));
                Optional<WareHouse> oWareHouse = wareHouseRepository.findByWarehouseCode(dto.getToWarehouse());
                if (oWareHouse.isPresent()) {
                    dto.setToWarehouseName(oWareHouse.get().getWarehouseName());
                } else {
                    throw new BusinessException("Can not find Ware House");
                }

                dto.setConsolidatorList(consolidators);
                return dto;
            }
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean updateDetailContainer(InputUpdateContainerDTO request) {
        try {
            Optional<ShipmentsContainers> oShipmentsContainers = shipmentsContainersRepository.findById(request.getContainerId());
            if (oShipmentsContainers.isPresent()) {
                ShipmentsContainers shipmentsContainers = oShipmentsContainers.get();
                List<ShipmentsContainersDetail> details = shipmentsContainers.getShipmentsContainersDetail().stream().map(item -> {
                    Optional<InputUpdateDetailContainerDTO> oInputDetail =
                        request.getInputUpdateDetailContainerDTOList().parallelStream().filter(i -> i.getId().equals(item.getId())).findFirst();
                    if (oInputDetail.isPresent()) {
                        item.setImportedQuantity(oInputDetail.get().getImportQuantity());
                        item.setUpdatedDate(new Date().toInstant());
                        double amount = Math.round(item.getImportAmount() * item.getUnitPrice() * 100) / 100;
                        item.setImportAmount(amount);
                    } else {
                        throw new BusinessException(String.format("Miss data sku %s.", item.getSku()));
                    }
                    return item;
                }).collect(Collectors.toList());
                shipmentsContainers.setShipmentsContainersDetail(details);
                shipmentsContainersRepository.saveAndFlush(shipmentsContainers);
                return true;
            } else {
                throw new BusinessException("The container not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentLocalBrokerDTO getLocalBrokerDetail(DetailObjectDTO request) {
        try {
            ShipmentLocalBrokerDTO shipmentLocalBrokerDTO = new ShipmentLocalBrokerDTO();
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
                User user;
                if (oUser.isEmpty()) {
                    throw new BusinessException("Can not find user.");
                } else {
                    user = oUser.get();
                }
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_WAREHOUSE)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
                if (CommonDataUtil.isNotNull(shipment.getShipmentLocalBroker())) {
                    CommonDataUtil.getModelMapper().map(shipment.getShipmentLocalBroker(), shipmentLocalBrokerDTO);
                    List<Resource> resources = resourceRepository.findByFileTypeAndShipmentId(GlobalConstant.FILE_UPLOAD, shipment.getId());
                    if (!resources.isEmpty()) {
                        Set<ResourceDTO> resourceDTOSet = resources.parallelStream().filter(i -> i.getModule().equals(MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_INFO) ||
                            i.getModule().equals(MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_ORDER) ||
                            i.getModule().equals(MODULE_SHIPMENT_LOCAL_BROKER_SHIPMENT_OTHERS)).map(this::convertToObjectResourceDTO).collect(Collectors.toSet());
                        shipmentLocalBrokerDTO.setResources(resourceDTOSet);
                    }
                    shipmentLocalBrokerDTO.setShipmentId(request.getId());
                }
                return shipmentLocalBrokerDTO;
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentLocalBrokerDTO saveLocalBrokerDetail(ShipmentLocalBrokerDTO request) {
        try {
            ShipmentLocalBrokerDTO shipmentLocalBrokerDTO = new ShipmentLocalBrokerDTO();
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getShipmentId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                ShipmentLocalBroker shipmentLocalBroker = new ShipmentLocalBroker();
                BeanUtils.copyProperties(request, shipmentLocalBroker);
                shipmentLocalBroker.setShipment(shipment);
                shipment.setShipmentLocalBroker(shipmentLocalBroker);
//                List<Resource> resourcesListing = resourceRepository.findByFileTypeAndPackingListWhId(GlobalConstant.FILE_UPLOAD, request.getId());
//                if (CommonDataUtil.isNotEmpty(resourcesListing)) {
//                    resourcesListing.parallelStream().forEach(item -> {
//                        try {
//                            if (
//                                item.getModule().equals(MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE) ||
//                                    item.getModule().equals(MODULE_SHIPMENT_US_BROKER_DUTY_FEE) ||
//                                    item.getModule().equals(MODULE_SHIPMENT_US_BROKER_OTHERS)) {
//                                resourceService.deleteFileUpload(item.getId());
//                            }
//                        } catch (IOException e) {
//                            throw new BusinessException("Can not delete file upload.");
//                        }
//                    });
//                }
                shipment.setStatusUsBroker(STATUS_SHIPMENT_US_BROKER_UPLOADED);
                shipmentRepository.saveAndFlush(shipment);
                CommonDataUtil.getModelMapper().map(shipment.getShipmentLocalBroker(), shipmentLocalBrokerDTO);
                return shipmentLocalBrokerDTO;
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public boolean saveUSBrokerDetail(ActionSingleIdDTO request) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
                User user = oUser.get();
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_WAREHOUSE) || authorities.contains(POMS_LOCAL_BROKER)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
//                List<Resource> resourcesListing = resourceRepository.findByFileTypeAndPackingListWhId(GlobalConstant.FILE_UPLOAD, request.getId());
//                if (CommonDataUtil.isNotEmpty(resourcesListing)) {
//                    resourcesListing.parallelStream().forEach(item -> {
//                        try {
//                            if (
//                                item.getModule().equals(MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE) ||
//                                    item.getModule().equals(MODULE_SHIPMENT_US_BROKER_DUTY_FEE) ||
//                                    item.getModule().equals(MODULE_SHIPMENT_US_BROKER_OTHERS)) {
//                                resourceService.deleteFileUpload(item.getId());
//                            }
//                        } catch (IOException e) {
//                            throw new BusinessException("Can not delete file upload.");
//                        }
//                    });
//                }
                return true;
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public USBrokerDTO getUSBrokerDetail(DetailObjectDTO request) {
        try {
            USBrokerDTO usBrokerDTO = new USBrokerDTO();
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
                User user = oUser.get();
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_WAREHOUSE) || authorities.contains(POMS_LOCAL_BROKER)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
                List<Resource> resources = resourceRepository.findByFileTypeAndShipmentId(GlobalConstant.FILE_UPLOAD, shipment.getId());
                Set<ResourceDTO> resourceDTOSet = resources.parallelStream().filter(i -> i.getModule().equals(MODULE_SHIPMENT_US_BROKER_INVOICES) ||
                    i.getModule().equals(MODULE_SHIPMENT_US_BROKER_ARRIVAL_NOTICE) ||
                    i.getModule().equals(MODULE_SHIPMENT_US_BROKER_DUTY_FEE) ||
                    i.getModule().equals(MODULE_SHIPMENT_US_BROKER_OTHERS)).map(this::convertToObjectResourceDTO).collect(Collectors.toSet());
                usBrokerDTO.setResources(resourceDTOSet);
                usBrokerDTO.setStatus(shipment.getStatusUsBroker());
                return usBrokerDTO;
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentLogisticsInfoDTO saveLogisticsInfo(ShipmentLogisticsInfoDTO request) {
        try {
            ShipmentLogisticsInfoDTO shipmentLogisticsInfoDTO = new ShipmentLogisticsInfoDTO();
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getShipmentId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                ShipmentLogisticsInfo shipmentLogisticsInfo = new ShipmentLogisticsInfo();
                BeanUtils.copyProperties(request, shipmentLogisticsInfo);
                shipmentLogisticsInfo.shipment(shipment);
                shipment.setShipmentLogisticsInfo(shipmentLogisticsInfo);
                shipmentRepository.saveAndFlush(shipment);
                CommonDataUtil.getModelMapper().map(shipment.getShipmentLogisticsInfo(), shipmentLogisticsInfoDTO);
                return shipmentLogisticsInfoDTO;
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentLogisticsInfoDTO getLogisticsInfoDetail(DetailObjectDTO request) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                if (shipment.getShipmentLogisticsInfo() == null) {
                    ShipmentLogisticsInfoDTO shipmentLogisticsInfoDTO = new ShipmentLogisticsInfoDTO();
                    return shipmentLogisticsInfoDTO;
                }
                return CommonDataUtil.getModelMapper().map(shipment.getShipmentLogisticsInfo(), ShipmentLogisticsInfoDTO.class);
            } else {
                throw new BusinessException("The shipment not exists.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    private ResourceDTO convertToObjectResourceDTO(Object o) {
        ResourceDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, ResourceDTO.class);
        return dto;
    }


    private List<LogUpdateDateDTO> getHistoryDate(Set<ShipmentLogUpdateDate> shipmentLogUpdateDate, String typeDate) {
        List<LogUpdateDateDTO> result;
        result = shipmentLogUpdateDate.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate()))
            .sorted(Comparator.comparing(ShipmentLogUpdateDate::getCreatedDate).reversed())
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

    private List<LogChangeFieldDTO> getHistoryField(Set<ShipmentLogUpdateField> shipmentLogUpdateField, String field) {
        List<LogChangeFieldDTO> result;
        result = shipmentLogUpdateField.parallelStream()
            .filter(i -> field.equals(i.getField()))
            .sorted(Comparator.comparing(ShipmentLogUpdateField::getCreatedDate).reversed())
            .map(item -> {
                User user = userRepository.findOneByLogin(item.getUpdatedBy()).orElse(null);
                String updateBy = CommonDataUtil.getUserFullName(user);

                LogChangeFieldDTO dto = new LogChangeFieldDTO();
                BeanUtils.copyProperties(item, dto);
                dto.setUpdatedBy(updateBy);
                return dto;
            })
            .collect(Collectors.toList());
        return result;
    }

    @Override
    public Set<ShipmentsQuantityDTO> getOrderQuantity(ActionSingleIdDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isEmpty()) {
                throw new BusinessException("Can not find user login.");
            } else {
                User user = oUser.get();
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_BROKER) || authorities.contains(POMS_WAREHOUSE) || authorities.contains(POMS_LOCAL_BROKER)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            }
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                return getListOrderedQuantity(shipment);
            } else {
                throw new BusinessException("Can not find shipment.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentsPackingListDTO getPackingListDetail(DetailObjectDTO request) {
        try {
            ShipmentsPackingListDTO dto = new ShipmentsPackingListDTO();
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                User user = oUser.get();
                Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
                if (oShipmentsPackingList.isPresent()) {
                    ShipmentsPackingList shipmentsPackingList = oShipmentsPackingList.get();
                    if (user.getVendor() != null && user.getVendor().length() > 0 && !user.getVendor().equals(shipmentsPackingList.getSupplier())) {
                        throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                    }
                    CommonDataUtil.getModelMapper().map(shipmentsPackingList, dto);
                    List<Resource> resourcesListing = resourceRepository.findByFileTypeAndPackingListWhId(GlobalConstant.FILE_UPLOAD, request.getId());
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
                        dto.setFileUploads(resources);
                    }
                    CommercialInvoiceWHDTO commercialInvoiceWHDTO = dto.getCommercialInvoiceWH();
                    if (commercialInvoiceWHDTO != null) {
                        List<Resource> resourcesCI = resourceRepository.findByFileTypeAndCommercialInvoiceWHId(GlobalConstant.FILE_UPLOAD, commercialInvoiceWHDTO.getId());
                        if (CommonDataUtil.isNotEmpty(resourcesCI)) {
                            List<ResourceDTO> resources = resourcesCI.parallelStream().map(item -> {
                                ResourceDTO data = new ResourceDTO();
                                data.setPath(item.getPath());
                                data.setId(item.getId());
                                data.setModule(item.getModule());
                                data.setName(item.getName());
                                data.setType(item.getType());
                                data.setSize(item.getFileSize());
                                return data;
                            }).collect(Collectors.toList());
                            commercialInvoiceWHDTO.setFileUploads(resources);
                        }
                    }
                    dto.setCommercialInvoiceWH(commercialInvoiceWHDTO);
                    return dto;
                }
            } else {
                throw new BusinessException("Can not find user login.");
            }
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public ShipmentsContainersDTO getContainersDetail(DetailObjectDTO request) {
        try {
            ShipmentsContainersDTO dto = new ShipmentsContainersDTO();
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                User user = oUser.get();
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_BROKER) || authorities.contains(POMS_LOCAL_BROKER)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
                Optional<ShipmentsContainers> oShipmentsContainers = shipmentsContainersRepository.findById(request.getId());
                if (oShipmentsContainers.isPresent()) {
                    ShipmentsContainers shipmentsContainers = oShipmentsContainers.get();
                    CommonDataUtil.getModelMapper().map(shipmentsContainers, dto);
                    return dto;
                }
            } else {
                throw new BusinessException("Can not find user login.");
            }
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public Set<ShipmentsPackingListDTO> getAllPackingList(DetailObjectDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                User user = oUser.get();
                Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
                if (oShipment.isPresent()) {
                    Shipment shipment = oShipment.get();
                    Boolean isConsolidator = shipment.getConsolidator().equals(request.getUserId());
                    Set<ShipmentsPackingList> shipmentsPackingLists = shipment.getShipmentProformaInvoicePKL().parallelStream().map(ShipmentProformaInvoicePKL::getShipmentsPackingList).collect(Collectors.toSet());
                    if (!shipmentsPackingLists.isEmpty()) {
                        if (user.getVendor() != null && user.getVendor().length() > 0) {
                            return shipmentsPackingLists.stream().filter(i -> i.getSupplier().equals(user.getVendor()) || isConsolidator).map(item -> {
                                ShipmentsPackingListDTO shipmentsPackingListDTO = new ShipmentsPackingListDTO();
                                shipmentsPackingListDTO.setInvoice(item.getInvoice());
                                shipmentsPackingListDTO.setPackingListId(item.getId());
                                shipmentsPackingListDTO.setStatus(item.getStatus());
                                return shipmentsPackingListDTO;
                            }).collect(Collectors.toSet());
                        } else {
                            return shipmentsPackingLists.stream().map(item ->
                            {
                                ShipmentsPackingListDTO shipmentsPackingListDTO = new ShipmentsPackingListDTO();
                                shipmentsPackingListDTO.setInvoice(item.getInvoice());
                                shipmentsPackingListDTO.setPackingListId(item.getId());
                                shipmentsPackingListDTO.setStatus(item.getStatus());
                                return shipmentsPackingListDTO;
                            }).collect(Collectors.toSet());
                        }
                    }
                    return Collections.emptySet();
                } else {
                    throw new BusinessException("Can not find shipment.");
                }

            } else {
                throw new BusinessException("Can not find user login.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public Set<ShipmentsContainersMainDTO> getAllContainers(DetailObjectDTO request) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            if (oUser.isPresent()) {
                User user = oUser.get();
                Set<String> authorities = userService.checkUserWithRole(oUser.get());
                if (user.getVendor() != null || authorities.contains(POMS_BROKER) || authorities.contains(POMS_LOCAL_BROKER)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
                Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
                if (oShipment.isPresent()) {
                    Shipment shipment = oShipment.get();
                    Set<ShipmentsContainers> shipmentsContainers = shipment.getShipmentsContainers();
                    return shipmentsContainers.parallelStream().map(i -> mappingEntityToDtoContainers(i, ShipmentsContainersMainDTO.class)).collect(Collectors.toSet());

                } else {
                    throw new BusinessException("Can not find shipment.");
                }

            } else {
                throw new BusinessException("Can not find user login.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    public Set<ShipmentsQuantityDTO> getListOrderedQuantity(Shipment shipment) {
        Set<ShipmentsQuantityDTO> shipmentsQuantities = new HashSet<>();
        shipment.getShipmentsPurchaseOrders().parallelStream().forEach(shipmentsPurchaseOrders -> {
            shipmentsPurchaseOrders.getShipmentsPurchaseOrdersDetail().parallelStream().forEach(item -> {
                ShipmentsQuantityDTO shipmentsQuantityDTO = new ShipmentsQuantityDTO();
                CommonDataUtil.getModelMapper().map(item, shipmentsQuantityDTO);
                shipmentsQuantities.add(shipmentsQuantityDTO);
            });
        });
        Map<String, List<ShipmentsQuantityDTO>> groupedBySku = shipmentsQuantities.stream()
            .collect(Collectors.groupingBy(ShipmentsQuantityDTO::getSku));
        Map<String, ShipmentsQuantityDTO> sumBySku = groupedBySku.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<ShipmentsQuantityDTO> group = entry.getValue();
                    Integer sumQuantity = group.stream()
                        .mapToInt(ShipmentsQuantityDTO::getQuantity)
                        .sum();
                    Double sumAmount = group.stream()
                        .mapToDouble(ShipmentsQuantityDTO::getAmount)
                        .sum();
                    Double sumTotalVolume = group.stream()
                        .mapToDouble(ShipmentsQuantityDTO::getTotalVolume)
                        .sum();
                    Double sumGrossWeight = group.stream()
                        .mapToDouble(ShipmentsQuantityDTO::getGrossWeight)
                        .sum();
                    Double sumNetWeight = group.stream()
                        .mapToDouble(ShipmentsQuantityDTO::getNetWeight)
                        .sum();
                    Double unitPrice = group.parallelStream().map(ShipmentsQuantityDTO::getUnitPrice).findFirst().get();
                    String productName = group.parallelStream().map(ShipmentsQuantityDTO::getProductName).findFirst().get();
                    ShipmentsQuantityDTO sumDto = new ShipmentsQuantityDTO();
                    sumDto.setSku(entry.getKey());
                    sumDto.setQuantity(sumQuantity);
                    sumDto.setAmount(sumAmount);
                    sumDto.setTotalVolume(sumTotalVolume);
                    sumDto.setGrossWeight(sumGrossWeight);
                    sumDto.setUnitPrice(unitPrice);
                    sumDto.setProductName(productName);
                    sumDto.setNetWeight(sumNetWeight);
                    return sumDto;
                }
            ));
        Set<ShipmentsQuantityDTO> sumBySkuSet = new HashSet<>(sumBySku.values());
        return sumBySkuSet;
    }

//    public Set<ShipmentsContainers> getListContainersSum(Shipment shipment) {
//
//        Map<String, String> containerType = new HashMap<>();
//        Set<ShipmentsPackingListDetailDTO> shipmentsPackingListDetails = new HashSet<>();
//        shipment.getShipmentProformaInvoicePKL().parallelStream().map(ShipmentProformaInvoicePKL::getShipmentsPackingList).distinct().forEach(item -> {
//            item.getShipmentsPackingListDetail().parallelStream().forEach(i -> {
//                ShipmentsPackingListDetailDTO shipmentsPackingListDetailDTO = new ShipmentsPackingListDetailDTO();
//                CommonDataUtil.getModelMapper().map(i, shipmentsPackingListDetailDTO);
//                containerType.put(i.getContainerNumber(), i.getContainerType());
//                shipmentsPackingListDetails.add(shipmentsPackingListDetailDTO);
//            });
//        });
//
//        Map<String, List<ShipmentsPackingListDetailDTO>> groupedBySku = shipmentsPackingListDetails.stream().filter(i -> i.getContainerNumber() != null && i.getContainerNumber().length() > 0)
//            .collect(Collectors.groupingBy(ShipmentsPackingListDetailDTO::getContainerNumber));
//        Set<ShipmentsContainers> shipmentsContainers = new HashSet<>();
//        groupedBySku.entrySet().forEach(entry -> {
//            ShipmentsContainers containers = new ShipmentsContainers();
//            containers.setWhLocation(shipment.getToWarehouse());
//            containers.setContainerNumber(entry.getKey());
//            containers.setType(containerType.get(entry.getKey()));
//            containers.setStatus(0);
//            containers.setTotalWeight(entry.getValue().stream().map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
//            containers.setTotalNetWeight(entry.getValue().stream().map(x -> Objects.isNull(x.getNetWeight()) ? 0 : x.getNetWeight()).reduce(0.0, Double::sum));
//            containers.setTotalVolume(entry.getValue().stream().map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
//            containers.setTotalAmount(entry.getValue().stream().map(x -> x.getUnitPrice() * x.getQuantity()).reduce(0.0, Double::sum));
//            containers.setTotalItems(entry.getValue().size());
//            List<ShipmentsContainersDetail> shipmentsContainersDetails = entry.getValue().stream().map(item -> {
//                ShipmentsContainersDetail shipmentsContainersDetail = new ShipmentsContainersDetail();
//                CommonDataUtil.getModelMapper().map(item, shipmentsContainersDetail);
//                return shipmentsContainersDetail;
//            }).collect(Collectors.toList());
//
//            Map<String, DoubleSummaryStatistics> result = shipmentsContainersDetails.stream()
//                .collect(Collectors.groupingBy(
//                    detail -> detail.getSku()
//                        + KEY_SPLIT + detail.getProductName()
//                        + KEY_SPLIT + detail.getProformaInvoiceId()
//                        + KEY_SPLIT + detail.getProformaInvoiceNo(),
//                    Collectors.summarizingDouble(ShipmentsContainersDetail::getUnitPrice)
//                ));
//            List<ShipmentsContainersDetail> shipmentsContainersDetailsNew = new ArrayList<>();
//            for (Map.Entry<String, DoubleSummaryStatistics> entryCont : result.entrySet()) {
//                String key = entryCont.getKey();
//                String[] arrKey = key.split(KEY_SPLIT);
//                DoubleSummaryStatistics stats = entryCont.getValue();
//                ShipmentsContainersDetail shipmentsContainersDetail = new ShipmentsContainersDetail();
//                shipmentsContainersDetail.setSku(arrKey[0]);
//                shipmentsContainersDetail.setProductName(arrKey[1]);
//                shipmentsContainersDetail.setProformaInvoiceId(Integer.valueOf(arrKey[2]));
//                shipmentsContainersDetail.setProformaInvoiceNo(arrKey[3]);
//                shipmentsContainersDetail.setGrossWeight(sumField(shipmentsContainersDetails, key, "grossWeight"));
//                shipmentsContainersDetail.setNetWeight(sumField(shipmentsContainersDetails, key, "netWeight"));
//                shipmentsContainersDetail.setQuantity((int) sumField(shipmentsContainersDetails, key, "quantity"));
//                shipmentsContainersDetail.setTotalVolume(sumField(shipmentsContainersDetails, key, "totalVolume"));
//                shipmentsContainersDetail.setImportedQuantity(0);
//                shipmentsContainersDetail.setUnitPrice(stats.getAverage());
//                shipmentsContainersDetail.setImportAmount(0.0);
//                double amount = Math.round(shipmentsContainersDetail.getQuantity() * shipmentsContainersDetail.getUnitPrice() * 100) / 100;
//                shipmentsContainersDetail.setAmount(amount);
//                shipmentsContainersDetailsNew.add(shipmentsContainersDetail);
//            }
//            containers.setShipmentsContainersDetail(shipmentsContainersDetailsNew);
//            shipmentsContainers.add(containers);
//        });
//        return shipmentsContainers;
//
//    }

    public Set<ShipmentsContainers> getListContainers(Shipment shipment) {

        Map<String, String> containerType = new HashMap<>();
        Set<ShipmentsPackingListDetailDTO> shipmentsPackingListDetails = new HashSet<>();
        shipment.getShipmentProformaInvoicePKL().parallelStream().map(ShipmentProformaInvoicePKL::getShipmentsPackingList).distinct().forEach(item -> {
            item.getShipmentsPackingListDetail().parallelStream().forEach(i -> {
                ShipmentsPackingListDetailDTO shipmentsPackingListDetailDTO = new ShipmentsPackingListDetailDTO();
                CommonDataUtil.getModelMapper().map(i, shipmentsPackingListDetailDTO);
                containerType.put(i.getContainerNumber(), i.getContainerType());
                shipmentsPackingListDetails.add(shipmentsPackingListDetailDTO);
            });
        });

        Map<String, List<ShipmentsPackingListDetailDTO>> groupedBySku = shipmentsPackingListDetails.stream().filter(i -> i.getContainerNumber() != null && i.getContainerNumber().length() > 0)
            .collect(Collectors.groupingBy(ShipmentsPackingListDetailDTO::getContainerNumber));
        Set<ShipmentsContainers> shipmentsContainers = new HashSet<>();
        groupedBySku.entrySet().forEach(entry -> {
            ShipmentsContainers containers = new ShipmentsContainers();
            containers.setWhLocation(shipment.getToWarehouse());
            containers.setContainerNumber(entry.getKey());
            containers.setType(containerType.get(entry.getKey()));
            containers.setStatus(STATUS_CONTAINER_STARTUP);
            containers.setTotalWeight(entry.getValue().stream().map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
            containers.setTotalNetWeight(entry.getValue().stream().map(x -> Objects.isNull(x.getNetWeight()) ? 0 : x.getNetWeight()).reduce(0.0, Double::sum));
            containers.setTotalVolume(entry.getValue().stream().map(x -> Objects.isNull(x.getTotalVolume()) ? 0 : x.getTotalVolume()).reduce(0.0, Double::sum));
            containers.setTotalAmount(entry.getValue().stream().map(x -> x.getUnitPrice() * x.getQuantity()).reduce(0.0, Double::sum));
            containers.setTotalItems(entry.getValue().size());
            List<ShipmentsContainersDetail> shipmentsContainersDetails = entry.getValue().stream().map(item -> {
                ShipmentsContainersDetail shipmentsContainersDetail = new ShipmentsContainersDetail();
                CommonDataUtil.getModelMapper().map(item, shipmentsContainersDetail);
                return shipmentsContainersDetail;
            }).collect(Collectors.toList());
            containers.setShipmentsContainersDetail(shipmentsContainersDetails);
            shipmentsContainers.add(containers);
        });
        return shipmentsContainers;

    }

    public Set<ShipmentsPackingListDetail> sumDetailPackingList(Set<ShipmentsPackingListDetail> shipmentsContainersDetails) {
        Map<String, DoubleSummaryStatistics> result = shipmentsContainersDetails.stream()
            .collect(Collectors.groupingBy(
                detail -> detail.getSku()
                    + KEY_SPLIT + detail.getProductName()
                    + KEY_SPLIT + detail.getBarcode()
                    + KEY_SPLIT + detail.getProformaInvoiceNo()
                    + KEY_SPLIT + detail.getProformaInvoiceId()
                    + KEY_SPLIT + detail.getUnitPrice()
                    + KEY_SPLIT + detail.getContainerType()
                    + KEY_SPLIT + detail.getContainerNumber(),
                Collectors.summarizingDouble(ShipmentsPackingListDetail::getQuantity)
            ));
        Set<ShipmentsPackingListDetail> shipmentsContainersDetailsNew = new HashSet<>();
        for (Map.Entry<String, DoubleSummaryStatistics> entryCont : result.entrySet()) {
            String key = entryCont.getKey();
            String[] arrKey = key.split(KEY_SPLIT);
            DoubleSummaryStatistics stats = entryCont.getValue();
            ShipmentsPackingListDetail shipmentsPackingListDetail = new ShipmentsPackingListDetail();
            shipmentsPackingListDetail.setSku(arrKey[0]);
            shipmentsPackingListDetail.setProductName(arrKey[1]);
            shipmentsPackingListDetail.setBarcode(arrKey[2]);
            shipmentsPackingListDetail.setProformaInvoiceId(Integer.valueOf(arrKey[4]));
            shipmentsPackingListDetail.setProformaInvoiceNo(arrKey[3]);
            shipmentsPackingListDetail.setUnitPrice(Double.valueOf(arrKey[5]));
            shipmentsPackingListDetail.setContainerType(arrKey[6]);
            shipmentsPackingListDetail.setContainerNumber(arrKey[7]);
            shipmentsPackingListDetail.setGrossWeight(sumFieldPKL(shipmentsContainersDetails, key, "grossWeight"));
            shipmentsPackingListDetail.setNetWeight(sumFieldPKL(shipmentsContainersDetails, key, "netWeight"));
            shipmentsPackingListDetail.setQuantity((int) stats.getSum());
            shipmentsPackingListDetail.setTotalVolume(sumFieldPKL(shipmentsContainersDetails, key, "totalVolume"));
            shipmentsPackingListDetail.setQtyEachCarton((int) sumFieldPKL(shipmentsContainersDetails, key, "qtyEachCarton"));
            shipmentsContainersDetailsNew.add(shipmentsPackingListDetail);
        }
        return shipmentsContainersDetailsNew;
    }

    private static double sumField(List<ShipmentsContainersDetail> shipments, String key, String fieldName) {
        return shipments.stream()
            .filter(detail -> (detail.getSku()
                + KEY_SPLIT + detail.getProductName()
                + KEY_SPLIT + detail.getProformaInvoiceId()
                + KEY_SPLIT + detail.getProformaInvoiceNo()).equals(key))
            .mapToDouble(detail -> {
                switch (fieldName) {
                    case "quantity":
                        return detail.getQuantity();
                    case "importedQuantity":
                        return detail.getImportedQuantity();
                    case "amount":
                        return detail.getAmount();
                    case "grossWeight":
                        return detail.getGrossWeight();
                    case "netWeight":
                        return detail.getNetWeight();
                    case "totalVolume":
                        return detail.getTotalVolume();
                    default:
                        return 0.0;
                }
            })
            .sum();
    }

    private static double sumFieldPKL(Set<ShipmentsPackingListDetail> shipments, String key, String fieldName) {
        return shipments.stream()
            .filter(detail -> (detail.getSku()
                + KEY_SPLIT + detail.getProductName()
                + KEY_SPLIT + detail.getBarcode()
                + KEY_SPLIT + detail.getProformaInvoiceNo()
                + KEY_SPLIT + detail.getProformaInvoiceId()
                + KEY_SPLIT + detail.getUnitPrice()).equals(key))
            .mapToDouble(detail -> {
                switch (fieldName) {
                    case "quantity":
                        return detail.getQuantity();
                    case "qtyEachCarton":
                        return detail.getQtyEachCarton();
                    case "grossWeight":
                        return detail.getGrossWeight();
                    case "netWeight":
                        return detail.getNetWeight();
                    case "totalVolume":
                        return detail.getTotalVolume();
                    default:
                        return 0.0;
                }
            })
            .sum();
    }

    @Override
    public void export(String filename, ActionSingleIdDTO request) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFileDetailPKL(request, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }

    @Override
    public void exportCI(String filename, ActionSingleIdDTO request) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFileDetailCI(request, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }

    @Override
    public byte[] exportMultiPKLCI(ListIdDTO request) {
        List<ShipmentsPackingList> shipmentsPackingLists = shipmentPackingListRepository.findAllById(request.getId());
        try {
            byte[] result = null;
            Map<String, byte[]> files = new HashMap<>();
            for (ShipmentsPackingList shipmentsPackingList : shipmentsPackingLists) {
                XSSFWorkbook workbook = new XSSFWorkbook();
                FileDTO fileDTO = new FileDTO();
                // export PL
                String invoice = shipmentsPackingList.getInvoice();
                if (CommonDataUtil.isNotEmpty(invoice)) {
                    fileDTO = generateExcelFileDetailPKL(shipmentsPackingList.getId(), workbook, files);
                    files.put(fileDTO.getFileName(), fileDTO.getContent());
                }

                // export CI
                if (shipmentsPackingList.getCommercialInvoiceWH() != null) {
                    FileDTO fileCI = commercialInvoiceWHService.exportExcelByIdCI(shipmentsPackingList.getCommercialInvoiceWH().getId());
                    if (CommonDataUtil.isNotNull(fileCI)) {
                        files.put(fileCI.getFileName(), fileCI.getContent());
                    }
                }
            }

            if (CommonDataUtil.isNotEmpty(files.entrySet())) {
                // zip all file
                result = FileUtil.zip(files);
            }

            if (CommonDataUtil.isNull(result)) {
                throw new BusinessException("Could not created file");
            }
            return result;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public FileDTO exportPKL(ActionSingleIdDTO request) throws IOException {
        FileDTO fileDTO;
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map<String, byte[]> files = new HashMap<>();
        fileDTO = generateExcelFileDetailPKL(request.getId(), workbook, files);
        return fileDTO;
    }

    @Override
    public FileDTO exportCI(ActionSingleIdDTO request) {
        FileDTO fileCI = commercialInvoiceWHService.exportExcelByIdCI(request.getId());
//        if (CommonDataUtil.isNotEmpty(files.entrySet())) {
//            // zip all file
//            result = FileUtil.zip(files);
//        }
        if (CommonDataUtil.isNull(fileCI.getContent())) {
            throw new BusinessException("Could not created file");
        }
        return fileCI;
    }

    @Override
    public boolean updateDate(LogUpdateDateRequestDTO request) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findById(request.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                LocalDate dateAfter = DateUtils.convertStringLocalDateBooking(request.getDate());
                if (request.getTypeUpdateDate().equals(SM_ETD)) {
                    shipment.setEtd(dateAfter);
                }
                Set<ShipmentLogUpdateDate> shipmentLogUpdateDateSet = shipment.getShipmentLogUpdateDates();
                ShipmentLogUpdateDate newestOrderDateBefore = getNewestOrderDateBefore(shipmentLogUpdateDateSet, request.getTypeUpdateDate());
                if (newestOrderDateBefore != null) {
                    LocalDate dateBefore = newestOrderDateBefore.getDateAfter();
                    if (!dateBefore.isEqual(dateAfter)) {
                        ShipmentLogUpdateDate shipmentLogUpdateDate = new ShipmentLogUpdateDate();
                        shipmentLogUpdateDate.setDateBefore(dateBefore);
                        shipmentLogUpdateDate.setDateAfter(dateAfter);
                        shipmentLogUpdateDate.setTypeDate(request.getTypeUpdateDate());
                        shipmentLogUpdateDate.setShipment(shipment);
                        shipmentLogUpdateDate.setCreatedBy(newestOrderDateBefore.getCreatedBy());
                        shipmentLogUpdateDate.setUpdatedBy(request.getUserId());
                        shipmentLogUpdateDateSet.add(shipmentLogUpdateDate);
                        shipment.setShipmentLogUpdateDates(shipmentLogUpdateDateSet);
                    } else {
                        throw new BusinessException("The ship date is the same day before and after the update.");
                    }
                } else {
                    ShipmentLogUpdateDate shipmentLogUpdateDate = new ShipmentLogUpdateDate();
                    shipmentLogUpdateDate.setDateBefore(null);
                    shipmentLogUpdateDate.setDateAfter(dateAfter);
                    shipmentLogUpdateDate.setTypeDate(request.getTypeUpdateDate());
                    shipmentLogUpdateDate.setShipment(shipment);
                    shipmentLogUpdateDate.setCreatedBy(request.getUserId());
                    shipmentLogUpdateDate.setUpdatedBy(request.getUserId());
                    shipmentLogUpdateDateSet.add(shipmentLogUpdateDate);
                    shipment.setShipmentLogUpdateDates(shipmentLogUpdateDateSet);
                }


                shipmentRepository.save(shipment);


                return true;
            } else {
                throw new BusinessException("Purchase Order not found.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }


    private ShipmentLogUpdateDate getNewestOrderDateBefore(Set<ShipmentLogUpdateDate> shipmentLogUpdateDate, String typeDate) {
        return shipmentLogUpdateDate.parallelStream()
            .filter(i -> typeDate.equals(i.getTypeDate())).max(Comparator.comparing(ShipmentLogUpdateDate::getCreatedDate)).orElse(null);
    }

    private void writeHeaderLineDetailPKL(XSSFWorkbook workbook, XSSFSheet sheet, ShipmentsPackingList shipmentsPackingList, Vendor vendor) {
        Row row = sheet.createRow(0);
        int rowCount = 0;
        XSSFCellStyle styleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setFontHeight(14);
        fontTitle.setBold(true);
        styleTitle.setFont(fontTitle);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        createCellNoBorder(row, 0, vendor.getVendorName(), styleTitle);
        ExcelUtil.mergeCell(sheet, 0, 0, 0, 11);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 0, vendor.getFactoryAddress(), styleTitle);
        ExcelUtil.mergeCell(sheet, 1, 1, 0, 11);
        rowCount++;
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 0, "PACKING LIST", styleTitle);
        ExcelUtil.mergeCell(sheet, 3, 3, 0, 11);
        rowCount++;

        XSSFCellStyle styleField = workbook.createCellStyle();
        XSSFFont fontField = workbook.createFont();
        fontField.setFontHeight(11);
        fontField.setBold(true);
        styleField.setFont(fontField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 0, "SOLD TO: ", styleField);
        createCellNoBorder(row, 1, SOLD_TO_COMPANY, styleField);
        createCellNoBorder(row, 8, "INV NO: ", styleField);
        createCellNoBorder(row, 9, shipmentsPackingList.getInvoice(), styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 1, "Address: " + SOLD_TO_ADDRESS, styleField);
        createCellNoBorder(row, 8, "DATE: ", styleField);
        if (shipmentsPackingList.getCommercialInvoiceWH() != null) {
            LocalDate createDate = shipmentsPackingList.getCommercialInvoiceWH().getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = createDate.format(formatter);
            createCellNoBorder(row, 9, formattedDate, styleField);
        } else {
            createCellNoBorder(row, 9, "", styleField);
        }

        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 1, "Phone: " + SOLD_TO_TELEPHONE, styleField);
        createCellNoBorder(row, 8, "P.O No.: ", styleField);
        createCellNoBorder(row, 9, "", styleField);
        rowCount++;

        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        // XSSFColor myColor = new XSSFColor(rgb);
        //style.setFillForegroundColor(myColor);
        // style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        row = sheet.createRow(rowCount + 1);
        int columnCount = 0;
        createCellNoAutoSize(row, columnCount++, "SKU", style);
        createCellNoAutoSize(row, columnCount++, "BARCODE", style);
        createCellNoAutoSize(row, columnCount++, "DESCRIPTION", style);
        createCellNoAutoSize(row, columnCount++, "QUANTITY", style);
        createCellNoAutoSize(row, columnCount++, "QTY OF EACH CARTON", style);
        createCellNoAutoSize(row, columnCount++, "TOTAL CARTON", style);
        createCellNoAutoSize(row, columnCount++, "N.W (KG)", style);
        createCellNoAutoSize(row, columnCount++, "G.W (KG)", style);
        createCellNoAutoSize(row, columnCount++, "MEASUREMENT/M3", style);
        createCellNoAutoSize(row, columnCount++, "TOTAL PALLET QTY", style);
        createCellNoAutoSize(row, columnCount, "CONTAINER/SEAL NUMBER", style);

    }

    public FileDTO generateExcelFileDetailPKL(Integer id, XSSFWorkbook workbook, Map<String, byte[]> files) throws IOException {
        ShipmentsPackingList shipmentsPackingList;
        Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(id);
        if (oShipmentsPackingList.isPresent()) {
            shipmentsPackingList = oShipmentsPackingList.get();

            String fileNamePL = "PackingList_" + shipmentsPackingList.getInvoice() + ".xlsx";
            String fileName = fileNamePL;
            String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
            fileNamePL = filePath + "/" + fileNamePL;
            Path pathFile = Paths.get(fileNamePL);
            XSSFSheet sheet = workbook.createSheet(shipmentsPackingList.getInvoice());
            Optional<Vendor> oVendor = vendorRepository.findByVendorCode(shipmentsPackingList.getSupplier());
            Vendor vendor = new Vendor();
            if (oVendor.isPresent()) {
                vendor = oVendor.get();
            }
            writeHeaderLineDetailPKL(workbook, sheet, shipmentsPackingList, vendor);
            CellStyle style = workbook.createCellStyle();
            CellStyle styleCenter = workbook.createCellStyle();
            styleCenter.setAlignment(HorizontalAlignment.CENTER);
            styleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFCellStyle styleDate = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
            double totalCarton = 0;
            double totalNetWeight = 0;
            double totalGrossWeight = 0;
            double totalCBM = 0;
            Set<ShipmentsContPallet> shipmentsContPallets = shipmentsPackingList.getShipmentsContPallet();
            List<ShipmentsPackingListDetail> details = shipmentsPackingList.getShipmentsPackingListDetail()
                .stream().map(i -> {
                    if (i.getContainerNumber() == null) {
                        i.setContainerNumber("");
                    }
                    return i;
                }).
                collect(Collectors.toList()).
                stream().
                sorted(Comparator.comparing(ShipmentsPackingListDetail::getContainerNumber).
                    reversed()).

                collect(Collectors.toList());
            String previousContainer = "";
            int countRow = 10;
            XSSFCellStyle styleTotal = workbook.createCellStyle();
            XSSFFont fontTotal = workbook.createFont();
            fontTotal.setFontHeight(12);
            styleTotal.setFont(fontTotal);
            byte[] rgb = new byte[3];
            rgb[0] = (byte) 248; // red
            rgb[1] = (byte) 203; // green
            rgb[2] = (byte) 173; // blue
            XSSFColor myColor = new XSSFColor(rgb);
            styleTotal.setFillForegroundColor(myColor);
            styleTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCellStyle styleFieldNumber3 = workbook.createCellStyle();
            styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));

            XSSFCellStyle styleFieldTotalNumber3 = workbook.createCellStyle();
            styleFieldTotalNumber3.setFont(fontTotal);
            styleFieldTotalNumber3.setFillForegroundColor(myColor);
            styleFieldTotalNumber3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleFieldTotalNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
            int rowMerge = 10;
            for (
                ShipmentsPackingListDetail detail : details) {
                Row row = sheet.createRow(countRow);
                int columnCount = 0;
                Optional<Integer> oTotalPallet = shipmentsContPallets.stream().filter(k -> k.getContainerNumber().equals(detail.getContainerNumber())).map(ShipmentsContPallet::getPalletQuantity).findFirst();
                int pallet = 0;
                if (oTotalPallet.isPresent()) {
                    pallet = oTotalPallet.get();
                }
                if (!previousContainer.equals(detail.getContainerNumber()) && countRow > 10) {
                    createCellNoAutoSize(row, columnCount++, "", styleTotal);
                    createCellNoAutoSize(row, columnCount++, "", styleTotal);
                    createCellNoAutoSize(row, columnCount++, "", styleTotal);
                    createCellNoAutoSize(row, columnCount++, "", styleTotal);

                    createCellNoAutoSize(row, columnCount++, "TOTAL:", styleTotal);
                    ExcelUtil.mergeCell(sheet, countRow, countRow, 0, 3);
                    createCellNoAutoSize(row, columnCount++, totalCarton, styleTotal);
                    createCellNoAutoSize(row, columnCount++, totalNetWeight, styleFieldTotalNumber3);

                    createCellNoAutoSize(row, columnCount++, totalGrossWeight, styleFieldTotalNumber3);
                    createCellNoAutoSize(row, columnCount, totalCBM, styleFieldTotalNumber3);
                    totalCarton = 0;
                    totalGrossWeight = 0;
                    totalNetWeight = 0;
                    totalCBM = 0;
                    if (rowMerge == 10) {
                        ExcelUtil.mergeCell(sheet, rowMerge, countRow, 9, 9);
                        ExcelUtil.mergeCell(sheet, rowMerge, countRow, 10, 10);
                    } else {
                        ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 9, 9);
                        ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 10, 10);
                    }
                    rowMerge = countRow;
                    countRow++;
                    row = sheet.createRow(countRow);
                    columnCount = 0;
                }
                createCellNoAutoSize(row, columnCount++, detail.getSku(), style);
                createCellNoAutoSize(row, columnCount++, detail.getBarcode(), style);
                createCellNoAutoSize(row, columnCount++, detail.getProductName(), style);
                createCellNoAutoSize(row, columnCount++, detail.getQuantity(), style);
                createCellNoAutoSize(row, columnCount++, detail.getQtyEachCarton(), style);
                createCellNoAutoSize(row, columnCount++, detail.getTotalCarton(), style);
                createCellNoAutoSize(row, columnCount++, detail.getNetWeight(), styleFieldNumber3);
                createCellNoAutoSize(row, columnCount++, detail.getGrossWeight(), styleFieldNumber3);
                createCellNoAutoSize(row, columnCount++, detail.getTotalVolume(), styleFieldNumber3);
                createCellNoAutoSize(row, columnCount++, pallet, styleCenter);
                createCellNoAutoSize(row, columnCount, detail.getContainerNumber(), styleCenter);
                previousContainer = detail.getContainerNumber();
                countRow++;
                totalCarton += detail.getTotalCarton();
                totalGrossWeight += detail.getGrossWeight();
                totalNetWeight += detail.getNetWeight();
                totalCBM += detail.getTotalVolume();
            }

            Row row = sheet.createRow(countRow);
            int columnCount = 0;

            createCellNoAutoSize(row, columnCount++, "", styleTotal);

            createCellNoAutoSize(row, columnCount++, "", styleTotal);

            createCellNoAutoSize(row, columnCount++, "", styleTotal);
            createCellNoAutoSize(row, columnCount++, "", styleTotal);

            createCellNoAutoSize(row, columnCount++, "TOTAL:", styleTotal);
            ExcelUtil.mergeCell(sheet, countRow, countRow, 0, 3);

            createCellNoAutoSize(row, columnCount++, totalCarton, styleTotal);
            createCellNoAutoSize(row, columnCount++, totalNetWeight, styleFieldTotalNumber3);
            createCellNoAutoSize(row, columnCount++, totalGrossWeight, styleFieldTotalNumber3);

            createCellNoAutoSize(row, columnCount, totalCBM, styleFieldTotalNumber3);
            if (rowMerge == 10) {
                ExcelUtil.mergeCell(sheet, rowMerge, countRow, 9, 9);
                ExcelUtil.mergeCell(sheet, rowMerge, countRow, 10, 10);
            } else {
                ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 9, 9);
                ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 10, 10);
            }

            row = sheet.createRow(countRow + 2);

            createCell(sheet, row, 6, vendor.getVendorName(), style);
            FileOutputStream fos = new FileOutputStream(fileNamePL);
            workbook.write(fos);
            fos.close();
            FileDTO fileDTO = new FileDTO();
            byte[] dataPL = Files.readAllBytes(pathFile);
            Files.deleteIfExists(pathFile);
            fileDTO.setContent(dataPL);
            fileDTO.setFileName(fileName);
            return fileDTO;
        }
        return null;
    }

    private void createCellNoAutoSize(Row row, int columnCount, Object valueOfCell, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void createCellNoBorder(Row row, int columnCount, Object valueOfCell, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);

    }

    public Workbook generateExcelFileDetailPKL(ActionSingleIdDTO request, XSSFWorkbook workbook) {
        ShipmentsPackingList shipmentsPackingList;
        Set<ShipmentsPackingListDetail> detailSet = new HashSet<>();
        Optional<ShipmentsPackingList> oShipmentsPackingList = shipmentPackingListRepository.findById(request.getId());
        if (oShipmentsPackingList.isPresent()) {
            shipmentsPackingList = oShipmentsPackingList.get();
            detailSet.addAll(shipmentsPackingList.getShipmentsPackingListDetail());
        } else {
            return null;
        }
        XSSFSheet sheet = workbook.createSheet("Detail PKL");
        int rowCount = 1;
        writeHeaderLineDetailPKL(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        XSSFCellStyle styleFieldNumber3 = workbook.createCellStyle();
        styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        for (ShipmentsPackingListDetail detail : detailSet) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, detail.getSku(), style);
            createCell(sheet, row, columnCount++, detail.getProductName(), style);
            createCell(sheet, row, columnCount++, detail.getQuantity(), style);
            createCell(sheet, row, columnCount++, detail.getQtyEachCarton(), style);
            createCell(sheet, row, columnCount++, detail.getTotalCarton(), style);
            createCell(sheet, row, columnCount++, detail.getTotalVolume(), styleFieldNumber3);
            createCell(sheet, row, columnCount++, detail.getNetWeight(), styleFieldNumber3);
            createCell(sheet, row, columnCount++, detail.getGrossWeight(), styleFieldNumber3);
            createCell(sheet, row, columnCount++, detail.getContainerNumber(), style);
            createCell(sheet, row, columnCount, detail.getContainerType(), style);
            createCell(sheet, row, columnCount, detail.getProformaInvoiceNo(), style);
        }
        return workbook;
    }

    public Workbook generateExcelFileDetailCI(ActionSingleIdDTO request, XSSFWorkbook workbook) {
        CommercialInvoiceWH commercialInvoiceWH;
        Set<CommercialInvoiceWHDetail> detailSet = new HashSet<>();
        Optional<CommercialInvoiceWH> oCommercialInvoiceWH = commercialInvoiceWHRepository.findById(request.getId());
        if (oCommercialInvoiceWH.isPresent()) {
            commercialInvoiceWH = oCommercialInvoiceWH.get();
            detailSet.addAll(commercialInvoiceWH.getCommercialInvoiceWHDetail());
        } else {
            return null;
        }
        XSSFSheet sheet = workbook.createSheet("Detail PKL");
        int rowCount = 1;
        writeHeaderLineDetailCI(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        XSSFCellStyle styleFieldNumber3 = workbook.createCellStyle();
        styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        XSSFCellStyle styleFieldNumber2 = workbook.createCellStyle();
        styleFieldNumber2.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        for (CommercialInvoiceWHDetail detail : detailSet) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, detail.getSku(), style);
            createCell(sheet, row, columnCount++, detail.getaSin(), style);
            createCell(sheet, row, columnCount++, detail.getProductTitle(), style);
            createCell(sheet, row, columnCount++, detail.getQty(), style);
            createCell(sheet, row, columnCount++, detail.getUnitPrice(), styleFieldNumber2);
            createCell(sheet, row, columnCount, detail.getAmount(), styleFieldNumber3);
        }
        return workbook;
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
        } else if (valueOfCell instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) valueOfCell).doubleValue());
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void writeHeaderLineDetailPKL(XSSFWorkbook workbook, XSSFSheet sheet) {
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
        createCell(sheet, row, i++, "PRODUCT NAME", style);
        createCell(sheet, row, i++, "QUANTITY", style);
        createCell(sheet, row, i++, "QTY OF EACH CARTON", style);
        createCell(sheet, row, i++, "TOTAL CARTON", style);
        createCell(sheet, row, i++, "MEASUREMENT/M3", style);
        createCell(sheet, row, i++, "NW (KG)", style);
        createCell(sheet, row, i++, "GW (KG)", style);
        createCell(sheet, row, i++, "CONTAINER/SEAL NUMBER", style);
        createCell(sheet, row, i, "CONTAINER TYPE", style);
        createCell(sheet, row, i, "PROFORMA INVOICE NO", style);
    }

    private void writeHeaderLineDetailCI(XSSFWorkbook workbook, XSSFSheet sheet) {
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
        createCell(sheet, row, i++, "BARCODE", style);
        createCell(sheet, row, i++, "PRODUCT NAME", style);
        createCell(sheet, row, i++, "QUANTITY", style);
        createCell(sheet, row, i++, "UNIT PRICE(USD)", style);
        createCell(sheet, row, i++, "AMOUNT (USD)", style);
    }

    @Override
    public ListFilterShipmentDTO getListFilter() {
        ListFilterShipmentDTO data = new ListFilterShipmentDTO();
        List<WareHouse> wareHouses = wareHouseRepository.findAll();
        List<Ports> ports = portsRepository.findAll();
        List<WareHouseDTO> wareHouseDTOList = wareHouses.parallelStream().map(i -> mappingEntityToDtoWH(i, WareHouseDTO.class)).collect(Collectors.toList());
        List<PortsDTO> portsOfLoadings = ports.parallelStream().filter(i -> i.getPublished().equals(1) && i.getCanExport().equals(1)).map(i -> mappingEntityToDtoPort(i, PortsDTO.class)).collect(Collectors.toList());
        List<PortsDTO> portsOfDischarges = ports.parallelStream().filter(i -> i.getPublished().equals(1) && i.getCanImport().equals(1)).map(i -> mappingEntityToDtoPort(i, PortsDTO.class)).collect(Collectors.toList());
        List<User> user = userRepository.findAll();
        Set<String> suppliers = new HashSet<>();
        user.stream().filter(i -> i.getVendor() != null && i.getVendor().length() > 0).forEach(i -> suppliers.add(i.getVendor()));
        data.setWareHouseDTOs(wareHouseDTOList);
        data.setPortsOfDischarges(portsOfDischarges);
        data.setPortsOfLoadings(portsOfLoadings);
        data.setSuppliers(suppliers);
        return data;
    }


    @Override
    public ShipmentDTO createShipment(ShipmentDTO shipmentDTO) {
        try {
            Shipment shipment = new Shipment();
            BeanUtils.copyProperties(shipmentDTO, shipment);
            final double[] totalAmount = {0};

            Optional<Integer> oShipmentId = shipmentRepository.findMaxShipmentId();
            if (oShipmentId.isPresent()) {
                String shipmentId = String.format("%05d", oShipmentId.get() + 1);
                shipment.setShipmentId("Y4A" + shipmentId);
            } else {
                String shipmentId = String.format("%05d", 1);
                shipment.setShipmentId("Y4A" + shipmentId);
            }


            Set<ShipmentsPurchaseOrders> detailSet = shipmentDTO.getShipmentsPurchaseOrders().parallelStream().map(item -> {
                Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(item.getPurchaseOrderId());
                if (oPurchaseOrdersWH.isPresent()) {
                    PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                    purchaseOrdersWH.setStatus(STATUS_PO_WH_SHIPMENT_CREATED);
                    purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                } else {
                    throw new BusinessException("Can not find Purchaser Order");
                }
                List<ShipmentsPurchaseOrders> shipmentsPurchaseOrderSet = shipmentsPurchaseOrdersRepository.findAllByPurchaseOrderIdAndId(item.getPurchaseOrderId(), -1);
                if (shipmentsPurchaseOrderSet.isEmpty()) {
                    ShipmentsPurchaseOrders shipmentsPurchaseOrders = new ShipmentsPurchaseOrders();
                    CommonDataUtil.getModelMapper().map(item, shipmentsPurchaseOrders);
                    Set<ShipmentsPurchaseOrdersDetail> shipmentsPurchaseOrdersDetails = new HashSet<>();
                    shipmentsPurchaseOrdersDetails = item.getShipmentsPurchaseOrdersDetail().parallelStream().map(i -> {
                        ShipmentsPurchaseOrdersDetail shipmentsPurchaseOrdersDetail = new ShipmentsPurchaseOrdersDetail();
                        CommonDataUtil.getModelMapper().map(i, shipmentsPurchaseOrdersDetail);
                        totalAmount[0] += shipmentsPurchaseOrdersDetail.getAmount();
                        return shipmentsPurchaseOrdersDetail;
                    }).collect(Collectors.toSet());
                    shipmentsPurchaseOrders.setShipmentsPurchaseOrdersDetail(shipmentsPurchaseOrdersDetails);
                    return shipmentsPurchaseOrders;
                } else {
                    throw new BusinessException(String.format("Purchase Order %s exists in another shipment", item.getPurchaseOrderNo()));
                }
            }).collect(Collectors.toSet());
            Set<ShipmentsContQty> shipmentsContQtySet = shipmentDTO.getShipmentsContQty().parallelStream().map(item -> {
                ShipmentsContQty shipmentsContQty = new ShipmentsContQty();
                CommonDataUtil.getModelMapper().map(item, shipmentsContQty);
                return shipmentsContQty;
            }).collect(Collectors.toSet());
            shipment.setShipmentsPurchaseOrders(detailSet);
            shipment.setShipmentsContQty(shipmentsContQtySet);
            shipment.setStatus(STATUS_SHIPMENT_STARTUP);

            shipment.setTotalAmount(totalAmount[0]);
            shipmentRepository.saveAndFlush(shipment);
            return mappingEntityToDto(shipment, ShipmentDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public ShipmentDTO updateShipment(ShipmentDTO shipmentDTO) {
        try {
            Optional<Shipment> oShipment = shipmentRepository.findById(shipmentDTO.getId());
            if (oShipment.isPresent()) {
                Shipment shipment = oShipment.get();
                //check change ATA
                boolean isChangeATA = false;
                if (shipmentDTO.getAta() != null && (shipment.getAta() == null || !shipmentDTO.getAta().equals(shipment.getAta()))) {
                    isChangeATA = true;
                }
                boolean isChangeATD = false;
                if (shipmentDTO.getAtd() != null && (shipment.getAtd() == null || !shipmentDTO.getAtd().equals(shipment.getAtd()))) {
                    isChangeATD = true;
                }
                if (isChangeATA && !shipment.getStatus().equals(STATUS_SHIPMENT_PIPELINE)) {
                    throw new BusinessException("Only the status shipment is PIPELINE must be to update ATA.");
                }
                if (isChangeATD && shipment.getStatus() >= STATUS_SHIPMENT_DOCKED_US) {
                    throw new BusinessException("Only the status of the shipment before Docker US must be to update ATD");
                }
                BeanUtils.copyProperties(shipmentDTO, shipment);
                final double[] totalAmount = {0};
                Set<ShipmentsPurchaseOrders> detailSetOld = new HashSet<>();
                detailSetOld = shipment.getShipmentsPurchaseOrders().stream().map(i -> {
                    i.setShipment(null);
                    Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(i.getPurchaseOrderId());
                    if (oPurchaseOrdersWH.isPresent()) {
                        PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                        purchaseOrdersWH.setStatus(STATUS_PO_PI_CONFIRMED);
                        purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                    } else {
                        throw new BusinessException("Can not find Purchaser Order");
                    }
                    return i;
                }).collect(Collectors.toSet());
                Set<ShipmentsContQty> shipmentsContQtySetOld = new HashSet<>();
                shipmentsContQtySetOld = shipment.getShipmentsContQty().stream().map(i -> {
                    i.setShipment(null);
                    return i;
                }).collect(Collectors.toSet());
                boolean finalIsChangeATA = isChangeATA;
                Set<ShipmentsPurchaseOrders> detailSet = shipmentDTO.getShipmentsPurchaseOrders().parallelStream().map(item -> {
                    Optional<PurchaseOrdersWH> oPurchaseOrdersWH = purchaseOrdersWHRepository.findById(item.getPurchaseOrderId());
                    if (finalIsChangeATA) {
                        if (oPurchaseOrdersWH.isPresent()) {
                            PurchaseOrdersWH purchaseOrdersWH = oPurchaseOrdersWH.get();
                            purchaseOrdersWH.setStatus(STATUS_PO_WH_DOCKED_US);
                            purchaseOrdersWHRepository.saveAndFlush(purchaseOrdersWH);
                        } else {
                            throw new BusinessException("Can not find Purchaser Order");
                        }
                    }
                    //get detail purchaser order
                    List<ShipmentsPurchaseOrders> shipmentsPurchaseOrderSet = shipmentsPurchaseOrdersRepository.findAllByPurchaseOrderIdAndId(item.getPurchaseOrderId(), shipmentDTO.getId());
                    if (shipmentsPurchaseOrderSet.isEmpty()) {
                        ShipmentsPurchaseOrders shipmentsPurchaseOrders = new ShipmentsPurchaseOrders();
                        CommonDataUtil.getModelMapper().map(item, shipmentsPurchaseOrders);
                        Set<ShipmentsPurchaseOrdersDetail> shipmentsPurchaseOrdersDetails = new HashSet<>();
                        shipmentsPurchaseOrdersDetails = item.getShipmentsPurchaseOrdersDetail().parallelStream().map(i -> {
                            ShipmentsPurchaseOrdersDetail shipmentsPurchaseOrdersDetail = new ShipmentsPurchaseOrdersDetail();
                            CommonDataUtil.getModelMapper().map(i, shipmentsPurchaseOrdersDetail);
                            totalAmount[0] += shipmentsPurchaseOrdersDetail.getAmount();

                            return shipmentsPurchaseOrdersDetail;
                        }).collect(Collectors.toSet());
                        shipmentsPurchaseOrders.setShipmentsPurchaseOrdersDetail(shipmentsPurchaseOrdersDetails);
                        return shipmentsPurchaseOrders;
                    } else {
                        throw new BusinessException(String.format("Purchase Order %s exists in another shipment", item.getPurchaseOrderNo()));
                    }
                }).collect(Collectors.toSet());
                //get container quantity
                Set<ShipmentsContQty> shipmentsContQtySet = shipmentDTO.getShipmentsContQty().parallelStream().map(item -> {
                    ShipmentsContQty shipmentsContQty = new ShipmentsContQty();
                    CommonDataUtil.getModelMapper().map(item, shipmentsContQty);
                    return shipmentsContQty;
                }).collect(Collectors.toSet());
                shipment.setShipmentsPurchaseOrders(detailSet);
                shipment.setShipmentsContQty(shipmentsContQtySet);
                shipment.setTotalAmount(totalAmount[0]);
                //log change field for shipment
                Set<ShipmentLogUpdateField> shipmentLogUpdateFields = shipment.getShipmentLogUpdateFields().parallelStream().filter(i -> i.getField().equals(SM_FILED_CONT)).collect(Collectors.toSet());
                if (shipmentsContQtySetOld != null) {
                    shipmentsContQtySetOld.stream().forEach(old -> {
                        shipmentsContQtySet.parallelStream().filter(item -> item.getContainerType().equals(old.getContainerType())).forEach(newCont -> {
                            if (!newCont.getQuantity().equals(old.getQuantity())) {
                                ShipmentLogUpdateField shipmentLogUpdateField = new ShipmentLogUpdateField();
                                shipmentLogUpdateField.setValueBefore(String.valueOf(old.getQuantity()));
                                shipmentLogUpdateField.setValueAfter(String.valueOf(newCont.getQuantity()));
                                shipmentLogUpdateField.setField(SM_FILED_CONT);
                                shipmentLogUpdateField.setFieldValue(newCont.getContainerType());
                                shipmentLogUpdateField.setShipment(shipment);
                                shipmentLogUpdateField.setCreatedBy(shipmentDTO.getUpdatedBy());
                                shipmentLogUpdateField.setUpdatedBy(shipmentDTO.getUpdatedBy());
                                shipmentLogUpdateFields.add(shipmentLogUpdateField);

                            }
                        });
                    });
                } else {
                    shipmentsContQtySet.parallelStream().forEach(newCont -> {
                        ShipmentLogUpdateField shipmentLogUpdateField = new ShipmentLogUpdateField();
                        shipmentLogUpdateField.setValueBefore(null);
                        shipmentLogUpdateField.setValueAfter(String.valueOf(newCont.getQuantity()));
                        shipmentLogUpdateField.setField(SM_FILED_CONT);
                        shipmentLogUpdateField.setShipment(shipment);
                        shipmentLogUpdateField.setFieldValue(newCont.getContainerType());
                        shipmentLogUpdateField.setCreatedBy(shipmentDTO.getUpdatedBy());
                        shipmentLogUpdateField.setUpdatedBy(shipmentDTO.getUpdatedBy());
                        shipmentLogUpdateFields.add(shipmentLogUpdateField);
                    });
                }
                shipment.setShipmentLogUpdateField(shipmentLogUpdateFields);
                if (isChangeATA) {
                    shipment.setStatus(STATUS_SHIPMENT_DOCKED_US);
                }
                shipmentsPurchaseOrdersRepository.deleteAll(detailSetOld);
                shipmentsContainerPalletRepository.deleteAll(shipmentsContQtySetOld);
                // if change ata should be update field eta to Container
                if (isChangeATA) {
                    Set<ShipmentsContainers> shipmentsContainer = shipment.getShipmentsContainers().parallelStream().map(i -> {
                        i.setEtaWh(shipment.getEta().plusDays(7).atStartOfDay());
                        return i;
                    }).collect(Collectors.toSet());
                    shipment.setShipmentsContainers(shipmentsContainer);
                }
                shipmentRepository.saveAndFlush(shipment);
                return mappingEntityToDto(shipment, ShipmentDTO.class);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
        return null;
    }


    public void updateStatusResource(Shipment shipment, Integer status) {
        if (status.equals(STATUS_SHIPMENT_REVIEW_BROKER)) {
            shipment.setStatusUsBroker(STATUS_SHIPMENT_US_BROKER_UPLOADED);
            shipment.setStatus(status);
            shipmentRepository.saveAndFlush(shipment);
        } else {
            shipment.setStatus(status);
            shipmentRepository.saveAndFlush(shipment);
        }
    }


    private <T> T mappingEntityToDto(Shipment shipment, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(shipment, dto);
            Optional<User> oUserUpdated = userRepository.findOneByLogin(shipment.getUpdatedBy());
            String updatedBy = "";
            if (oUserUpdated.isPresent()) {
                updatedBy = oUserUpdated.get().getLastName() + " " + oUserUpdated.get().getFirstName();
            }

            Optional<User> oUserCreated = userRepository.findOneByLogin(shipment.getCreatedBy());
            String createdBy = "";
            if (oUserCreated.isPresent()) {
                createdBy = oUserCreated.get().getLastName() + " " + oUserCreated.get().getFirstName();
            }

            if (dto instanceof ShipmentMainDTO) {
                Set<ListPODTO> purchaseOrder = new HashSet<>();
                shipment.getShipmentsPurchaseOrders().parallelStream().forEach(item -> {
                    ListPODTO listPODTO = new ListPODTO();
                    listPODTO.setId(item.getPurchaseOrderId());
                    listPODTO.setInvoice(item.getPurchaseOrderNo());
                    purchaseOrder.add(listPODTO);
                });
                clazz.getMethod("setPurchaseOrder", Set.class).invoke(dto, purchaseOrder);
                clazz.getMethod("setUpdatedBy", String.class).invoke(dto, updatedBy);
                clazz.getMethod("setCreatedBy", String.class).invoke(dto, createdBy);
            } else {
                Set<ShipmentsQuantityDTO> shipmentsQuantityDTOS = getListOrderedQuantity(shipment);
                clazz.getMethod("setShipmentsQuantity", Set.class).invoke(dto, shipmentsQuantityDTOS);
            }
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

    private <T> T mappingEntityToDtoWH(WareHouse wareHouse, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(wareHouse, dto);
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

    private <T> T mappingEntityToDtoContainers(ShipmentsContainers shipmentsContainers, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(shipmentsContainers, dto);
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

    @Override
    public String checkPermissionUser(String userId, TabAction action, Tab tab) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(userId);
            User user = oUser.get();
            Set<String> authorities = userService.checkUserWithRole(oUser.get());
            if (!containsPOMS(authorities)) {
                throw new BusinessException(ERRORS_PERMISSION);
            }
            String supplier = user.getVendor();
            boolean isSupplier = false;
            boolean isPU = false;
            if (!CommonDataUtil.isEmpty(supplier)) {
                isSupplier = true;
            } else {
                if (authorities.contains(POMS_USER)) {
                    isPU = true;
                }
            }
            if (tab.equals(Tab.INFORMATION)) {
                if (action.equals(TabAction.VIEW) && (authorities.contains(POMS_LOCAL_BROKER))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && !isPU) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.DELETE) && !isPU) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.LOGISTIC)) {
                if (action.equals(TabAction.VIEW) && (authorities.contains(POMS_LOCAL_BROKER) || authorities.contains(POMS_WAREHOUSE))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && !authorities.contains(POMS_LOGISTIC)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.ORDER_QUANTITY)) {
                if (action.equals(TabAction.VIEW) && (!authorities.contains(POMS_LOGISTIC) && !isPU)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.CONTAINER)) {
                if (action.equals(TabAction.VIEW) && (!authorities.contains(POMS_LOGISTIC) && !isPU && !authorities.contains(POMS_WAREHOUSE))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.CONTAINER_DETAIL)) {
                if (action.equals(TabAction.VIEW) && (!authorities.contains(POMS_LOGISTIC) && !isPU && !authorities.contains(POMS_WAREHOUSE))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && (!authorities.contains(POMS_LOGISTIC) && !authorities.contains(POMS_WAREHOUSE))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.LOCAL_BROKER)) {
                if (action.equals(TabAction.VIEW) && (isSupplier || authorities.contains(POMS_WAREHOUSE))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && (!authorities.contains(POMS_LOCAL_BROKER))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.SUPPLIER_DOCS)) {
                if (action.equals(TabAction.VIEW) && (authorities.contains(POMS_WAREHOUSE) || authorities.contains(POMS_LOCAL_BROKER))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && (!isSupplier)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.CONFIRM) && (!isPU)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.REJECT) && (!isPU)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            } else if (tab.equals(Tab.US_BROKER)) {
                if (action.equals(TabAction.VIEW) && (authorities.contains(POMS_WAREHOUSE) || authorities.contains(POMS_LOCAL_BROKER) || isSupplier)) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.EDIT) && (!authorities.contains(POMS_LOGISTIC) || !authorities.contains(POMS_BROKER))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.CONFIRM) && (!authorities.contains(POMS_LOGISTIC))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                } else if (action.equals(TabAction.REJECT) && (!authorities.contains(POMS_LOGISTIC))) {
                    throw new BusinessException(ERRORS_PERMISSION);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }
        return "";
    }

    public boolean containsPOMS(Set<String> authorities) {
        return authorities.stream().anyMatch(authority -> authority.contains("POMS"));
    }

    private <T> T mappingEntityToDtoPort(Ports port, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(port, dto);
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

}
