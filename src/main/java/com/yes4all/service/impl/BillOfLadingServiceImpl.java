package com.yes4all.service.impl;

import com.yes4all.common.constants.Constant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.FileUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.BillOfLadingService;
import com.yes4all.service.BookingService;
import com.yes4all.service.CommercialInvoiceService;
import com.yes4all.service.SendMailService;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.common.utils.CommonDataUtil.getSubjectMailBOL;
import static com.yes4all.constants.GlobalConstant.*;
import static com.yes4all.service.impl.ResourceServiceImpl.getFileResourcePath;


/**
 * Service Implementation for managing {@link CommercialInvoice}.
 */
@Service
@Transactional
public class BillOfLadingServiceImpl implements BillOfLadingService {

    private final Logger log = LoggerFactory.getLogger(BillOfLadingServiceImpl.class);

    @Autowired
    private BookingPurchaseOrderRepository bookingPurchaseOrderRepository;
    @Autowired
    private BillOfLadingRepository billOfLadingRepository;

    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    private static final String LINK_DETAIL_BOL = "/bill-of-lading/detail/";

    @Value("${attribute.link.url}")
    private String linkPOMS;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    CommercialInvoiceService commercialInvoiceService;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private BookingProformaInvoiceRepository bookingProformaInvoiceRepository;

    @Override
    public Page<BillOfLadingMainDTO> listingBillOfLadingWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "updated_at");
        Page<BillOfLading> data = billOfLadingRepository.findByCondition(filterParams.get("bolNoBOL"), filterParams.get("bookingNoBOL"), filterParams.get("userId"), pageable);
        return data.map(item -> mappingEntityToDto(item, BillOfLadingMainDTO.class));
    }

    @Override
    public BillOfLadingDTO getBillOfLadingDetail(DetailObjectDTO request) {
        try {
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findById(request.getId());
            if (oBillOfLading.isPresent()) {
                BillOfLading billOfLading = oBillOfLading.get();
                Optional<User> oUser = userRepository.findById(request.getUserId());
                Boolean isPU = false;
                if (oUser.isPresent()) {
                    User user = oUser.get();
                    Set<Authority> authorities = user.getAuthorities();
                    isPU = authorities.stream().anyMatch(i -> i.getName().equals(POMS_USER));
                    if (billOfLading.getBrokerId() != null && (authorities.stream().anyMatch(i -> i.getName().equals(POMS_BROKER))) && !billOfLading.getBrokerId().equals(user.getLogin())) {
                        throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                    }
                } else {
                    throw new BusinessException("User not exists");
                }
                Pageable pageableResource = PageRequestUtil.genPageRequest(request.getPage(), request.getSize(), Sort.Direction.DESC, "uploadDate");
                Page<Resource> pageBOLAttachments = resourceRepository.findByFileTypeAndBillOfLadingIdAndModule(GlobalConstant.FILE_UPLOAD, billOfLading.getId(), Constant.MODULE_BILL_OF_LADING, pageableResource);
                Page<Resource> pageInvoice = resourceRepository.findByFileTypeAndBillOfLadingIdAndModule(GlobalConstant.FILE_UPLOAD, billOfLading.getId(), Constant.MODULE_BILL_OF_LADING_INVOICE, pageableResource);
                Page<Resource> pageDuty = resourceRepository.findByFileTypeAndBillOfLadingIdAndModule(GlobalConstant.FILE_UPLOAD, billOfLading.getId(), Constant.MODULE_BILL_OF_LADING_DUTY_FEE, pageableResource);
                Page<Resource> pageOther = resourceRepository.findByFileTypeAndBillOfLadingIdAndModule(GlobalConstant.FILE_UPLOAD, billOfLading.getId(), Constant.MODULE_BILL_OF_LADING_OTHER, pageableResource);

                Page<ResourceDTO> pageInvoiceDTO = pageInvoice.map(this::convertToObjectResourceDTO);
                Page<ResourceDTO> pageDutyDTO = pageDuty.map(this::convertToObjectResourceDTO);
                Page<ResourceDTO> pageOtherDTO = pageOther.map(this::convertToObjectResourceDTO);
                Page<ResourceDTO> pageBOLAttachmentDTOs = pageBOLAttachments.map(this::convertToObjectResourceDTO);

                BillOfLadingDTO data = CommonDataUtil.getModelMapper().map(billOfLading, BillOfLadingDTO.class);
                Set<BookingMainDTO> detailBookings = data.getBooking().stream().map(item -> {
                    String fromSoStr = bookingPurchaseOrderRepository.findAllFromSOByBookingId(item.getId());
                    item.setPOAmazon(fromSoStr);
                    return item;
                }).collect(Collectors.toSet());
                data.setBooking(detailBookings);
                data.setBolAttachments(pageBOLAttachmentDTOs);
                data.setInvoice(pageInvoiceDTO);
                data.setDuty(pageDutyDTO);
                data.setOther(pageOtherDTO);
                if (isPU && (billOfLading.getStatus().equals(STATUS_BOL_NEW) || billOfLading.getStatus().equals(STATUS_BOL_UPDATE))) {
                    data.setInvoice(null);
                    data.setDuty(null);
                    data.setOther(null);
                }
                return data;
            }
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public Boolean submitBOL(DetailObjectDTO request) {
        try {
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findById(request.getId());
            if (oBillOfLading.isPresent()) {
                BillOfLading billOfLading = oBillOfLading.get();
                Optional<User> oUserBroker = userRepository.findById(billOfLading.getBrokerId());
                String userBroker;
                if (oUserBroker.isEmpty()) {
                    throw new BusinessException("Can not find user broker");
                } else {
                    userBroker = (oUserBroker.get().getLastName() == null ? "" : oUserBroker.get().getLastName() + " ") + oUserBroker.get().getFirstName();
                }
                billOfLading.getBooking().forEach(item -> {
                    Optional<Booking> oBooking = bookingRepository.findById(item.getId());
                    if (oBooking.isPresent()) {
                        Booking booking = oBooking.get();
                        Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                        for (BookingProformaInvoice element : bookingProformaInvoices) {
                            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                            if (oProformaInvoice.isPresent()) {
                                List<String> listEmail = new ArrayList<>();
                                List<String> listEmailCC = new ArrayList<>();
                                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                                Optional<User> oUserVendor = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                                if (oUserVendor.isEmpty()) {
                                    throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                                }
                                User userVendor = oUserVendor.get();
                                List<String> listMailPU = getListUserPU(userVendor);
                                String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                                if (userPUPrimaryStr != null) {
                                    Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                                    if (oUserPUPrimary.isEmpty()) {
                                        throw new BusinessException("Can not find user PU.");
                                    }
                                    User userPUPrimary = oUserPUPrimary.get();
                                    listEmail.add(userPUPrimary.getEmail());
                                    listEmailCC.addAll(listMailPU);
                                    listEmailCC.add(userVendor.getEmail());
                                }
                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_BOL + billOfLading.getId() + "?size=20&page=0", billOfLading.getBillOfLadingNo(), userBroker, "", "submitBOL", SUBMIT_BOL);
                                String subject = getSubjectMailBOL(billOfLading.getBillOfLadingNo());
                                sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                            }
                        }
                    } else {
                        throw new BusinessException(String.format("Booking %s not exist.", item.getBookingConfirmation()));
                    }
                });
                billOfLading.setStatus(STATUS_BOL_SUBMIT);
                billOfLading.setReason(null);
                billOfLadingRepository.save(billOfLading);
                return true;
            } else {
                throw new BusinessException("Bill Of Lading not exist.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public Boolean requestBOL(DetailObjectDTO request) {
        try {
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findById(request.getId());
            if (oBillOfLading.isPresent()) {
                BillOfLading billOfLading = oBillOfLading.get();
                Optional<User> oUser = userRepository.findById(request.getUserId());
                if (oUser.isPresent()) {
                    User user = oUser.get();
                    Set<Authority> authorities = user.getAuthorities();
                    if (billOfLading.getBrokerId() != null && (user.getVendor() != null && (authorities.stream().noneMatch(i -> i.getName().equals("POMS-BROKER"))))) {
                        throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                    }
                } else {
                    throw new BusinessException("User not exists");
                }
                Optional<User> oUserBroker = userRepository.findById(billOfLading.getBrokerId());
                String emailBroker;
                if (oUserBroker.isEmpty()) {
                    throw new BusinessException("Can not find user broker");
                } else {
                    emailBroker = oUserBroker.get().getEmail();
                }
                billOfLading.getBooking().forEach(item -> {
                    Optional<Booking> oBooking = bookingRepository.findById(item.getId());
                    if (oBooking.isPresent()) {
                        Booking booking = oBooking.get();
                        Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                        for (BookingProformaInvoice element : bookingProformaInvoices) {
                            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                            if (oProformaInvoice.isPresent()) {
                                List<String> listEmail = new ArrayList<>();
                                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                                Optional<User> oUserVendor = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                                if (oUserVendor.isEmpty()) {
                                    throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                                }
                                User userVendor = oUserVendor.get();
                                List<String> listMailPU = getListUserPU(userVendor);
                                listEmail.add(emailBroker);
                                List<String> listEmailCC = new ArrayList<>(listMailPU);
                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_BOL + billOfLading.getId() + "?size=20&page=0", billOfLading.getBillOfLadingNo(), "", "", "submitBOL", REQUEST_BOL);
                                String subject = getSubjectMailBOL(billOfLading.getBillOfLadingNo());
                                sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                            }
                        }
                    } else {
                        throw new BusinessException(String.format("Booking %s not exist.", item.getBookingConfirmation()));
                    }
                });
                billOfLading.setReason(request.getReason());
                billOfLading.setStatus(STATUS_BOL_REQUEST);
                billOfLadingRepository.save(billOfLading);
                return true;
            } else {
                throw new BusinessException("Bill Of Lading not exist.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public Boolean confirmBOL(DetailObjectDTO request) {
        try {
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findById(request.getId());
            if (oBillOfLading.isPresent()) {
                BillOfLading billOfLading = oBillOfLading.get();
                Optional<User> oUserBroker = userRepository.findById(billOfLading.getBrokerId());
                String emailBroker;
                if (oUserBroker.isEmpty()) {
                    throw new BusinessException("Can not find user broker");
                } else {
                    emailBroker = oUserBroker.get().getEmail();
                }
                billOfLading.getBooking().forEach(item -> {
                    Optional<Booking> oBooking = bookingRepository.findById(item.getId());
                    if (oBooking.isPresent()) {
                        Booking booking = oBooking.get();
                        bookingService.completedBooking(booking.getId());
                        Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                        for (BookingProformaInvoice element : bookingProformaInvoices) {
                            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                            if (oProformaInvoice.isPresent()) {
                                List<String> listEmail = new ArrayList<>();
                                List<String> listEmailCC = new ArrayList<>();
                                ProformaInvoice proformaInvoice = oProformaInvoice.get();
                                Optional<User> oUserVendor = userRepository.findOneByVendor(proformaInvoice.getSupplier());
                                if (oUserVendor.isEmpty()) {
                                    throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", proformaInvoice.getVendorCode()));
                                }
                                User userVendor = oUserVendor.get();
                                List<String> listMailPU = getListUserPU(userVendor);
                                listEmail.add(emailBroker);
                                listEmailCC.addAll(listMailPU);
                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_BOL + billOfLading.getId() + "?size=20&page=0", billOfLading.getBillOfLadingNo(), "", "", "submitBOL", CONFIRM_BOL);
                                String subject = getSubjectMailBOL(billOfLading.getBillOfLadingNo());
                                sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                            }
                        }
                    } else {
                        throw new BusinessException(String.format("Booking %s not exist.", item.getBookingConfirmation()));
                    }
                });
                billOfLading.setStatus(STATUS_BOL_CONFIRMED);
                billOfLadingRepository.save(billOfLading);
                return true;
            } else {
                throw new BusinessException("Bill Of Lading not exist.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
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

    private ResourceDTO convertToObjectResourceDTO(Object o) {
        ResourceDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, ResourceDTO.class);
        return dto;
    }

    @Override
    public BillOfLadingDTO createBillOfLading(BillOfLadingDTO billOfLadingDTO) throws IOException, URISyntaxException {
        try {
            BillOfLading billOfLading = new BillOfLading();
            BeanUtils.copyProperties(billOfLadingDTO, billOfLading);
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findOneByBillOfLadingNo(billOfLadingDTO.getBillOfLadingNo());
            if (oBillOfLading.isPresent()) {
                throw new BusinessException(String.format("Bill Of Lading No %s already exists.", billOfLadingDTO.getBillOfLadingNo()));
            }

            //get details PI from DTO
            Set<Booking> detailSet = billOfLadingDTO.getBooking().parallelStream().map(item -> {
                Optional<Booking> oBooking = bookingRepository.findById(item.getId());
                if (oBooking.isPresent()) {
                    Booking booking = oBooking.get();
                    booking.setStatus(STATUS_BOOKING_ON_BOARDING);
                    booking.getBookingProformaInvoice().parallelStream().forEach(element -> {
                        Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                        if (oProformaInvoice.isPresent()) {
                            ProformaInvoice proformaInvoice = oProformaInvoice.get();
                            PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                            purchaseOrders.setStatus(STATUS_PO_ON_BOARDING);
                            proformaInvoice.setStatus(STATUS_PI_ON_BOARDING);
                            purchaseOrdersRepository.save(purchaseOrders);
                        }
                    });
                    bookingRepository.save(booking);
                    return booking;
                } else {
                    throw new BusinessException(String.format("Booking %s not exist.", item.getBookingConfirmation()));
                }
            }).collect(Collectors.toSet());
            billOfLading.setBooking(detailSet);
            billOfLading.setStatus(STATUS_BOL_NEW);
            billOfLadingRepository.saveAndFlush(billOfLading);
            return mappingEntityToDto(billOfLading, BillOfLadingDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public BillOfLadingDTO updateBillOfLading(BillOfLadingDTO billOfLadingDTO) throws IOException, URISyntaxException {
        try {
            Optional<BillOfLading> oBillOfLading = billOfLadingRepository.findById(billOfLadingDTO.getId());
            if (oBillOfLading.isPresent()) {
                BillOfLading billOfLading = oBillOfLading.get();
                BeanUtils.copyProperties(billOfLadingDTO, billOfLading);
                billOfLading.setUpdatedAt(new Date().toInstant());
                //get booking
                Set<Booking> detailSet = billOfLadingDTO.getBooking().parallelStream().map(item -> {
                    Optional<Booking> oBooking = bookingRepository.findById(item.getId());
                    if (oBooking.isPresent()) {
                        Booking booking = oBooking.get();
                        Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                        for (BookingProformaInvoice element : bookingProformaInvoices) {
                            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                            if (oProformaInvoice.isPresent()) {
                                PurchaseOrders purchaseOrders = oProformaInvoice.get().getPurchaseOrders();
                                if (billOfLading.getAtd() != null) {
                                    purchaseOrders.setAtd(billOfLading.getAtd());
                                }
                                if (billOfLading.getEta() != null) {
                                    purchaseOrders.setEta(billOfLading.getEta());
                                }
                                purchaseOrdersRepository.save(purchaseOrders);


                            }
                        }
                        return booking;
                    } else {
                        throw new BusinessException(String.format("Booking %s not exist.", item.getBookingConfirmation()));
                    }
                }).collect(Collectors.toSet());
                billOfLading.setBooking(detailSet);
                billOfLading.setStatus(STATUS_BOL_UPDATE);
                billOfLadingRepository.saveAndFlush(billOfLading);
                return mappingEntityToDto(billOfLading, BillOfLadingDTO.class);
            } else {
                throw new BusinessException("Bill Of Lading not exist.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }
    }

    @Override
    public byte[] exportExcel(BillOfLadingExportExcelDTO dto) {
        try {
            byte[] result = null;
            Map<String, byte[]> files = new HashMap<>();
            List<BookingProformaInvoice> listBookingProformaInvoice = bookingProformaInvoiceRepository.findByBookingIdIn(dto.getIds());
            for (BookingProformaInvoice bookingProformaInvoice : listBookingProformaInvoice) {
                // export PL
                String invoice = bookingProformaInvoice.getBookingPackingList().getInvoice();
                if (CommonDataUtil.isNotEmpty(invoice)) {
                    String fileNamePL = "PackingList_" + invoice + ".xlsx";
                    String filePath = getFileResourcePath(GlobalConstant.FILE_UPLOAD);
                    fileNamePL = filePath + "/" + fileNamePL;
                    Path pathFile = Paths.get(fileNamePL);
                    bookingService.exportPKLFromBOL(fileNamePL, bookingProformaInvoice.getBookingPackingList().getId());
                    byte[] dataPL = Files.readAllBytes(pathFile);
                    Files.deleteIfExists(pathFile);
                    files.put(fileNamePL, dataPL);
                }

                // export CI
                FileDTO fileCI = commercialInvoiceService.exportExcel(bookingProformaInvoice.getBookingPackingList().getId());
                if (CommonDataUtil.isNotNull(fileCI)) {
                    files.put(fileCI.getFileName(), fileCI.getContent());
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


    private <T> T mappingEntityToDto(BillOfLading billOfLading, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(billOfLading, dto);
            List<ListBookingIds> listBookingIds = new ArrayList<>();
            if (billOfLading.getBooking() != null) {
                billOfLading.getBooking().forEach(i -> {
                    ListBookingIds listBookingId = new ListBookingIds();
                    listBookingId.setBookingConfirmation(i.getBookingConfirmation());
                    listBookingId.setId(i.getId());
                    listBookingIds.add(listBookingId);
                });
            }
            if (dto instanceof BillOfLadingMainDTO) {
                clazz.getMethod("setListBookingIds", List.class).invoke(dto, listBookingIds);
            }
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

}
