package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.CommercialInvoiceService;
import com.yes4all.service.SendMailService;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;


/**
 * Service Implementation for managing {@link CommercialInvoice}.
 */
@Service
@Transactional
public class CommercialInvoiceServiceImpl implements CommercialInvoiceService {

    private final Logger log = LoggerFactory.getLogger(CommercialInvoiceServiceImpl.class);
    private static final String LINK_DETAIL_PKL = "/booking/packing-list/detail/";
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SendMailService sendMailService;
    @Value("${attribute.link.url}")
    private String linkPOMS;

    @Autowired
    private CommercialInvoiceDetailRepository commercialInvoiceDetailRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;
    @Autowired
    private BookingRepository bookingRepository;


    @Override
    public Page<CommercialInvoiceMainDTO> listingCommercialInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<CommercialInvoice> data = commercialInvoiceRepository.findByCondition(filterParams.get("fromSO"), filterParams.get("invoiceNo"),
            filterParams.get("term"), filterParams.get("shipDateFrom"), filterParams.get("shipDateTo"),
            filterParams.get("amountFrom"), filterParams.get("amountTo"), filterParams.get("status"),
            filterParams.get("updatedDateFrom"), filterParams.get("updatedDateTo"), filterParams.get("supplier"), pageable);
        return data.map(item -> mappingEntityToDto(item, CommercialInvoiceMainDTO.class));
    }


    @Override
    @Transactional(readOnly = true)
    public CommercialInvoiceDTO getCommercialInvoiceDetail(BodyGetDetailDTO request,Boolean isViewPKL) {
        try {
            Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(request.getId());
            if (oCommercialInvoice.isPresent()) {
                CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                if ((request.getVendor().length() > 0 && !request.getVendor().equals(commercialInvoice.getSupplier())
                    || (request.getVendor().length() == 0 && Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_NEW))) && !isViewPKL) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                Set<CommercialInvoiceDetail> commercialInvoiceDetail = commercialInvoiceDetailRepository.findByCommercialInvoice(commercialInvoice);
                commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetail);
                CommercialInvoiceDTO commercialInvoiceDTO = CommonDataUtil.getModelMapper().map(commercialInvoice, CommercialInvoiceDTO.class);
//                //role Y4A
//                if (!request.getIsSupplier()) {
//                    //handle UI with status CI Reject
//                    if (commercialInvoice.getStatus() == GlobalConstant.STATUS_CI_REJECT) {
//                        List<CommercialInvoiceDetailDTO> detailDTOSet;
//                        detailDTOSet = commercialInvoiceDTO.getCommercialInvoiceDetail().stream().map(item -> {
//                            List<CommercialInvoiceDetailLogDTO> detailLogSet = item.getCommercialInvoiceDetailLog();
//                            item.setCommercialInvoiceDetailLog(detailLogSet);
//                            Optional<CommercialInvoiceDetailLogDTO> oInvoiceDetailLogMaxVersion = detailLogSet.stream().max(Comparator.comparing(CommercialInvoiceDetailLogDTO::getVersion));
//                            if (oInvoiceDetailLogMaxVersion.isPresent()) {
//                                item.setUnitPrice(oInvoiceDetailLogMaxVersion.get().getUnitPriceAfter());
//                                item.setAmount(oInvoiceDetailLogMaxVersion.get().getAmountAfter());
//                            } else {
//                                throw new BusinessException("Can not find log detail");
//                            }
//                            return item;
//                        }).collect(Collectors.toList());
//                        commercialInvoiceDTO.setCommercialInvoiceDetail(detailDTOSet);
//                    }
//                }
                //get information user total amount log
                List<CommercialInvoiceToTalAmountLogDTO> commercialInvoiceToTalAmountLogDTO;
                commercialInvoiceToTalAmountLogDTO = commercialInvoice.getCommercialInvoiceTotalAmountLog().stream().map(item -> {
                    CommercialInvoiceToTalAmountLogDTO invoiceToTalAmountLogDTO;
                    invoiceToTalAmountLogDTO = CommonDataUtil.getModelMapper().map(item, CommercialInvoiceToTalAmountLogDTO.class);
                    Optional<User> oUser = userRepository.findOneByLogin(item.getUpdatedBy());
                    String updatedBy = "";
                    if (oUser.isPresent()) {
                        updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                        invoiceToTalAmountLogDTO.setUpdatedNameBy(updatedBy);
                    }
                    return invoiceToTalAmountLogDTO;
                }).filter(Objects::nonNull).sorted(Comparator.comparing(CommercialInvoiceToTalAmountLogDTO::getVersion).reversed()).collect(Collectors.toList());

                commercialInvoiceDTO.setCommercialInvoiceToTalAmountLog(commercialInvoiceToTalAmountLogDTO);
                //get resource
                List<Resource> resourcesListing = resourceRepository.findByFileTypeAndCommercialInvoiceId(GlobalConstant.FILE_UPLOAD, commercialInvoice.getId());
                if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                    List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                        ResourceDTO data = new ResourceDTO();
                        BeanUtils.copyProperties(item, data);
                        return data;
                    }).collect(Collectors.toList());
                    commercialInvoiceDTO.setFileUploads(resources);
                }
                List<CommercialInvoiceDetailDTO> invoiceDetailDTOS;
                //get information user detail log
                invoiceDetailDTOS = commercialInvoiceDTO.getCommercialInvoiceDetail().stream().map(i -> {
                    List<CommercialInvoiceDetailLogDTO> commercialInvoiceDetailLogDTOSet = i.getCommercialInvoiceDetailLog();
                    List<CommercialInvoiceDetailLogDTO> invoiceDetailLogDTOSet = null;
                    if (commercialInvoiceDetailLogDTOSet != null) {
                        invoiceDetailLogDTOSet = commercialInvoiceDetailLogDTOSet.stream().map(item -> {
                            Optional<User> oUser = userRepository.findOneByLogin(item.getUpdatedBy());
                            String updatedBy = "";
                            if (oUser.isPresent()) {
                                updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                                item.setUpdatedNameBy(updatedBy);
                            }
                            return item;
                        }).collect(Collectors.toList());
                    }
                    i.setCommercialInvoiceDetailLog(invoiceDetailLogDTOSet.stream().sorted(Comparator.comparing(CommercialInvoiceDetailLogDTO::getVersion).reversed()).collect(Collectors.toList()));
                    return i;
                }).sorted(Comparator.comparing(CommercialInvoiceDetailDTO::getSku)).collect(Collectors.toList());
                commercialInvoiceDTO.setCommercialInvoiceDetail(invoiceDetailDTOS);
                commercialInvoiceDTO.setIsViewCI(request.getIsViewCI() == null);
                return commercialInvoiceDTO;

            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CommercialInvoiceDTO getCommercialInvoiceDetailWithInvoiceNo(String invoiceNo) {

        Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findByInvoiceNoAndIsDeleted(invoiceNo, false);
        if (oCommercialInvoice.isPresent()) {
            CommercialInvoice commercialInvoice = oCommercialInvoice.get();
            Set<CommercialInvoiceDetail> commercialInvoiceDetail = commercialInvoiceDetailRepository.findByCommercialInvoice(commercialInvoice);
            commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetail);
            CommercialInvoiceDTO result = CommonDataUtil.getModelMapper().map(commercialInvoice, CommercialInvoiceDTO.class);
            List<Resource> resourcesListing = resourceRepository.findByFileTypeAndCommercialInvoiceId(GlobalConstant.FILE_UPLOAD, commercialInvoice.getId());
            if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                    ResourceDTO data = new ResourceDTO();
                    data.setPath(item.getPath());
                    data.setId(item.getId());
                    data.setModule(item.getModule());
                    return data;
                }).collect(Collectors.toList());
                result.setFileUploads(resources);
            }
            return result;

        }
        return null;
    }

    @Override
    public CommercialInvoiceDTO createCommercialInvoice(CommercialInvoiceDTO request) {
        try {
            CommercialInvoice commercialInvoice = new CommercialInvoice();
            BeanUtils.copyProperties(request, commercialInvoice);
            Optional<CommercialInvoice> oCommercialInvoiceDuplicate = commercialInvoiceRepository.findByInvoiceNoAndIsDeleted(request.getInvoiceNo(), false);
            if (oCommercialInvoiceDuplicate.isPresent()) {
                throw new BusinessException(String.format("Invoice No %s already exists.", request.getInvoiceNo()));
            }
            commercialInvoice.setStatus(GlobalConstant.STATUS_CI_NEW);

            //packing list
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(request.getBookingPackingListId());

            //get details PI from DTO
            Set<CommercialInvoiceDetail> detailSet = request.getCommercialInvoiceDetail().parallelStream().map(item -> {
                CommercialInvoiceDetail commercialInvoiceDetail;
                commercialInvoiceDetail = CommonDataUtil.getModelMapper().map(item, CommercialInvoiceDetail.class);
                Optional<ProformaInvoice> oProformaInvoiceDetail = proformaInvoiceRepository.findByOrderNo(item.getProformaInvoiceNo());
                if (oProformaInvoiceDetail.isPresent()) {
                    ProformaInvoice proformaInvoiceDetail = oProformaInvoiceDetail.get();
                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoiceDetail.getProformaInvoiceDetail();
                    ProformaInvoiceDetail proformaInvoiceDetailElement = proformaInvoiceDetailSet.stream().filter(k -> k.getFromSo().equals(commercialInvoiceDetail.getFromSo())
                        && k.getSku().equals(commercialInvoiceDetail.getSku())).findFirst().orElse(null);
                    if (proformaInvoiceDetailElement == null) {
                        throw new BusinessException(String.format("Sku= %s ; SO= %s not exists in PI", commercialInvoiceDetail.getSku(), commercialInvoiceDetail.getFromSo()));
                    } else {
                        commercialInvoiceDetail.setUnitPrice(proformaInvoiceDetailElement.getUnitPrice());
                        commercialInvoiceDetail.setAmount(proformaInvoiceDetailElement.getUnitPrice() * commercialInvoiceDetail.getQty());
                    }
                } else {
                    throw new BusinessException("ProformaInvoice Not exists!");
                }
                return commercialInvoiceDetail;
            }).collect(Collectors.toSet());
            commercialInvoice.setCommercialInvoiceDetail(detailSet);
            commercialInvoice.setAmount(detailSet.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));

            oBookingPackingList.ifPresent(commercialInvoice::setBookingPackingList);
            commercialInvoiceRepository.saveAndFlush(commercialInvoice);

            return mappingEntityToDto(commercialInvoice, CommercialInvoiceDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public CommercialInvoiceDTO updateCommercialInvoice(Integer id, CommercialInvoiceDTO request) {

        try {
            Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(id);
            CommercialInvoice commercialInvoice;
            if (oCommercialInvoice.isEmpty()) {
                throw new BusinessException("Invoice not found.");
            }
            commercialInvoice = oCommercialInvoice.get();


            if (commercialInvoice.getStatus() != GlobalConstant.STATUS_CI_NEW && commercialInvoice.getSupplierUpdatedLatest() != null && commercialInvoice.getSupplierUpdatedLatest() == request.getIsSupplier()) {
                throw new BusinessException("Supplier has adjusted so can not adjust.");
            }
            if (commercialInvoice.getStatus() != GlobalConstant.STATUS_CI_NEW && commercialInvoice.getSupplierUpdatedLatest() != null && !commercialInvoice.getSupplierUpdatedLatest() == !request.getIsSupplier()) {
                throw new BusinessException("Ye4all has adjusted so can not adjust.");
            }
            int totalQuantity = commercialInvoice.getCommercialInvoiceDetail().stream().map(x -> Objects.isNull(x.getQty()) ? 0 : x.getQty()).reduce(0, Integer::sum);

            double ratioTruckingCost = 0;
            if (request.getTruckingCost() != null) {
                ratioTruckingCost = request.getTruckingCost() / totalQuantity;

            }
            final int[] count = {0};
            final double[] totalAmountAllocated = {0};
            final double[] totalAmount = {0};
            final int[] size = {commercialInvoice.getCommercialInvoiceDetail().size()};

            double finalRatioTruckingCost = ratioTruckingCost;
            Set<CommercialInvoiceDetail> detailSet = request.getCommercialInvoiceDetail().stream().map(item -> {
                CommercialInvoiceDetail commercialInvoiceDetail;
                commercialInvoiceDetail = CommonDataUtil.getModelMapper().map(item, CommercialInvoiceDetail.class);
                if (item.getUnitPrice() == null) {
                    throw new BusinessException("Unit price can not empty.");
                }
                if (request.getTruckingCost() == null || request.getTruckingCost() == 0) {
                    commercialInvoiceDetail.setUnitPriceAllocated(commercialInvoiceDetail.getUnitPrice());
                }
                if (request.getIsSupplier() && request.getTruckingCost() != null && request.getTruckingCost() > 0) {
                    count[0]++;
                    //allocate the latest detail
                    if (count[0] == size[0]) {
                        double totalRemain = request.getTruckingCost() - (totalAmountAllocated[0] - totalAmount[0]);
                        double unitPriceAllocate = Double.parseDouble(df.format(totalRemain / item.getQty() + item.getUnitPrice()));
                        commercialInvoiceDetail.setUnitPriceAllocated(unitPriceAllocate);
                        double amountAllocated = Double.parseDouble(df.format(item.getQty() * item.getUnitPrice())) + totalRemain;
                        commercialInvoiceDetail.setAmount(amountAllocated);
                    } else {
                        totalAmount[0] += Double.parseDouble(df.format(item.getQty() * item.getUnitPrice()));
                        double unitPriceAllocate = item.getUnitPrice() + finalRatioTruckingCost;
                        double amountAllocated = Double.parseDouble(df.format(unitPriceAllocate * item.getQty()));
                        double unitPriceAllocateRound = Double.parseDouble(df.format(unitPriceAllocate));
                        totalAmountAllocated[0] += amountAllocated;
                        commercialInvoiceDetail.setAmount(amountAllocated);
                        commercialInvoiceDetail.setUnitPriceAllocated(unitPriceAllocateRound);
                    }
                }
                //write log detail
                if (Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                    Set<CommercialInvoiceDetailLog> commercialInvoiceDetailLogs = commercialInvoiceDetail.getCommercialInvoiceDetailLog();
                    Optional<CommercialInvoiceDetailLog> oCommercialInvoiceDetailLog = commercialInvoiceDetailLogs.stream().max(Comparator.comparing(CommercialInvoiceDetailLog::getVersion));
                    if (oCommercialInvoiceDetailLog.isPresent()) {
                        CommercialInvoiceDetailLog commercialInvoiceDetailLog = oCommercialInvoiceDetailLog.get();
                        if (!commercialInvoiceDetailLog.getUnitPriceAfterAllocate().equals(commercialInvoiceDetail.getUnitPriceAllocated())) {
                            if (Objects.equals(commercialInvoiceDetail.getStatus(), GlobalConstant.STATUS_CI_DETAIL_REJECT)) {
                                commercialInvoiceDetail.setStatusY4a(GlobalConstant.STATUS_CI_DETAIL_ADJUSTED);
                            }
                            commercialInvoiceDetail.setStatus(GlobalConstant.STATUS_CI_NO_REJECT);
                            //write log (update and send CI)
                            CommercialInvoiceDetailLog commercialInvoiceDetailLogNew = new CommercialInvoiceDetailLog();
                            commercialInvoiceDetailLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
                            commercialInvoiceDetailLogNew.setUpdatedDate(new Date().toInstant());
                            commercialInvoiceDetailLogNew.setVersion(commercialInvoiceDetailLog.getVersion() + 1);
                            commercialInvoiceDetailLogNew.setUnitPriceAfter(item.getUnitPrice());
                            commercialInvoiceDetailLogNew.setUnitPriceBefore(commercialInvoiceDetailLog.getUnitPriceAfter());
                            commercialInvoiceDetailLogNew.setAmountAfter(commercialInvoiceDetail.getAmount());
                            commercialInvoiceDetailLogNew.setAmountBefore(commercialInvoiceDetailLog.getAmountAfter());
                            commercialInvoiceDetailLogNew.setUnitPriceBeforeAllocate(commercialInvoiceDetailLog.getUnitPriceAfterAllocate());
                            commercialInvoiceDetailLogNew.setUnitPriceAfterAllocate(commercialInvoiceDetail.getUnitPriceAllocated());
                            commercialInvoiceDetailLogs.add(commercialInvoiceDetailLogNew);

                        }
                    } else {
                        throw new BusinessException("Can not find log detail.");
                    }
                    commercialInvoiceDetail.setCommercialInvoiceDetailLog(commercialInvoiceDetailLogs);
                }
                if (Objects.equals(commercialInvoiceDetail.getStatus(), GlobalConstant.STATUS_CI_DETAIL_REJECT) && commercialInvoice.getSupplierUpdatedLatest()) {
                    commercialInvoiceDetail.setStatusY4a(GlobalConstant.STATUS_CI_DETAIL_NO_ADJUSTED);
                }
                return commercialInvoiceDetail;
            }).collect(Collectors.toSet());
            commercialInvoice.setCommercialInvoiceDetail(detailSet);
            commercialInvoice.setUpdatedDate(new Date().toInstant());
            commercialInvoice.setRemark(request.getRemark());
            commercialInvoice.setTruckingCost(request.getTruckingCost());
            if (Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_SENT_BUYER)) {
                commercialInvoice.setStatus(GlobalConstant.STATUS_CI_REJECT);
            }
            commercialInvoice.setSupplierUpdatedLatest(request.getIsSupplier());
            commercialInvoice.setAmount(detailSet.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            if (commercialInvoice.getTheFirstReject() != null && commercialInvoice.getTheFirstReject() && !request.getIsSupplier()) {
                commercialInvoice.setTheFirstReject(false);
            } else {
                commercialInvoice.setTheFirstReject(request.getTheFirstReject());
            }

            //write log total amount
            if (Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                //write log when change total amount
                Set<CommercialInvoiceTotalAmountLog> commercialInvoiceTotalAmountLogs = commercialInvoice.getCommercialInvoiceTotalAmountLog();
                double amountBefore = 0;
                Optional<CommercialInvoiceTotalAmountLog> oCommercialInvoiceTotalAmountLog = commercialInvoiceTotalAmountLogs.stream().max(Comparator.comparing(CommercialInvoiceTotalAmountLog::getVersion));
                if (oCommercialInvoiceTotalAmountLog.isPresent()) {
                    CommercialInvoiceTotalAmountLog commercialInvoiceTotalAmountLog = oCommercialInvoiceTotalAmountLog.get();
                    amountBefore = commercialInvoiceTotalAmountLog.getAmountTotalAfter();
                    //sum total amount before
                    double amountAfter = commercialInvoice.getCommercialInvoiceDetail().stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum);
                    if (amountAfter != amountBefore) {
                        CommercialInvoiceTotalAmountLog commercialInvoiceTotalAmountLogNew = new CommercialInvoiceTotalAmountLog();
                        commercialInvoiceTotalAmountLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
                        commercialInvoiceTotalAmountLogNew.setUpdatedDate(new Date().toInstant());
                        commercialInvoiceTotalAmountLogNew.setVersion(commercialInvoiceTotalAmountLog.getVersion() + 1);
                        commercialInvoiceTotalAmountLogNew.setAmountTotalAfter(commercialInvoice.getAmount());
                        commercialInvoiceTotalAmountLogNew.setAmountTotalBefore(amountBefore);
                        commercialInvoiceTotalAmountLogNew.setTruckingCostLog(commercialInvoice.getTruckingCost());
                        commercialInvoiceTotalAmountLogs.add(commercialInvoiceTotalAmountLogNew);
                    }
                } else {
                    throw new BusinessException("Can not find log total.");
                }
                commercialInvoice.setCommercialInvoiceTotalAmountLog(commercialInvoiceTotalAmountLogs);
            }
            commercialInvoiceRepository.save(commercialInvoice);
            List<PurchaseOrders> purchaseOrdersList = commercialInvoice.getBookingPackingList().getBookingProformaInvoice().stream().map(element -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice =oProformaInvoice.get();
                    Optional<User> userSupplier = userRepository.findOneByVendor(commercialInvoice.getSupplier());
                    if (!userSupplier.isPresent()) {
                        throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", commercialInvoice.getSupplier()));
                    }
                    User userVendor = userSupplier.get();
                    String subject = getSubjectMail(proformaInvoice.getPurchaseOrders().getPoNumber(), proformaInvoice.getPurchaseOrders().getCountry(), userVendor);
                    if (!request.getIsSupplier() && (Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_SENT_BUYER) || Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT))) {
                        List<String> listEmail = new ArrayList<>();
                        listEmail.add(userVendor.getEmail());
                        List<String> listMailSC = getListUserSC(userVendor);
                        List<String> listMailPU = getListUserPU(userVendor);
                        List<String> listMailCC = new ArrayList<>();
                        listMailCC.addAll(listMailSC);
                        listMailCC.addAll(listMailPU);
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PKL + commercialInvoice.getBookingPackingList().getId() + "?size=20&page=0", commercialInvoice.getInvoiceNo(), null, null, null, GlobalConstant.REJECT_CI);
                        sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                    }
                    if (request.getIsSupplier() && Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                        List<String> listEmail = new ArrayList<>();
                        List<String> listMailSC = getListUserSC(userVendor);
                        List<String> listMailPU = getListUserPU(userVendor);
                        List<String> listMailCC = new ArrayList<>();
                        String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                        if (userPUPrimaryStr != null) {
                            Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                            if (oUserPUPrimary.isEmpty()) {
                                throw new BusinessException("Can not find user PU.");
                            }
                            User userPUPrimary = oUserPUPrimary.get();
                            listEmail.add(userPUPrimary.getEmail());
                            listMailCC.addAll(listMailSC);
                            listMailCC.addAll(listMailPU);
                        }
                        listMailCC.add(userVendor.getEmail());
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PKL + commercialInvoice.getBookingPackingList().getId() + "?size=20&page=0", commercialInvoice.getInvoiceNo(), null, null, null, GlobalConstant.SEND_CI);
                        sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);
                    }
                    return oProformaInvoice.get().getPurchaseOrders();
                }
                return null;
            }).collect(Collectors.toList());
            if (purchaseOrdersList.isEmpty()) {
                throw new BusinessException("Can not find Purchase Order");
            }
            return mappingEntityToDto(commercialInvoice, CommercialInvoiceDTO.class);
        } catch (
            Exception ex) {
            log.error(ex.getMessage());

            throw new BusinessException(ex.getMessage());
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
    public boolean confirmed(ListingIdSupplierDTO request) {
        try {
            Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(request.getId());
            if (oCommercialInvoice.isPresent()) {
                CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                if (request.getIsSupplier()) {
                    throw new BusinessException("User supplier not permission confirm CI.");
                }
                if (commercialInvoice.getStatus() != GlobalConstant.STATUS_CI_REJECT && commercialInvoice.getStatus() != GlobalConstant.STATUS_CI_SENT_BUYER) {
                    throw new BusinessException("The status not valid.");
                }
                BookingPackingList bookingPackingList = commercialInvoice.getBookingPackingList();
                if (!CommonDataUtil.isNotNull(bookingPackingList)) {
                    throw new BusinessException("Can not find Packing List");
                }
                Set<BookingProformaInvoice> bookingProformaInvoices = bookingPackingList.getBookingProformaInvoice();

                Booking booking = bookingProformaInvoices.stream().map(BookingProformaInvoice::getBooking).findFirst().orElse(null);
                if (booking == null) {
                    throw new BusinessException("Can not find Booking");
                }
                if (booking.getStatus() == GlobalConstant.STATUS_BOOKING_CANCEL) {
                    throw new BusinessException("Booking have been canceled, can not confirm Packing List.");
                }
                bookingPackingList.setStatus(GlobalConstant.STATUS_PKL_CONFIRMED);
                commercialInvoice.setStatus(GlobalConstant.STATUS_CI_CONFIRMED);
                bookingPackingList.setCommercialInvoice(commercialInvoice);
                final boolean[] isAllPKLCreated = {true};
                List<PurchaseOrders> purchaseOrdersList = new ArrayList<>();
                //if all packing list created status purchase order transfer to LOADED
                booking.getBookingProformaInvoice().parallelStream().forEach(element -> {
                    BookingPackingList bookingPackingListElement = element.getBookingPackingList();
                    if (bookingPackingListElement.getStatus() == 0) {
                        isAllPKLCreated[0] = false;
                    }
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                    if (oProformaInvoice.isPresent()) {
                        ProformaInvoice proformaInvoice = oProformaInvoice.get();
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_LOADED);
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_LOADED);
                        purchaseOrdersList.add(purchaseOrders);
                    }
                });
                //if all PKL created then update status booking to loaded
                if (isAllPKLCreated[0]) {
                    booking.setStatus(GlobalConstant.STATUS_BOOKING_LOADED);
                    bookingRepository.save(booking);
                    purchaseOrdersRepository.saveAll(purchaseOrdersList);
                }
                List<String> listEmail = new ArrayList<>();
                Optional<User> userEmailsSupplier = userRepository.findOneByVendor(commercialInvoice.getSupplier());
                if (userEmailsSupplier.isEmpty()) {
                    throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", commercialInvoice.getSupplier()));
                }
                User userVendor = userEmailsSupplier.get();
                Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersList.stream().max(Comparator.comparing(PurchaseOrders::getId));
                if (oPurchaseOrders.isEmpty()) {
                    throw new BusinessException("Can not find Purchase Order");
                }
                PurchaseOrders purchaseOrders = oPurchaseOrders.get();
                listEmail.add(userVendor.getEmail());
                bookingPackingListRepository.save(bookingPackingList);
                List<String> listMailSC = getListUserSC(userVendor);
                List<String> listMailPU = getListUserPU(userVendor);
                List<String> listMailCC = new ArrayList<>();
                listEmail.addAll(listMailSC);
                listEmail.addAll(listMailPU);
                listMailCC.add(userVendor.getEmail());
                String strPONumber = purchaseOrdersList.stream().map(PurchaseOrders::getPoNumber).collect(Collectors.joining(","));
                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PKL + commercialInvoice.getBookingPackingList().getId() + "?size=20&page=0", strPONumber, "", null, null, GlobalConstant.CONFIRM_CI);
                String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), userVendor);
                sendMailService.sendMail(subject, content, listEmail, listMailCC, null, null);

            }

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean send(ListingIdSupplierDTO request) {
        try {
            Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(request.getId());
            if (oCommercialInvoice.isPresent()) {
                CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                if (!Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_NEW) && !Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                    throw new BusinessException("The status not valid.");
                } else {
                    Set<CommercialInvoiceDetail> detailSet = commercialInvoice.getCommercialInvoiceDetail();
                    detailSet = detailSet.stream().map(item -> {
                        if (Objects.equals(item.getStatus(), GlobalConstant.STATUS_CI_DETAIL_REJECT) && Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                            Set<CommercialInvoiceDetailLog> commercialInvoiceDetailLogs = item.getCommercialInvoiceDetailLog();
                            Optional<CommercialInvoiceDetailLog> oCommercialInvoiceDetailLog = commercialInvoiceDetailLogs.stream().max(Comparator.comparing(CommercialInvoiceDetailLog::getVersion));
                            if (oCommercialInvoiceDetailLog.isPresent()) {
                                CommercialInvoiceDetailLog commercialInvoiceDetailLog = oCommercialInvoiceDetailLog.get();
                                if (!commercialInvoiceDetailLog.getUnitPriceAfter().equals(item.getUnitPriceAllocated())) {
                                    item.setStatusY4a(GlobalConstant.STATUS_CI_DETAIL_ADJUSTED);
                                    item.setStatus(GlobalConstant.STATUS_CI_NO_REJECT);
                                    //write log
                                    CommercialInvoiceDetailLog commercialInvoiceDetailLogNew = new CommercialInvoiceDetailLog();
                                    commercialInvoiceDetailLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
                                    commercialInvoiceDetailLogNew.setUpdatedDate(new Date().toInstant());
                                    commercialInvoiceDetailLogNew.setVersion(commercialInvoiceDetailLog.getVersion() + 1);
                                    commercialInvoiceDetailLogNew.setUnitPriceAfter(item.getUnitPrice());
                                    commercialInvoiceDetailLogNew.setUnitPriceBefore(commercialInvoiceDetailLog.getUnitPriceAfter());
                                    commercialInvoiceDetailLogNew.setAmountAfter(item.getAmount());
                                    commercialInvoiceDetailLogNew.setAmountBefore(commercialInvoiceDetailLog.getAmountAfter());
                                    commercialInvoiceDetailLogs.add(commercialInvoiceDetailLogNew);
                                }
                            } else {
                                throw new BusinessException("Can not find log detail.");
                            }
                            item.setCommercialInvoiceDetailLog(commercialInvoiceDetailLogs);
                        }
                        return item;
                    }).collect(Collectors.toSet());
                    commercialInvoice.setCommercialInvoiceDetail(detailSet);
                    if (Objects.equals(commercialInvoice.getStatus(), GlobalConstant.STATUS_CI_REJECT)) {
                        //write log when change total amount
                        Set<CommercialInvoiceTotalAmountLog> commercialInvoiceTotalAmountLogs = commercialInvoice.getCommercialInvoiceTotalAmountLog();
                        double amountBefore = 0;
                        Optional<CommercialInvoiceTotalAmountLog> oCommercialInvoiceTotalAmountLog = commercialInvoiceTotalAmountLogs.stream().max(Comparator.comparing(CommercialInvoiceTotalAmountLog::getVersion));
                        if (oCommercialInvoiceTotalAmountLog.isPresent()) {
                            CommercialInvoiceTotalAmountLog commercialInvoiceTotalAmountLog = oCommercialInvoiceTotalAmountLog.get();
                            amountBefore = commercialInvoiceTotalAmountLog.getAmountTotalAfter();
                            //sum total amount before
                            double amountAfter = commercialInvoice.getCommercialInvoiceDetail().stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum);
                            if (amountAfter != amountBefore) {
                                CommercialInvoiceTotalAmountLog commercialInvoiceTotalAmountLogNew = new CommercialInvoiceTotalAmountLog();
                                commercialInvoiceTotalAmountLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
                                commercialInvoiceTotalAmountLogNew.setUpdatedDate(new Date().toInstant());
                                commercialInvoiceTotalAmountLogNew.setVersion(commercialInvoiceTotalAmountLog.getVersion() + 1);
                                commercialInvoiceTotalAmountLogNew.setAmountTotalAfter(commercialInvoice.getAmount());
                                commercialInvoiceTotalAmountLogNew.setAmountTotalBefore(amountBefore);
                                commercialInvoiceTotalAmountLogs.add(commercialInvoiceTotalAmountLogNew);
                            }
                        } else {
                            throw new BusinessException("Can not find log total.");
                        }
                        commercialInvoice.setCommercialInvoiceTotalAmountLog(commercialInvoiceTotalAmountLogs);
                    }
                    commercialInvoice.setStatus(GlobalConstant.STATUS_CI_SENT_BUYER);

                    List<User> listEmailYes4all = userRepository.findAllByIsYes4all(true);
                    if (listEmailYes4all.isEmpty()) {
                        throw new BusinessException("Can not find any email of Y4A.");
                    }
                    List<String> listEmail = new ArrayList<>(listEmailYes4all.stream().map(User::getEmail).collect(Collectors.toList()));
                    List<PurchaseOrders> purchaseOrdersList = commercialInvoice.getBookingPackingList().getBookingProformaInvoice().stream().map(element -> {
                        Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                        return oProformaInvoice.map(ProformaInvoice::getPurchaseOrders).orElse(null);
                    }).collect(Collectors.toList());
                    if (purchaseOrdersList.isEmpty()) {
                        throw new BusinessException("Can not find Purchase Order");
                    }
                    Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersList.stream().max(Comparator.comparing(PurchaseOrders::getId));
                    if (oPurchaseOrders.isEmpty()) {
                        throw new BusinessException("The PurchaseOrders not found.");
                    }
                    PurchaseOrders purchaseOrders = oPurchaseOrders.get();
                    Optional<User> oUser = userRepository.findOneByVendor(commercialInvoice.getSupplier());
                    if (oUser.isEmpty()) {
                        throw new BusinessException("The user not found.");
                    }
                    commercialInvoiceRepository.save(commercialInvoice);
                    String subject = getSubjectMail(purchaseOrders.getPoNumber(), purchaseOrders.getCountry(), oUser.get());
                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PKL + commercialInvoice.getBookingPackingList().getId() + "?size=20&page=0", commercialInvoice.getInvoiceNo(), null, null, null, GlobalConstant.REJECT_CI);
                    sendMailService.sendMail(subject, content, listEmail, null, null, null);
                }
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }


    public FileDTO exportExcel(Integer bookingPackingListId) {
        try {
            FileDTO fileDTO = new FileDTO();
            byte[] content = null;

            // get data CI
            Optional<CommercialInvoice> optCommercialInvoice = commercialInvoiceRepository.findByBookingPackingListId(bookingPackingListId);
            if (optCommercialInvoice.isPresent()) {
                // file name
                fileDTO.setFileName(optCommercialInvoice.get().getInvoiceNo() + ".xlsx");

                // header excel
                List<String> listHeader = getHeaderExcel();

                // detail excel
                Set<CommercialInvoiceDetail> listCommercialInvoiceDetail = commercialInvoiceDetailRepository.findByCommercialInvoice(
                    optCommercialInvoice.get());
                Map<Long, List<Object>> mapData = getDetailExcel(listCommercialInvoiceDetail);
                BookingPackingList bookingPackingList = optCommercialInvoice.get().getBookingPackingList();
                Optional<Vendor> oVendor = vendorRepository.findByVendorCode(bookingPackingList.getSupplier());
                Vendor vendor;
                if (oVendor.isPresent()) {
                    vendor = oVendor.get();
                } else {
                    throw new BusinessException("Can not find vendor");
                }
                content = ExcelUtil.generateExcelFile(null, 0, listHeader, mapData, bookingPackingList,vendor);
            } else {
                throw new BusinessException(String.format("id CommercialInvoice %s not exists", bookingPackingListId));
            }

            if (CommonDataUtil.isNull(content)) {
                throw new BusinessException("Could not created excel file CommercialInvoice.");
            }
            // file data
            fileDTO.setContent(content);
            return fileDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }
    public FileDTO exportExcelByIdCI(Integer id) {
        try {
            FileDTO fileDTO = new FileDTO();
            byte[] content = null;

            // get data CI
            Optional<CommercialInvoice> optCommercialInvoice = commercialInvoiceRepository.findById(id);
            if (optCommercialInvoice.isPresent()) {
                // file name
                fileDTO.setFileName(optCommercialInvoice.get().getInvoiceNo() + ".xlsx");

                // header excel
                List<String> listHeader = getHeaderExcel();

                // detail excel
                Set<CommercialInvoiceDetail> listCommercialInvoiceDetail = commercialInvoiceDetailRepository.findByCommercialInvoice(
                    optCommercialInvoice.get());
                Map<Long, List<Object>> mapData = getDetailExcel(listCommercialInvoiceDetail);
                BookingPackingList bookingPackingList = optCommercialInvoice.get().getBookingPackingList();
                Optional<Vendor> oVendor = vendorRepository.findByVendorCode(bookingPackingList.getSupplier());
                Vendor vendor;
                if (oVendor.isPresent()) {
                    vendor = oVendor.get();
                } else {
                    throw new BusinessException("Can not find vendor");
                }
                content = ExcelUtil.generateExcelFile(null, 0, listHeader, mapData, bookingPackingList,vendor);
            } else {
                throw new BusinessException(String.format("id CommercialInvoice %s not exists", id));
            }

            if (CommonDataUtil.isNull(content)) {
                throw new BusinessException("Could not created excel file CommercialInvoice.");
            }
            // file data
            fileDTO.setContent(content);
            return fileDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }
    public FileDTO exportExcelById(Integer id) {
        try {
            FileDTO fileDTO = new FileDTO();
            byte[] content = null;

            // Get data CI
            Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(id);
            if (oCommercialInvoice.isPresent()) {
                // File name
                fileDTO.setFileName(oCommercialInvoice.get().getInvoiceNo().replace(" ", "") + ".xlsx");

                // Get data CI detail
                CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                Set<CommercialInvoiceDetail> commercialInvoiceDetail = commercialInvoiceDetailRepository.findByCommercialInvoice(commercialInvoice);
                commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetail);

                // Create workbook
                XSSFWorkbook workbook = new XSSFWorkbook();
                // Create general sheet
                generateGeneral(workbook, commercialInvoice);
                // Create detail sheet
                generateDetail(workbook, commercialInvoice);

                // Convert workbook to byte array
                content = ExcelUtil.convertWorkbookToByte(workbook);
            } else {
                throw new BusinessException(String.format("id CommercialInvoice %s not exists", id));
            }

            if (CommonDataUtil.isNull(content)) {
                throw new BusinessException("Could not created excel file CommercialInvoice");
            }
            // File data
            fileDTO.setContent(content);
            return fileDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException("Could not created excel file CommercialInvoice");
        }
    }

    private XSSFWorkbook generateGeneral(XSSFWorkbook workbook, CommercialInvoice commercialInvoice) {
        // Data
        Long index = 1L;
        Map<Long, List<Object>> mapData = new HashMap<>();
        mapData.put(index++, Arrays.asList("General", ""));
        mapData.put(index++, Arrays.asList("Seller", CommonDataUtil.toEmpty(commercialInvoice.getSeller())));
        mapData.put(index++, Arrays.asList("Buyer", CommonDataUtil.toEmpty(commercialInvoice.getBuyer())));
        BigDecimal amount = BigDecimal.valueOf((Double) CommonDataUtil.toZero(commercialInvoice.getAmount()));
        mapData.put(index++, Arrays.asList("Amount", CommonDataUtil.moneyFormatter(amount)));
        mapData.put(index++, Arrays.asList("Remark", CommonDataUtil.toEmpty(commercialInvoice.getRemark())));
        mapData.put(index++, Arrays.asList("Bank Information", ""));
        mapData.put(index++, Arrays.asList("Company name", CommonDataUtil.toEmpty(commercialInvoice.getCompanyName())));
        mapData.put(index++, Arrays.asList("A/C number", CommonDataUtil.toEmpty(commercialInvoice.getAcNumber())));
        mapData.put(index++, Arrays.asList("Beneficiary's Bank", CommonDataUtil.toEmpty(commercialInvoice.getBeneficiaryBank())));
        mapData.put(index, Arrays.asList("Swift Code", CommonDataUtil.toEmpty(commercialInvoice.getSwiftCode())));

        // Sheet name
        String sheetName = "General";
        // Generate
        workbook = ExcelUtil.generateExcelVerticalFileWorkbook(workbook, sheetName, 0, mapData);
        // Set format again
        if (CommonDataUtil.isNotNull(workbook)) {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            ExcelUtil.mergeCell(sheet, 0, 0, 0, 1);
            ExcelUtil.mergeCell(sheet, 5, 5, 0, 1);
            sheet.getRow(0).getCell(0).setCellStyle(ExcelUtil.createStyleDetail(workbook));
            sheet.getRow(5).getCell(0).setCellStyle(ExcelUtil.createStyleDetail(workbook));
        }
        return workbook;
    }

    private XSSFWorkbook generateDetail(XSSFWorkbook workbook, CommercialInvoice commercialInvoice) {
        // Header excel
        List<String> listHeader = getHeaderExcel();

        // Detail excel
        Map<Long, List<Object>> mapData = getDetailExcel(commercialInvoice.getCommercialInvoiceDetail());

        // Return data
        return ExcelUtil.generateExcelFileWorkbook(workbook, "Detail", 0, listHeader, mapData);
    }

    private List<String> getHeaderExcel() {
        List<String> listHeader = new ArrayList<>();
        listHeader.add("AMAZON PO");
        listHeader.add("SKU");
        listHeader.add("ASIN");
        listHeader.add("DESCRIPTION");
        listHeader.add("QUANTITY");
        listHeader.add("UNIT PRICE (USD)");
        listHeader.add("AMOUNT");

        return listHeader;
    }

    private Map<Long, List<Object>> getDetailExcel(Set<CommercialInvoiceDetail> listCommercialInvoiceDetail) {
        // Detail excel
        Long row = 10L;
        Map<Long, List<Object>> mapData = new HashMap<>();

        for (CommercialInvoiceDetail commercialInvoiceDetail : listCommercialInvoiceDetail) {
            List<Object> listData = new ArrayList<>();
            listData.add(CommonDataUtil.toEmpty(commercialInvoiceDetail.getFromSo()));
            listData.add(CommonDataUtil.toEmpty(commercialInvoiceDetail.getSku()));
            listData.add(CommonDataUtil.toEmpty(commercialInvoiceDetail.getaSin()));
            listData.add(CommonDataUtil.toEmpty(commercialInvoiceDetail.getProductTitle()));
            listData.add(CommonDataUtil.toZero(commercialInvoiceDetail.getQty()));
            listData.add(CommonDataUtil.toZero(commercialInvoiceDetail.getUnitPriceAllocated()));
            listData.add(CommonDataUtil.toZero(commercialInvoiceDetail.getAmount()));
            mapData.put(row++, listData);
        }

        return mapData;
    }

    private <T> T mappingEntityToDto(CommercialInvoice commercialInvoice, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(commercialInvoice, dto);
            CommercialInvoice cIModel = null;
            if (commercialInvoice.getCommercialInvoiceDetail().isEmpty()) {
                Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(commercialInvoice.getId());
                if (oCommercialInvoice.isPresent()) {
                    cIModel = oCommercialInvoice.get();
                }
            } else {
                cIModel = commercialInvoice;
            }
            if (cIModel == null) {
                cIModel = commercialInvoice;
            }
            List<CommercialInvoiceDetail> listDetailPI = new ArrayList<>(cIModel.getCommercialInvoiceDetail());
            String fromSoStr = listDetailPI.stream().map(CommercialInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(", "));
            Integer bookingPackingListId = null;
            String bookingPackingListNo = null;
            Optional<User> oUser = userRepository.findOneByLogin(commercialInvoice.getUpdatedBy());
            String updatedBy = "";
            if (oUser.isPresent()) {
                updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
            }
            if (commercialInvoice.getBookingPackingList() != null) {

                bookingPackingListId = commercialInvoice.getBookingPackingList().getId();
                bookingPackingListNo = commercialInvoice.getBookingPackingList().getInvoice();

            }
            if (dto instanceof CommercialInvoiceDTO) {
                clazz.getMethod("setCommercialInvoiceDetail", List.class).invoke(dto, listDetailPI);
            } else if (dto instanceof CommercialInvoiceMainDTO) {
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setBookingPackingListId", Integer.class).invoke(dto, bookingPackingListId);
                clazz.getMethod("setBookingPackingListNo", String.class).invoke(dto, bookingPackingListNo);
                clazz.getMethod("setUpdatedBy", String.class).invoke(dto, updatedBy);

            }
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

}
