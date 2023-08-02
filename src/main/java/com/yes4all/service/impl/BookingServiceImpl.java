package com.yes4all.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVReader;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.*;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.BookingService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.utils.CommonDataUtil.getSubjectMail;
import static com.yes4all.constants.GlobalConstant.*;


/**
 * Service Implementation for managing {@link PurchaseOrders}.
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommercialInvoiceServiceImpl commercialInvoiceService;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    private static final String LINK_DETAIL_PKL = "/booking/packing-list/detail/";

    @Autowired
    private SendMailService sendMailService1;

    @Autowired
    private SendMailService sendMailService;
    @Value("${attribute.link.url}")
    private String linkPOMS;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingProformaInvoiceRepository bookingProformaInvoiceRepository;
    @Autowired
    private BookingPurchaseOrderRepository bookingPurchaseOrderRepository;
    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;
    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private BookingPackingListDetailRepository bookingPackingListDetailRepository;

    @Value("${attribute.host.url_pims}")
    private String linkPIMS;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public BookingDTO createBooking(BookingDTO bookingDetailsDTO) {
        try {
            Booking booking = new Booking();
            BeanUtils.copyProperties(bookingDetailsDTO, booking);
            Set<BookingPurchaseOrder> bookingPurchaseOrderSet = bookingDetailsDTO.getBookingPurchaseOrderDTO().stream().map(item -> {
                BookingPurchaseOrder detail = new BookingPurchaseOrder();
                BeanUtils.copyProperties(item, detail);
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingPurchaseOrder(bookingPurchaseOrderSet);
            Set<BookingProformaInvoice> bookingProformaInvoice = bookingDetailsDTO.getBookingProformaInvoiceMainDTO().stream().map(item -> {
                BookingProformaInvoice detail = new BookingProformaInvoice();
                BeanUtils.copyProperties(item, detail);
                if (item.getBookingPackingListDTO() != null) {
                    BookingPackingList bookingPackingList = new BookingPackingList();
                    Set<BookingPackingListDetail> bookingPackingListDetailSet = new HashSet<>();
                    BeanUtils.copyProperties(item.getBookingPackingListDTO(), bookingPackingList);
                    if (item.getBookingPackingListDTO() != null) {
                        bookingPackingListDetailSet = item.getBookingPackingListDTO().getBookingPackingListDetailsDTO().stream().map(k -> {
                            BookingPackingListDetail bookingPackingListDetail = new BookingPackingListDetail();
                            BeanUtils.copyProperties(k, bookingPackingListDetail);
                            return bookingPackingListDetail;
                        }).collect(Collectors.toSet());
                        bookingPackingList.setBookingPackingListDetail(bookingPackingListDetailSet);
                    }
                    detail.setBookingPackingList(bookingPackingList);
                }
                Optional<ProformaInvoice> proformaInvoice = proformaInvoiceRepository.findByOrderNo(item.getInvoiceNo());
                if (!proformaInvoice.isPresent()) {
                    throw new BusinessException("Proforma Invoice not exits.");
                }
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingProformaInvoice(bookingProformaInvoice);
            Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocation = bookingDetailsDTO.getBookingPurchaseOrderLocationDTO().stream().map(item -> {
                BookingPurchaseOrderLocation detail = new BookingPurchaseOrderLocation();
                BeanUtils.copyProperties(item, detail);
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingPurchaseOrderLocation(bookingPurchaseOrderLocation);
            bookingRepository.saveAndFlush(booking);
            return mappingEntityToDto(booking, BookingDTO.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    @Override
    public List<BookingMainDTO> getAllBookingListBookingNo(SearchBookingDTO request) {
        List<Booking> bookings;
        if (Boolean.TRUE.equals(request.getIsSearch())) {
            bookings = bookingRepository.findAllByNotInBookingNos(request.getListBookingNo(), request.getId() == null ? 0 : request.getId());
        } else {
            bookings = bookingRepository.findAllByBookingNos(request.getListBookingNo());
        }
        if (!bookings.isEmpty()) {
            return bookings.stream().map(booking -> {
                BookingMainDTO bookingMainDTO = new BookingMainDTO();
                BeanUtils.copyProperties(booking, bookingMainDTO);
                bookingMainDTO.setPOAmazon(booking.getBookingProformaInvoice().stream().map(BookingProformaInvoice::getPoAmazon).distinct().collect(Collectors.joining(",")));
                return bookingMainDTO;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public BookingDTO completedBooking(Integer id) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(id);
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                if (booking.getStatus() != GlobalConstant.STATUS_BOOKING_ON_BOARDING && booking.getFreightTerms().equals("MPP")) {
                    throw new BusinessException("Completed Booking failed: The status must be on boarding.");
                }
                Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                Map<Integer, Integer> bookingPackingListStatus = new HashMap<>();
                Map<Integer, Integer> bookingCIStatus = new HashMap<>();
                List<PurchaseOrders> purchaseOrdersList = new ArrayList<>();
                bookingProformaInvoices.parallelStream().forEach(item -> {
                    bookingPackingListStatus.put(item.getBookingPackingList().getId(), item.getBookingPackingList().getStatus());
                    if (item.getBookingPackingList().getCommercialInvoice() != null) {
                        bookingCIStatus.put(item.getBookingPackingList().getCommercialInvoice().getId(), item.getBookingPackingList().getCommercialInvoice().getStatus());
                    } else {
                        //set key=value=-1 if 1 packing list not create
                        bookingCIStatus.put(-1, -1);
                    }
                    //set status= PO DONE if booking completed
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(item.getProformaInvoiceNo());
                    if (oProformaInvoice.isPresent()) {
                        ProformaInvoice proformaInvoice = oProformaInvoice.get();
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_DONE);
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_DONE);
                        purchaseOrdersList.add(purchaseOrders);
                    }
                });
                // check status PKL
                boolean checkStatusPackingList = true;
                for (Map.Entry<Integer, Integer> entry : bookingPackingListStatus.entrySet()) {
                    if (entry.getValue() != GlobalConstant.STATUS_PKL_CONFIRMED) {
                        checkStatusPackingList = false;
                    }
                }
                // check status CI
                boolean checkStatusCI = true;
                for (Map.Entry<Integer, Integer> entry : bookingCIStatus.entrySet()) {
                    if (entry.getValue() != GlobalConstant.STATUS_CI_CONFIRMED) {
                        checkStatusCI = false;
                    }
                }
                // if all status packing list and CI equal confirm  then status booking change completed
                if (checkStatusPackingList && checkStatusCI) {
                    booking.setStatus(GlobalConstant.STATUS_BOOKING_COMPLETED);
                    bookingRepository.save(booking);
                    purchaseOrdersRepository.saveAll(purchaseOrdersList);
                } else {
                    throw new BusinessException("Packing List and CI have not been confirmed.");
                }
                return CommonDataUtil.getModelMapper().map(booking, BookingDTO.class);
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public BookingPackingListDTO submitPackingList(BookingPackingListDTO request, Integer id) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(id);
            List<BookingPackingList> bookingPackingLists = new ArrayList<>();
            String supplier = "";
            if (oBooking.isPresent()) {
                if (oBooking.get().getStatus() == GlobalConstant.STATUS_BOOKING_CANCEL) {
                    throw new BusinessException("Booking have been CANCELED, can not create Packing List.");
                }
                if (oBooking.get().getStatus() == GlobalConstant.STATUS_BOOKING_ON_BOARDING) {
                    throw new BusinessException("Booking have been ON BOARDING, can not create Packing List.");
                }
                if (oBooking.get().getStatus() == GlobalConstant.STATUS_BOOKING_COMPLETED) {
                    throw new BusinessException("Booking have been COMPLETED, can not create Packing List.");
                }
            } else {
                throw new BusinessException("Can not find Booking");
            }
            Set<BookingProformaInvoice> bookingProformaInvoiceSet = new HashSet<>();
            if (request.getBookingPackingListIds() != null) {
                for (Integer element : request.getBookingPackingListIds()) {
                    Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(element);
                    if (!oBookingPackingList.isPresent()) {
                        throw new BusinessException("Packing List not found.");
                    }
                    BookingPackingList bookingPackingList = oBookingPackingList.get();
                    if (bookingPackingList.getStatus() != GlobalConstant.STATUS_PKL_NEW) {
                        throw new BusinessException("Packing List have been created, Can not delete.");
                    }
                    //delete ids packing list and create new packing list just 1 foreign key id relationship proforma invoice booking
                    bookingProformaInvoiceSet.addAll(bookingPackingList.getBookingProformaInvoice());
                    bookingPackingList.setBookingProformaInvoice(null);
                    supplier = bookingPackingList.getSupplier();
                    bookingPackingLists.add(bookingPackingList);
                }
            } else {
                throw new BusinessException("Missing data");
            }

            BookingPackingList bookingPackingList = new BookingPackingList();
            BeanUtils.copyProperties(request, bookingPackingList);
            Set<BookingPackingListDetail> detailSet = request.getBookingPackingListDetailsDTO().parallelStream().map(item -> {
                BookingPackingListDetail bookingPackingListDetail;
                bookingPackingListDetail = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetail.class);
                bookingPackingListDetail.setId(null);
                return bookingPackingListDetail;
            }).collect(Collectors.toSet());
            if (request.getBookingPackingListContainerPallet() != null) {
                Set<BookingPackingListContainerPallet> detailContainerPalletSet = request.getBookingPackingListContainerPallet().parallelStream().map(item -> {
                    BookingPackingListContainerPallet bookingPackingListContainerPallet;
                    bookingPackingListContainerPallet = CommonDataUtil.getModelMapper().map(item, BookingPackingListContainerPallet.class);
                    bookingPackingListContainerPallet.setId(null);
                    return bookingPackingListContainerPallet;
                }).collect(Collectors.toSet());
                bookingPackingList.setBookingPackingListContainerPallet(detailContainerPalletSet);
            }
            bookingPackingList.setBookingPackingListDetail(detailSet);
            bookingPackingList.setStatus(GlobalConstant.STATUS_PKL_SUBMIT);
            bookingPackingList.setSupplier(supplier);
            bookingPackingListRepository.save(bookingPackingList);
            //create CI with detail Packing list
            CommercialInvoice commercialInvoice = new CommercialInvoice();
            Set<CommercialInvoiceDetail> commercialInvoiceDetailSet = new HashSet<>();
            Set<BookingProformaInvoiceMainDTO> bookingProformaInvoiceList = request.getBookingProformaInvoiceMainDTO();

            for (BookingProformaInvoiceMainDTO bookingProformaInvoiceDTO : bookingProformaInvoiceList) {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(bookingProformaInvoiceDTO.getProformaInvoiceNo());
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                    //after submit packing list with update status PI
                    proformaInvoice.setStatus(GlobalConstant.STATUS_PI_CREATED_PKL);
                    //after submit packing list with update status PO
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PACKING_LIST_CREATED);
                    purchaseOrdersRepository.save(purchaseOrders);
                    //save invoiceNo and PO number at Commercial Invoice increase performance get data
                    String invoiceNo = commercialInvoice.getInvoiceNo();
                    if (invoiceNo == null || invoiceNo.length() == 0) {
                        invoiceNo = bookingProformaInvoiceDTO.getProformaInvoiceNo();
                    } else {
                        invoiceNo += " - " + bookingProformaInvoiceDTO.getProformaInvoiceNo();
                    }
                    String numberPO = commercialInvoice.getNumberPO();
                    if (numberPO == null || numberPO.length() == 0) {
                        numberPO = purchaseOrders.getPoNumber();
                    } else {
                        if (numberPO.indexOf(purchaseOrders.getPoNumber()) < 0) {
                            numberPO += " - " + purchaseOrders.getPoNumber();
                        }
                    }


                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoice.getProformaInvoiceDetail();
                    BeanUtils.copyProperties(proformaInvoice, commercialInvoice);
                    commercialInvoice.setId(null);
                    commercialInvoice.setNumberPO(numberPO);
                    commercialInvoice.setInvoiceNo(invoiceNo);
                    // copy data detail packing list to detail Commercial Invoice
                    detailSet.stream().forEach(packingList -> {
                        if (packingList.getQuantity() > 0) {
                            CommercialInvoiceDetail commercialInvoiceDetail = new CommercialInvoiceDetail();
                            Long cdcVersionMax = proformaInvoiceDetailSet.stream().mapToLong(ProformaInvoiceDetail::getCdcVersion).max().orElseThrow(
                                () -> {
                                    throw new BusinessException("Can not find new version detail proforma invoice");
                                }
                            );
                            ProformaInvoiceDetail proformaInvoiceDetail = proformaInvoiceDetailSet.stream().filter(pi -> pi.getCdcVersion().equals(cdcVersionMax) && pi.getSku().equals(packingList.getSku()) && pi.getFromSo().equals(packingList.getPoNumber())).findFirst().orElse(null);
                            if (proformaInvoiceDetail != null) {
                                commercialInvoiceDetail.setStatus(0);
                                commercialInvoiceDetail.setSku(packingList.getSku());
                                commercialInvoiceDetail.setaSin(packingList.getaSin());
                                commercialInvoiceDetail.setFromSo(packingList.getPoNumber());
                                commercialInvoiceDetail.setProductTitle(packingList.getTitle());
                                commercialInvoiceDetail.setQty(packingList.getQuantity());
                                commercialInvoiceDetail.setUnitPrice(proformaInvoiceDetail.getUnitPrice());
                                commercialInvoiceDetail.setAmount(commercialInvoiceDetail.getQty() * commercialInvoiceDetail.getUnitPrice());
                                commercialInvoiceDetail.setUnitPriceAllocated(proformaInvoiceDetail.getUnitPrice());
                                commercialInvoiceDetailSet.add(commercialInvoiceDetail);
                            }
                        }
                    });
                    proformaInvoiceRepository.save(proformaInvoice);
                } else {
                    throw new BusinessException("Proforma Invoice not exists!");
                }
            }
            bookingProformaInvoiceSet = bookingProformaInvoiceSet.stream().map(i -> {
                i.setBookingPackingList(bookingPackingList);
                return i;
            }).collect(Collectors.toSet());
            bookingPackingList.setBookingProformaInvoice(bookingProformaInvoiceSet);
            bookingPackingListRepository.save(bookingPackingList);

//            //define data commercialInvoice from vendor PIMS
//            Optional<Vendor> oVendor = vendorRepository.findByVendorCode(commercialInvoice.getSupplier());
//            if (oVendor.isPresent()) {
//                Vendor vendor = oVendor.get();
//                commercialInvoice.setSeller(vendor.getVendorName() + " \n " + vendor.getFactoryAddress());
//                if (vendor.getBankInformation() != null) {
//                    commercialInvoice.setCompanyName(vendor.getBankInformation().getCompanyName());
//                    commercialInvoice.setAcNumber(vendor.getBankInformation().getAcNumber());
//                    commercialInvoice.setBeneficiaryBank(vendor.getBankInformation().getBeneficiaryBank());
//                    commercialInvoice.setSwiftCode(vendor.getBankInformation().getSwiftCode());
//                }
//            } else {
//                throw new BusinessException("Vendor id not exists!");
//            }
            Booking booking = oBooking.get();
            booking.setStatus(STATUS_BOOKING_WAITING_PKL);
            bookingRepository.save(booking);
            commercialInvoice.setAmount(commercialInvoiceDetailSet.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetailSet);
            commercialInvoice.setBookingPackingList(bookingPackingList);
            commercialInvoice.setStatus(GlobalConstant.STATUS_CI_NEW);
            commercialInvoice.setSupplier(bookingPackingList.getSupplier());
            commercialInvoice.setSupplierUpdatedLatest(true);
            bookingPackingListRepository.deleteAll(bookingPackingLists);
            commercialInvoiceRepository.saveAndFlush(commercialInvoice);
            return CommonDataUtil.getModelMapper().map(bookingPackingList, BookingPackingListDTO.class);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }


    }

    @Override
    public BookingPackingListDTO confirmPackingList(BodyConfirmCIDTO request) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(request.getBookingId());
            if (oBooking.isPresent()) {
                if (oBooking.get().getStatus() == GlobalConstant.STATUS_BOOKING_CANCEL) {
                    throw new BusinessException("Booking have been canceled, can not confirm Packing List.");
                }
            } else {
                throw new BusinessException("Can not find Booking");
            }
            Booking booking = oBooking.get();
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(request.getPackingListId());
            if (!oBookingPackingList.isPresent()) {
                throw new BusinessException("Can not find Packing List");
            }
            BookingPackingList bookingPackingList = oBookingPackingList.get();
            if (bookingPackingList.getStatus() == GlobalConstant.STATUS_PKL_CONFIRMED) {
                throw new BusinessException("Packing list already confirm.");
            }

            bookingPackingList.setStatus(GlobalConstant.STATUS_PKL_CONFIRMED);

            // confirm packing list then confirm commercial invoice
            CommercialInvoice commercialInvoice = bookingPackingList.getCommercialInvoice();
            if (commercialInvoice.getStatus() != STATUS_CI_SENT_BUYER) {
                throw new BusinessException("The status Invoice not valid.");
            }
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
            bookingPackingListRepository.saveAndFlush(bookingPackingList);

            return CommonDataUtil.getModelMapper().map(bookingPackingList, BookingPackingListDTO.class);

        } catch (
            Exception e) {
            throw new BusinessException(e.getMessage());
        }

    }

    @Override
    public boolean sendPackingList(Integer id) {
        try {
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(id);
            if (oBookingPackingList.isPresent()) {
                if (oBookingPackingList.get().getStatus() != STATUS_PKL_SUBMIT) {
                    throw new BusinessException("The only status Packing List is Submit can Send.");
                }
            } else {
                throw new BusinessException("Can not find Packing List");
            }
            Booking booking;
            Optional<Booking> oBooking = bookingRepository.findAllByPackingListId(id);
            if (oBooking.isPresent()) {
                booking = oBooking.get();
            } else {
                throw new BusinessException("Can not find Booking");
            }
            BookingPackingList bookingPackingList = oBookingPackingList.get();
            bookingPackingList.setStatus(STATUS_PKL_SEND);
            //check status all PKL Another
            final boolean[] isAllPKLSent = {true};
            booking.getBookingProformaInvoice().parallelStream().filter(i -> !i.getBookingPackingList().getId().equals(id)).forEach(element -> {
                BookingPackingList bookingPackingListElement = element.getBookingPackingList();
                if (bookingPackingListElement.getStatus() != STATUS_PKL_SEND && bookingPackingListElement.getStatus() != STATUS_PKL_CONFIRMED) {
                    isAllPKLSent[0] = false;
                }
            });
            if (isAllPKLSent[0]) {
                booking.setStatus(STATUS_BOOKING_REVIEW_CI_PKL);
            }
            bookingRepository.save(booking);
            //-------
            final double[] totalAmount = {0};
            //set detail CI
            CommercialInvoice commercialInvoice = bookingPackingList.getCommercialInvoice();
            Set<CommercialInvoiceDetail> detailCISet = commercialInvoice.getCommercialInvoiceDetail().stream().map(item -> {
                //write log the first
                Set<CommercialInvoiceDetailLog> commercialInvoiceDetailLogs = new HashSet<>();
                CommercialInvoiceDetailLog commercialInvoiceDetailLogNew = new CommercialInvoiceDetailLog();
                commercialInvoiceDetailLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
                commercialInvoiceDetailLogNew.setUpdatedDate(new Date().toInstant());
                commercialInvoiceDetailLogNew.setVersion(1);
                commercialInvoiceDetailLogNew.setUnitPriceAfter(item.getUnitPrice());
                if (commercialInvoice.getTruckingCost() != null && commercialInvoice.getTruckingCost() > 0) {
                    double amount = Double.parseDouble(df.format(item.getQty() * item.getUnitPrice()));
                    commercialInvoiceDetailLogNew.setAmountBefore(amount);
                    totalAmount[0] += amount;
                }
                commercialInvoiceDetailLogNew.setAmountAfter(item.getAmount());
                commercialInvoiceDetailLogNew.setUnitPriceAfterAllocate(item.getUnitPriceAllocated());
                commercialInvoiceDetailLogs.add(commercialInvoiceDetailLogNew);
                item.setCommercialInvoiceDetailLog(commercialInvoiceDetailLogs);
                return item;
            }).collect(Collectors.toSet());
            commercialInvoice.setCommercialInvoiceDetail(detailCISet);

            //write log total amount
            Set<CommercialInvoiceTotalAmountLog> commercialInvoiceTotalAmountLogs = new HashSet<>();
            CommercialInvoiceTotalAmountLog commercialInvoiceTotalAmountLogNew = new CommercialInvoiceTotalAmountLog();
            commercialInvoiceTotalAmountLogNew.setUpdatedBy(commercialInvoice.getUpdatedBy());
            commercialInvoiceTotalAmountLogNew.setUpdatedDate(new Date().toInstant());
            commercialInvoiceTotalAmountLogNew.setVersion(1);
            if (commercialInvoice.getTruckingCost() != null && commercialInvoice.getTruckingCost() > 0) {
                commercialInvoiceTotalAmountLogNew.setAmountTotalBefore(totalAmount[0]);
            }
            commercialInvoiceTotalAmountLogNew.setAmountTotalAfter(commercialInvoice.getAmount());
            commercialInvoiceTotalAmountLogNew.setAmountTotalAfter(commercialInvoice.getAmount());
            commercialInvoiceTotalAmountLogNew.setTruckingCostLog(commercialInvoice.getTruckingCost());
            commercialInvoiceTotalAmountLogs.add(commercialInvoiceTotalAmountLogNew);
            commercialInvoice.setCommercialInvoiceTotalAmountLog(commercialInvoiceTotalAmountLogs);

            commercialInvoice.setStatus(GlobalConstant.STATUS_CI_SENT_BUYER);

//            //check if not attachment file can not send PKL
//            List<Resource> resourcesListing = resourceRepository.findByFileTypeAndCommercialInvoiceId(GlobalConstant.FILE_UPLOAD, commercialInvoice.getId());
//            if (!CommonDataUtil.isNotEmpty(resourcesListing)) {
//                throw new BusinessException("Please attach the necessary file(s) before clicking Send.");
//            }
            Optional<User> userSupplier = userRepository.findOneByVendor(commercialInvoice.getSupplier());
            if (userSupplier.isEmpty()) {
                throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", commercialInvoice.getSupplier()));
            }
            User userVendor = userSupplier.get();
            Set<BookingProformaInvoice> bookingProformaInvoice = bookingPackingList.getBookingProformaInvoice();
            //get PO Number from CI
            String strPONumber = bookingProformaInvoice.stream().map(bookingPI -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(bookingPI.getProformaInvoiceNo());
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    return proformaInvoice.getPurchaseOrders().getPoNumber();
                } else {
                    throw new BusinessException(String.format("Proforma Invoice %s not exists", bookingPI.getProformaInvoiceNo()));
                }
            }).collect(Collectors.joining(" - "));
            commercialInvoice.setTheFirstReject(true);
            List<PurchaseOrders> purchaseOrdersList = bookingPackingList.getBookingProformaInvoice().stream().map(element -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                if (oProformaInvoice.isPresent()) {
                    List<String> listEmail = new ArrayList<>();
                    List<String> listEmailCC = new ArrayList<>();
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    List<String> listMailSC = getListUserSC(userVendor);
                    List<String> listMailPU = getListUserPU(userVendor);
                    String userPUPrimaryStr = proformaInvoice.getUserPUPrimary();
                    if (userPUPrimaryStr != null) {
                        Optional<User> oUserPUPrimary = userRepository.findOneByLogin(userPUPrimaryStr);
                        if (!oUserPUPrimary.isPresent()) {
                            throw new BusinessException("Can not find user PU.");
                        }
                        User userPUPrimary = oUserPUPrimary.get();
                        listEmail.add(userPUPrimary.getEmail());
                        listEmailCC.addAll(listMailSC);
                        listEmailCC.addAll(listMailPU);
                        listEmailCC.add(userVendor.getEmail());
                    }
                    String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PKL + commercialInvoice.getBookingPackingList().getId() + "?size=20&page=0", strPONumber, supplier, "The Commercial Invoice and Packing List for PO ", "created", "CreatedCI");
                    String subject = getSubjectMail(proformaInvoice.getPurchaseOrders().getPoNumber(), proformaInvoice.getPurchaseOrders().getCountry(), userVendor);
                    sendMailService.sendMail(subject, content, listEmail, listEmailCC, null, null);
                    return proformaInvoice.getPurchaseOrders();
                }
                return null;
            }).collect(Collectors.toList());
            if (purchaseOrdersList.isEmpty()) {
                throw new BusinessException("Can not find Purchase Order");
            }
            bookingPackingList.setSendMail(false);
            bookingPackingListRepository.saveAndFlush(bookingPackingList);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return false;
    }

    @Override
    public BookingDTO sendBooking(SendBookingDTO request) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(request.getId());
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                if (booking.getStatus() != STATUS_BOOKING_UPLOAD) {
                    throw new BusinessException("The status not valid.");
                }
                booking.setConsolidator(request.getVendor());
                booking.setStatus(STATUS_BOOKING_CREATED);
                Set<BookingPurchaseOrder> bookingPurchaseOrderSet = booking.getBookingPurchaseOrder();

                bookingPurchaseOrderSet.stream().forEach(item -> {
                    if (item.getStatusDetail().equals("Select reason")) {
                        throw new BusinessException(String.format("Please choose reason adjust when adjust quantity { ASin: %s ; FromSo: %s }", item.getaSin(), item.getPoNumber()));
                    }
                });
                bookingRepository.saveAndFlush(booking);

                // Send mail
                String bookingConfirmation = booking.getBookingConfirmation();
                String titleLink = "View Booking Confirmation";
                String link = linkPOMS + GlobalConstant.LINK_DETAIL_BOOKING + booking.getId();
                List<PurchaseOrders> purchaseOrdersList = booking.getBookingProformaInvoice().stream().map(element -> {
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                    if (oProformaInvoice.isPresent()) {
                        return oProformaInvoice.get().getPurchaseOrders();
                    }
                    return null;
                }).collect(Collectors.toList());
                if (purchaseOrdersList.isEmpty()) {
                    throw new BusinessException("Can not find Purchase Order");
                }
                Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersList.stream().max(Comparator.comparing(PurchaseOrders::getId));
                final PurchaseOrders[] purchaseOrders = {new PurchaseOrders()};
                if (oPurchaseOrders.isPresent()) {
                    purchaseOrders[0] = oPurchaseOrders.get();
                } else {
                    throw new BusinessException("Can not find Purchase Order");
                }
                purchaseOrders[0] = oPurchaseOrders.get();
                List<String> invoices = booking.getBookingProformaInvoice().parallelStream().map(BookingProformaInvoice::getProformaInvoiceNo).collect(Collectors.toList());
                List<ProformaInvoice> proformaInvoices = proformaInvoiceRepository.findByOrderNoIn(invoices);
                Map<String, User> listUser = new HashMap<>();
                proformaInvoices.stream().forEach(item -> {
                    Optional<User> userEmailsSupplier = userRepository.findOneByVendor(item.getSupplier());
                    if (!userEmailsSupplier.isPresent()) {
                        throw new BusinessException(String.format("Can not find user Supplier with vendor %s in the system.", request.getVendor()));
                    }
                    listUser.put(item.getSupplier(), userEmailsSupplier.get());
                });
                // send mail to consolidator
                Optional<ProformaInvoice> oProformaInvoiceConsolidator = proformaInvoices.stream().filter(i -> i.getSupplier().equals(request.getVendor())).collect(Collectors.toList()).stream().findFirst();

                if (oProformaInvoiceConsolidator.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoiceConsolidator.get();
                    User user = listUser.get(proformaInvoice.getSupplier());
                    String poAmazon = proformaInvoice.getProformaInvoiceDetail().stream().map(ProformaInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(", "));
                    String content = CommonDataUtil.contentSendBooking(bookingConfirmation, proformaInvoice.getOrderNo(), poAmazon, request.getVendor(), booking.getCreatedAt(), link, titleLink);
                    purchaseOrders[0] = proformaInvoice.getPurchaseOrders();
                    String subject = getSubjectMail(purchaseOrders[0].getPoNumber(), purchaseOrders[0].getCountry(), user);
                    List<String> listMailSC = getListUserSC(user);
                    List<String> listMailPU = getListUserPU(user);
                    List<String> listMailCC = new ArrayList<>();
                    listMailCC.addAll(listMailSC);
                    listMailCC.addAll(listMailPU);
                    sendMailService.sendMail(subject, content, Collections.singletonList(user.getEmail()), listMailCC, null, null);
                }
                // send mail to supplier another.
                List<ProformaInvoice> proformaInvoiceList = proformaInvoices.stream().filter(i -> !i.getSupplier().equals(request.getVendor())).collect(Collectors.toList());
                proformaInvoiceList.stream().forEach(item -> {
                    User user = listUser.get(item.getSupplier());
                    purchaseOrders[0] = item.getPurchaseOrders();
                    String poAmazon = item.getProformaInvoiceDetail().stream().map(ProformaInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(", "));
                    String subject = getSubjectMail(purchaseOrders[0].getPoNumber(), purchaseOrders[0].getCountry(), user);
                    String content = CommonDataUtil.contentSendBooking(bookingConfirmation, item
                        .getOrderNo(), poAmazon, request.getVendor(), booking.getCreatedAt(), link, titleLink);
                    List<String> listMailSC = getListUserSC(user);
                    List<String> listMailPU = getListUserPU(user);
                    List<String> listMailCC = new ArrayList<>();
                    listMailCC.addAll(listMailSC);
                    listMailCC.addAll(listMailPU);
                    sendMailService.sendMail(subject, content, Collections.singletonList(user.getEmail()), listMailCC, null, null);
                });


                return CommonDataUtil.getModelMapper().map(booking, BookingDTO.class);
            } else {
                throw new BusinessException("The booking not exists.");
            }
        } catch (Exception e) {
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
                    if (!oUser.isPresent()) {
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
                    if (!oUser.isPresent()) {
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
    public boolean deleteBooking(Integer id) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(id);
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                if (booking.getStatus() != STATUS_BOOKING_UPLOAD) {
                    throw new BusinessException("The status not valid.");
                }
                Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                bookingProformaInvoices.stream().forEach(item -> {
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(item.getProformaInvoiceNo());
                    if (!oProformaInvoice.isPresent()) {
                        throw new BusinessException("Can not find proforma invoice.");
                    } else {
                        ProformaInvoice proformaInvoice = oProformaInvoice.get();
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_CONFIRMED);
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_CONFIRMED);
                        purchaseOrdersRepository.save(purchaseOrders);
                        purchaseOrdersRepository.save(purchaseOrders);
                    }
                });
                bookingRepository.deleteById(id);
                return true;
            } else {
                throw new BusinessException("The booking not exists.");
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public BookingDetailsDTO getBookingDetailsDTO(BookingPageGetDetailDTO request) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(request.getId());
            if (oBooking.isPresent()) {
                Set<String> listVendor;
                Booking booking = oBooking.get();
                String supplier = request.getVendor();
                BookingDetailsDTO data = CommonDataUtil.getModelMapper().map(booking, BookingDetailsDTO.class);
                Optional<User> oUser = userRepository.findOneByLogin(booking.getCreatedBy());
                if (oUser.isPresent()) {
                    data.setUploadBy(oUser.get().getLastName() + " " + oUser.get().getFirstName());
                }
                Pageable pageablePI = PageRequestUtil.genPageRequest(request.getPagePI(), request.getSizePI(), Sort.Direction.DESC, "id");
                Pageable pageableResource = PageRequestUtil.genPageRequest(request.getPageResource(), request.getSizeResource(), Sort.Direction.DESC, "uploadDate");
                //
                String finalSupplier = supplier;
                List<BookingPurchaseOrder> pODetail = booking.getBookingPurchaseOrder().stream().filter(i -> finalSupplier.isEmpty() || i.getSupplier().equals(finalSupplier)).collect(Collectors.toList());
                if(request.getVendor().equals(booking.getConsolidator())){
                    supplier="";
                }
                Page<BookingProformaInvoice> pagePIDetail = bookingProformaInvoiceRepository.findAllByBookingAndSupplierContainingIgnoreCase(booking.getId(),supplier, pageablePI);
                Page<Resource> pageResourceDetail = resourceRepository.findByFileTypeAndBookingId(GlobalConstant.FILE_UPLOAD, booking.getId(), pageableResource);
                List<BookingPurchaseOrderDTO> bookingPurchaseOrderDTO = pODetail.stream().map(this::convertToObjectBookingPurchaseOrderDTO).collect(Collectors.toList());
                if (request.getVendor().length() > 0) {
                    long count = bookingPurchaseOrderDTO.parallelStream().filter(i -> i.getSupplier().equals(request.getVendor())).count();
                    if (count == 0) {
                        throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                    }
                }
                if (request.getVendor().length() > 0 && booking.getStatus() == STATUS_BOOKING_UPLOAD) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                Page<BookingProformaInvoiceMainDTO> pageBookingProformaInvoiceMainDTO = pagePIDetail.map(this::convertToObjectBookingProformaInvoiceMainDTO);
                Page<ResourceDTO> pageResourceDTO = pageResourceDetail.map(this::convertToObjectResourceDTO);
                data.setProformaInvoice(pageBookingProformaInvoiceMainDTO);
                data.setBookingPurchaseOrder(bookingPurchaseOrderDTO);
                data.setResource(pageResourceDTO);
                //get list vendor
                listVendor = pODetail.stream().map(BookingPurchaseOrder::getSupplier).collect(Collectors.toSet());
                if (Objects.equals(booking.getStatus(), STATUS_BOOKING_UPLOAD)) {
                    data.setListVendor(listVendor);
                }
                if (data.getConsolidator() == null) {
                    data.setConsolidator("");
                }
                return data;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public Integer updateBooking(BookingDetailsDTO request) {
        try {
            Optional<Booking> oBooking = bookingRepository.findById(request.getId());
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                if (booking.getStatus() == GlobalConstant.STATUS_BOOKING_CANCEL) {
                    throw new BusinessException("Booking have been canceled, can not update Booking.");
                }
                booking.setCds(request.getCds());
                booking.setShipDate(request.getShipDate());
                Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                for (BookingProformaInvoice element : bookingProformaInvoices) {
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                    if (oProformaInvoice.isPresent()) {
                        PurchaseOrders purchaseOrders = oProformaInvoice.get().getPurchaseOrders();

                        if (booking.getCds() != null) {
                            ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
                            LocalDate cdsDate = LocalDate.ofInstant(booking.getCds(), zone);
                            purchaseOrders.setActualShipDate(cdsDate);
                            purchaseOrders.setIsSendmail(false);
                            purchaseOrdersRepository.save(purchaseOrders);
                        }
                    }
                }
                List<BookingPurchaseOrderDTO> bookingPurchaseOrderDTOs = request.getBookingPurchaseOrder();
                Set<BookingPurchaseOrder> detailSet = bookingPurchaseOrderDTOs.stream().map(i -> {
                    BookingPurchaseOrder bookingPurchaseOrder;
                    bookingPurchaseOrder = CommonDataUtil.getModelMapper().map(i, BookingPurchaseOrder.class);
                    return bookingPurchaseOrder;
                }).collect(Collectors.toSet());
                booking.setBookingPurchaseOrder(detailSet);
                bookingRepository.saveAndFlush(booking);
                return booking.getId();
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public ResultDTO cancelBooking(Integer id) {
        Optional<Booking> oBooking = bookingRepository.findById(id);
        ResultDTO resultDTO = new ResultDTO();
        try {
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                Set<BookingProformaInvoice> bookingProformaInvoiceSet = booking.getBookingProformaInvoice();
                bookingProformaInvoiceSet.stream().forEach(element -> {
                    BookingPackingList bookingPackingList = element.getBookingPackingList();
                    if (bookingPackingList.getCommercialInvoice() != null) {
                        throw new BusinessException("This booking already exists Invoice.Can not cancel!");
                    }
                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNo(element.getProformaInvoiceNo());
                    if (oProformaInvoice.isPresent()) {
                        ProformaInvoice proformaInvoice = oProformaInvoice.get();
                        PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PI_REVIEWING);
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_Y4A_REVIEW);
                        purchaseOrdersRepository.save(purchaseOrders);
                    }
                });
                booking.setStatus(GlobalConstant.STATUS_BOOKING_CANCEL);
                bookingRepository.saveAndFlush(booking);
                resultDTO.setId(booking.getId());
                return resultDTO;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
        return resultDTO;
    }

    @Override
    public Page<BookingMainDTO> listingBookingWithCondition(Integer page, Integer
        limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "updated_at");
        Page<Booking> data = bookingRepository.findByCondition(filterParams.get("booking"), filterParams.get("poAmazonBooking"), filterParams.get("masterPOCIBooking"), filterParams.get("supplier"), pageable);
        return data.map(item -> mappingEntityToDto(item, BookingMainDTO.class));
    }

    @Override
    public BookingPackingListDTO getPackingListDetailsDTO(BodyGetDetailDTO request) {
        try {
            Optional<BookingPackingList> data = bookingPackingListRepository.findById(request.getId());
            BookingPackingListDTO bookingPackingListDTO = new BookingPackingListDTO();
            CommercialInvoiceDTO commercialInvoiceDTO;
            if (data.isPresent()) {
                final Booking[] booking = new Booking[1];
                BookingPackingList bookingPackingList = data.get();

                BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
                if (bookingPackingList.getCommercialInvoice() != null) {
                    CommercialInvoice commercialInvoice = bookingPackingList.getCommercialInvoice();
                    BodyGetDetailDTO bodyGetDetailDT = new BodyGetDetailDTO();
                    bodyGetDetailDT.setId(commercialInvoice.getId());
                    bodyGetDetailDT.setVendor(request.getVendor());
                    bodyGetDetailDT.setIsSupplier(request.getIsSupplier());
                    bodyGetDetailDT.setIsViewCI(false);
                    commercialInvoiceDTO = commercialInvoiceService.getCommercialInvoiceDetail(bodyGetDetailDT,true);
                    bookingPackingListDTO.setCommercialInvoice(commercialInvoiceDTO);
                }
                Set<BookingPackingListDetailsDTO> detailSet = bookingPackingList.getBookingPackingListDetail().parallelStream().map(item -> {
                    BookingPackingListDetailsDTO bookingPackingListDetailsDTO;
                    bookingPackingListDetailsDTO = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetailsDTO.class);
                    return bookingPackingListDetailsDTO;
                }).collect(Collectors.toSet());
                Set<BookingProformaInvoiceMainDTO> detailPISet = bookingPackingList.getBookingProformaInvoice().parallelStream().map(item -> {
                    BookingProformaInvoiceMainDTO bookingProformaInvoiceMainDTO;
                    bookingPackingListDTO.setBookingId(item.getBooking().getId());
                    booking[0] =item.getBooking();
                    bookingProformaInvoiceMainDTO = CommonDataUtil.getModelMapper().map(item, BookingProformaInvoiceMainDTO.class);
                    return bookingProformaInvoiceMainDTO;
                }).collect(Collectors.toSet());
                Set<BookingPackingListContainerPalletDTO> detailPalletSet = bookingPackingList.getBookingPackingListContainerPallet().parallelStream().map(item -> {
                    BookingPackingListContainerPalletDTO bookingPackingListContainerPalletDTO;
                    bookingPackingListContainerPalletDTO = CommonDataUtil.getModelMapper().map(item, BookingPackingListContainerPalletDTO.class);
                    return bookingPackingListContainerPalletDTO;
                }).collect(Collectors.toSet());
                if (request.getVendor().length() > 0 && !request.getVendor().equals(bookingPackingList.getSupplier())  && !request.getVendor().equals(booking[0].getConsolidator()) ) {
                    throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                }
                bookingPackingListDTO.setBookingPackingListContainerPallet(detailPalletSet);
                bookingPackingListDTO.setBookingPackingListDetailsDTO(detailSet);
                bookingPackingListDTO.setBookingProformaInvoiceMainDTO(detailPISet);
                bookingPackingListDTO.setConsolidator(booking[0].getConsolidator());
                List<Integer> ids = new ArrayList<>();
                ids.add(request.getId());
                bookingPackingListDTO.setBookingPackingListIds(ids);
                return bookingPackingListDTO;
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public BookingPackingListDTO getDetailsCreatedPackingList(ListIdDTO request) {
        try {
            Set<BookingPackingListDetailsDTO> detailNewSet = new HashSet<>();
            Set<BookingProformaInvoiceMainDTO> bookingProformaInvoiceSet = new HashSet<>();
            BookingPackingListDTO bookingPackingListNewDTO = new BookingPackingListDTO();
            Optional<User> oUser = userRepository.findOneByLogin(request.getUserId());
            String company = "";
            String fax = "";
            String address = "";
            String telephone = "";
            if (oUser.isPresent()) {
                User user = oUser.get();
                String vendorCode = user.getVendor();
                if (vendorCode == null) {
                    throw new BusinessException("Vendor code not define!");
                }

                Optional<Vendor> oVendor = vendorRepository.findByVendorCode(vendorCode);
                if (oVendor.isPresent()) {
                    Vendor vendor = oVendor.get();
                    company = vendor.getCompanyOwner();
                    address = vendor.getFactoryAddress();
                    telephone = vendor.getMainContactPhone();
                } else {
                    throw new BusinessException("Vendor code not exists!");
                }
            }

            StringBuilder invoiceNo = new StringBuilder();
            //get list packing list from multi proforma invoice
            //each row proforma invoice after import will create 1 packing list foreign key packing_list_id
            //So need get all detail that packing list
            for (Integer id : request.getId()) {
                Optional<BookingPackingList> data = bookingPackingListRepository.findById(id);
                if (data.isPresent()) {
                    BookingPackingList bookingPackingList = data.get();
                    if (request.getVendor().length() > 0 && !request.getVendor().equals(bookingPackingList.getSupplier())) {
                        throw new BusinessException(GlobalConstant.ERRORS_PERMISSION);
                    }
                    BeanUtils.copyProperties(bookingPackingList, bookingPackingListNewDTO);
                    if (invoiceNo.length() == 0) {
                        invoiceNo.append(bookingPackingList.getInvoice());
                    } else {
                        invoiceNo.append(" - " + bookingPackingList.getInvoice());
                    }
                    Set<BookingProformaInvoice> bookingProformaInvoice = bookingPackingList.getBookingProformaInvoice();
                    Set<BookingProformaInvoiceMainDTO> bookingProformaInvoiceDTO = bookingProformaInvoice.parallelStream().map(item -> {
                        BookingProformaInvoiceMainDTO bookingProformaInvoiceMainDTODTO;
                        bookingProformaInvoiceMainDTODTO = CommonDataUtil.getModelMapper().map(item, BookingProformaInvoiceMainDTO.class);
                        return bookingProformaInvoiceMainDTODTO;
                    }).collect(Collectors.toSet());
                    Set<BookingPackingListDetailsDTO> detailSet = bookingPackingList.getBookingPackingListDetail().parallelStream().map(item -> {
                        BookingPackingListDetailsDTO bookingPackingListDetailsDTO;
                        bookingPackingListDetailsDTO = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetailsDTO.class);
                        return bookingPackingListDetailsDTO;
                    }).collect(Collectors.toSet());
                    detailNewSet.addAll(detailSet);
                    bookingProformaInvoiceSet.addAll(bookingProformaInvoiceDTO);
                }
            }
            bookingPackingListNewDTO.setInvoice(invoiceNo.toString());
            bookingPackingListNewDTO.setId(null);
            bookingPackingListNewDTO.setBookingProformaInvoiceMainDTO(bookingProformaInvoiceSet);
            bookingPackingListNewDTO.setBookingPackingListDetailsDTO(detailNewSet);
            bookingPackingListNewDTO.setBookingPackingListIds(request.getId());
            bookingPackingListNewDTO.setFromCompany(company);
            bookingPackingListNewDTO.setFromAddress(address);
            bookingPackingListNewDTO.setFromTelephone(telephone);
            bookingPackingListNewDTO.setFromFax(fax);
            return bookingPackingListNewDTO;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

//    @Override
//    public BookingPackingListPageDTO getPackingListDetailsPageDTO(Integer id, Integer page, Integer limit) {
//        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "id");
//        Optional<BookingPackingList> data = bookingPackingListRepository.findById(id);
//        BookingPackingListPageDTO bookingPackingListPageDTO = new BookingPackingListPageDTO();
//        if (data.isPresent()) {
//            BookingPackingList bookingPackingList = data.get();
//            Page<BookingPackingListDetail> pageBookingPackingListDetail = bookingPackingListDetailRepository.findAllByBookingPackingList(bookingPackingList, pageable);
//            Page<BookingPackingListDetailsDTO> pageBookingPackingListDetailDTO = pageBookingPackingListDetail.map(this::convertToObjectBookingPackingListDetailDTO);
//            BeanUtils.copyProperties(bookingPackingList, bookingPackingListPageDTO);
//            bookingPackingListPageDTO.setBookingPackingListDetailsDTO(pageBookingPackingListDetailDTO);
//            return bookingPackingListPageDTO;
//        }
//        return null;
//    }

    private ResourceDTO convertToObjectResourceDTO(Object o) {
        ResourceDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, ResourceDTO.class);
        String path = dto.getPath().replace("[", "%5B").replace("]", "%5D");
        dto.setPath(path);
        return dto;
    }

    private BookingProformaInvoiceMainDTO convertToObjectBookingProformaInvoiceMainDTO(Object o) {
        BookingProformaInvoiceMainDTO dto;
        BookingProformaInvoice bookingProformaInvoice = (BookingProformaInvoice) o;
        dto = CommonDataUtil.getModelMapper().map(o, BookingProformaInvoiceMainDTO.class);
        if (bookingProformaInvoice.getBookingPackingList() != null) {
            Set<BookingPackingListDetailsDTO> bookingPackingListDetailsDTOSet;
            BookingPackingList bookingPackingList = bookingProformaInvoice.getBookingPackingList();
            BookingPackingListDTO bookingPackingListDTO = new BookingPackingListDTO();
            //get status and id packing list for font end call api
            dto.setBookingPackingListId(bookingPackingList.getId());
            dto.setBookingPackingListStatus(bookingPackingList.getStatus());
            Set<BookingPackingListDetail> bookingPackingListDetailSet = bookingPackingList.getBookingPackingListDetail();
            BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
            bookingPackingListDetailsDTOSet = bookingPackingListDetailSet.stream().map(item -> {
                BookingPackingListDetailsDTO bookingPackingListDetailsDTO = new BookingPackingListDetailsDTO();
                BeanUtils.copyProperties(item, bookingPackingListDetailsDTO);
                return bookingPackingListDetailsDTO;
            }).collect(Collectors.toSet());
            bookingPackingListDTO.setBookingPackingListDetailsDTO(bookingPackingListDetailsDTOSet);
            if (bookingPackingList.getCommercialInvoice() != null) {
                dto.setCommercialInvoiceId(bookingPackingList.getCommercialInvoice().getId());
            }
            dto.setBookingPackingListDTO(bookingPackingListDTO);
        }
        return dto;
    }

    private BookingPurchaseOrderDTO convertToObjectBookingPurchaseOrderDTO(Object o) {
        BookingPurchaseOrderDTO dto;
        dto = CommonDataUtil.getModelMapper().map(o, BookingPurchaseOrderDTO.class);
        return dto;
    }


    private <T> T mappingEntityToDto(Booking booking, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(booking, dto);
            if (dto instanceof BookingMainDTO) {
                String fromSoStr = bookingPurchaseOrderRepository.findAllFromSOByBookingId(booking.getId());
                Optional<User> oUser = userRepository.findOneByLogin(booking.getUpdatedBy());
                String updatedBy = "";
                if (oUser.isPresent()) {
                    updatedBy = oUser.get().getLastName() + " " + oUser.get().getFirstName();
                }
                clazz.getMethod("setPOAmazon", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setUpdatedBy", String.class).invoke(dto, updatedBy);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void exportPKLFromBOL(String filename, Integer id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFileDetailPKLFromBOL(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }

    @Override
    public void export(String filename, List<Integer> id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFileDetailPKL(id, workbook);
        generateExcelFileContainerPallet(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }

    private void writeHeaderLineContainerPallet(XSSFWorkbook workbook, XSSFSheet sheet) {
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
        createCell(sheet, row, 0, "CONTAINER/SEAL NUMBER", style);
        createCell(sheet, row, 1, "TOTAL PALLET QTY", style);

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
        createCell(sheet, row, 0, "PROFORMA INVOICE", style);
        createCell(sheet, row, 1, "AMAZON PO", style);
        createCell(sheet, row, 2, "SKU", style);
        createCell(sheet, row, 3, "ASIN", style);
        createCell(sheet, row, 4, "DESCRIPTION", style);
        createCell(sheet, row, 5, "QUANTITY", style);
        createCell(sheet, row, 6, "TOTAL OF EACH CARTON", style);
        createCell(sheet, row, 7, "TOTAL CARTON", style);
        createCell(sheet, row, 8, "N.W (KG)", style);
        createCell(sheet, row, 9, "G.W (KG)", style);
        createCell(sheet, row, 10, "MEASUREMENT/M3", style);
        createCell(sheet, row, 11, "CONTAINER/SEAL NUMBER", style);
    }

    private void writeHeaderLineDetailPKLFromBOL(XSSFWorkbook workbook, XSSFSheet sheet, BookingPackingList bookingPackingList, Vendor vendor) {
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
        createCellNoBorder(row, 9, bookingPackingList.getCommercialInvoice().getInvoiceNo(), styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 1, "Address: " + SOLD_TO_ADDRESS, styleField);
        createCellNoBorder(row, 8, "DATE: ", styleField);
        LocalDate localDate =  bookingPackingList.getCommercialInvoice().getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = localDate.format(formatter);
        createCellNoBorder(row, 9,formattedDate, styleField);
        rowCount++;
        row = sheet.createRow(rowCount);
        createCellNoBorder(row, 1, "Phone: " + SOLD_TO_TELEPHONE, styleField);
        createCellNoBorder(row, 8, "P.O No.: ", styleField);
        createCellNoBorder(row, 9, bookingPackingList.getCommercialInvoice().getCommercialInvoiceDetail().stream().map(CommercialInvoiceDetail::getFromSo).distinct().collect(Collectors.joining(",")), styleField);
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
        createCellNoAutoSize(row, columnCount++, "AMAZON PO", style);
        createCellNoAutoSize(row, columnCount++, "SKU", style);
        createCellNoAutoSize(row, columnCount++, "ASIN", style);
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

    public Workbook generateExcelFileDetailPKL(List<Integer> id, XSSFWorkbook workbook) {
        BookingPackingList bookingPackingList;
        Set<BookingPackingListDetail> detailSet = new HashSet<>();
        for (int item : id) {
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(item);
            if (oBookingPackingList.isPresent()) {
                bookingPackingList = oBookingPackingList.get();
                detailSet.addAll(bookingPackingList.getBookingPackingListDetail());
            } else {
                return null;
            }
        }
        XSSFSheet sheet = workbook.createSheet("Detail PKL");
        int rowCount = 1;
        writeHeaderLineDetailPKL(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        XSSFCellStyle styleFieldNumber3= workbook.createCellStyle();
        styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        for (BookingPackingListDetail detail : detailSet) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, detail.getProformaInvoiceNo(), style);
            createCell(sheet, row, columnCount++, detail.getPoNumber(), style);
            createCell(sheet, row, columnCount++, detail.getSku(), style);
            createCell(sheet, row, columnCount++, detail.getaSin(), style);
            createCell(sheet, row, columnCount++, detail.getTitle(), style);
            createCell(sheet, row, columnCount++, detail.getQuantity(), style);
            createCell(sheet, row, columnCount++, detail.getQtyEachCarton(), style);
            createCell(sheet, row, columnCount++, detail.getTotalCarton(), style);
            createCell(sheet, row, columnCount++, detail.getNetWeight(), styleFieldNumber3);
            createCell(sheet, row, columnCount++, detail.getGrossWeight(), styleFieldNumber3);
            createCell(sheet, row, columnCount++, detail.getCbm(), styleFieldNumber3);
            createCell(sheet, row, columnCount, detail.getContainer(), style);
        }
        return workbook;
    }

    public Workbook generateExcelFileDetailPKLFromBOL(Integer id, XSSFWorkbook workbook) {
        BookingPackingList bookingPackingList;
        Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(id);
        if (oBookingPackingList.isPresent()) {
            bookingPackingList = oBookingPackingList.get();
        } else {
            return null;
        }
        XSSFSheet sheet = workbook.createSheet(bookingPackingList.getInvoice());
        Optional<Vendor> oVendor = vendorRepository.findByVendorCode(bookingPackingList.getSupplier());
        Vendor vendor = new Vendor();
        if (oVendor.isPresent()) {
            vendor = oVendor.get();
        }
        writeHeaderLineDetailPKLFromBOL(workbook, sheet, bookingPackingList, vendor);
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
        Set<BookingPackingListContainerPallet> bookingPackingListContainerPallets = bookingPackingList.getBookingPackingListContainerPallet();
        List<BookingPackingListDetail> details = bookingPackingList.getBookingPackingListDetail()
            .stream().map(i -> {
                if (i.getContainer() == null) {
                    i.setContainer("");
                }
                return i;
            }).
            collect(Collectors.toList()).
            stream().
            sorted(Comparator.comparing(BookingPackingListDetail::getContainer).
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
        XSSFCellStyle styleFieldNumber3= workbook.createCellStyle();
        styleFieldNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));

        XSSFCellStyle styleFieldTotalNumber3= workbook.createCellStyle();
        styleFieldTotalNumber3.setFont(fontTotal);
        styleFieldTotalNumber3.setFillForegroundColor(myColor);
        styleFieldTotalNumber3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleFieldTotalNumber3.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        int rowMerge = 10;
        for (
            BookingPackingListDetail detail : details) {
            Row row = sheet.createRow(countRow);
            int columnCount = 0;
            Optional<Integer> oTotalPallet = bookingPackingListContainerPallets.stream().filter(k -> k.getContainer().equals(detail.getContainer())).map(BookingPackingListContainerPallet::getTotalPalletQty).findFirst();

            int pallet = 0;
            if (oTotalPallet.isPresent()) {
                pallet = oTotalPallet.get();
            }


            if (!previousContainer.equals(detail.getContainer()) && countRow > 10) {
                createCellNoAutoSize(row, columnCount++, "", styleTotal);
                createCellNoAutoSize(row, columnCount++, "", styleTotal);
                createCellNoAutoSize(row, columnCount++, "", styleTotal);
                createCellNoAutoSize(row, columnCount++, "", styleTotal);
                createCellNoAutoSize(row, columnCount++, "", styleTotal);
                createCellNoAutoSize(row, columnCount++, "TOTAL:", styleTotal);
                ExcelUtil.mergeCell(sheet, countRow, countRow, 0, 4);
                createCellNoAutoSize(row, columnCount++, totalCarton, styleTotal);
                createCellNoAutoSize(row, columnCount++, totalNetWeight, styleFieldNumber3);
                createCellNoAutoSize(row, columnCount++, totalGrossWeight, styleFieldNumber3);
                createCellNoAutoSize(row, columnCount, totalCBM, styleFieldNumber3);
                totalCarton = 0;
                totalNetWeight = 0;
                totalGrossWeight = 0;
                totalCBM = 0;
                if (rowMerge == 10) {
                    ExcelUtil.mergeCell(sheet, rowMerge, countRow, 10, 10);
                    ExcelUtil.mergeCell(sheet, rowMerge, countRow, 11, 11);
                } else {
                    ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 10, 10);
                    ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 11, 11);
                }
                rowMerge = countRow;
                countRow++;
                row = sheet.createRow(countRow);
                columnCount = 0;
            }
            createCellNoAutoSize(row, columnCount++, detail.getPoNumber(), style);
            createCellNoAutoSize(row, columnCount++, detail.getSku(), style);
            createCellNoAutoSize(row, columnCount++, detail.getaSin(), style);
            createCellNoAutoSize(row, columnCount++, detail.getTitle(), style);
            createCellNoAutoSize(row, columnCount++, detail.getQuantity(), style);
            createCellNoAutoSize(row, columnCount++, detail.getQtyEachCarton(), style);
            createCellNoAutoSize(row, columnCount++, detail.getTotalCarton(), style);
            createCellNoAutoSize(row, columnCount++, detail.getNetWeight(), styleFieldNumber3);
            createCellNoAutoSize(row, columnCount++, detail.getGrossWeight(), styleFieldNumber3);
            createCellNoAutoSize(row, columnCount++, detail.getCbm(), styleFieldNumber3);
            createCellNoAutoSize(row, columnCount++, pallet, styleCenter);
            createCellNoAutoSize(row, columnCount, detail.getContainer(), styleCenter);
            previousContainer = detail.getContainer();
            countRow++;
            totalCarton += detail.getTotalCarton();
            totalNetWeight += detail.getNetWeight();
            totalGrossWeight += detail.getGrossWeight();
            totalCBM += detail.getCbm();
        }

        Row row = sheet.createRow(countRow);
        int columnCount = 0;

        createCellNoAutoSize(row, columnCount++, "", styleTotal);

        createCellNoAutoSize(row, columnCount++, "", styleTotal);

        createCellNoAutoSize(row, columnCount++, "", styleTotal);

        createCellNoAutoSize(row, columnCount++, "", styleTotal);

        createCellNoAutoSize(row, columnCount++, "", styleTotal);

        createCellNoAutoSize(row, columnCount++, "TOTAL:", styleTotal);
        ExcelUtil.mergeCell(sheet, countRow, countRow, 0, 4);

        createCellNoAutoSize(row, columnCount++, totalCarton, styleTotal);

        createCellNoAutoSize(row, columnCount++, totalNetWeight, styleFieldTotalNumber3);

        createCellNoAutoSize(row, columnCount++, totalGrossWeight, styleFieldTotalNumber3);

        createCellNoAutoSize(row, columnCount, totalCBM, styleFieldTotalNumber3);
        if (rowMerge == 10) {
            ExcelUtil.mergeCell(sheet, rowMerge, countRow, 10, 10);
            ExcelUtil.mergeCell(sheet, rowMerge, countRow, 11, 11);
        } else {
            ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 10, 10);
            ExcelUtil.mergeCell(sheet, rowMerge + 1, countRow, 11, 11);
        }

        row = sheet.createRow(countRow + 2);

        createCell(sheet, row, 6, vendor.getVendorName(), style);

        return workbook;
    }

    public Workbook generateExcelFileContainerPallet(List<Integer> id, XSSFWorkbook workbook) {
        BookingPackingList bookingPackingList;
        Set<BookingPackingListContainerPallet> detailSet = new HashSet<>();
        for (int item : id) {
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(item);
            if (oBookingPackingList.isPresent()) {
                bookingPackingList = oBookingPackingList.get();
                detailSet.addAll(bookingPackingList.getBookingPackingListContainerPallet());
            } else {
                return null;
            }
        }
        XSSFSheet sheet = workbook.createSheet(CONTAINER_PALLET_SHEET);
        int rowCount = 1;
        writeHeaderLineContainerPallet(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        for (BookingPackingListContainerPallet detail : detailSet) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, detail.getContainer(), style);
            createCell(sheet, row, columnCount, detail.getTotalPalletQty(), style);
        }
        return workbook;
    }

    @Override
    public ResultUploadBookingDTO save(MultipartFile file, String userId) {
        ResultUploadBookingDTO resultUploadBookingDTO = new ResultUploadBookingDTO();
        try (
            CSVReader br = new CSVReader(new InputStreamReader(file.getInputStream()));
        ) {

            Booking booking = new Booking();
            Map<String, BookingProformaInvoice> mapPI = new HashMap<>();
            Map<String, BookingPackingList> mapPackingList = new HashMap<>();
            booking.setCreatedBy(userId);
            booking.setCreatedAt(new Date().toInstant());
            Map<String, String> listSO = new HashMap<>();
            Set<BookingPurchaseOrder> bookingPurchaseOrderSet = new HashSet<>();
            Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocationSet = new HashSet<>();
            Set<BookingProformaInvoice> bookingProformaInvoices = new HashSet<>();
            int row = 0;
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            mapper.registerModule(new JavaTimeModule());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            String[] nextLine;
            Set<String> listSupplier = new HashSet<>();
            while ((nextLine = br.readNext()) != null) {
                //check data if have data column from so then get data
                if (((nextLine[19] == null || nextLine[19].length() == 0) && row > 6)) {
                    break;
                }
                if (((nextLine[0] == null || nextLine[0].length() == 0) && row == 5)) {
                    row++;
                    continue;
                }
                BookingPurchaseOrder bookingPurchaseOrder = new BookingPurchaseOrder();
                for (int i = 0; i < nextLine.length; i++) {
                    if (nextLine[i] == null) {
                        nextLine[i] = "";
                    }
                    if (row == 1) {
                        switch (i) {
                            case 11:
                                if (nextLine[i].startsWith("Booking :")) {
                                    booking.setBookingConfirmation(nextLine[i].substring(9));
                                } else {
                                    booking.setBookingConfirmation("");
                                }
                                break;
                            case 12:
                                if (nextLine[i].startsWith("Master Booking:")) {
                                    booking.setInvoice(nextLine[i].substring(15).trim());
                                }
                                break;
                            case 20:
                                booking.setVendorCode(nextLine[i]);
                                break;
                            case 38:
                                booking.setPortOfLoading(nextLine[i]);
                                break;
                            case 39:
                                booking.setOriginEtd(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                break;
                            case 40:
                                booking.setFreightMode(nextLine[i]);
                                break;
                            case 41:
                                booking.setEstimatedDeliveryDate(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                break;
                            case 43:
                                booking.setDischargeEta(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                break;
                            case 46:
                                booking.setPortOfDischarge(nextLine[i]);
                                break;
                            case 42:
                                booking.setDestination(nextLine[i]);
                                break;
                            case 47:
                                booking.setFcrNo(nextLine[i]);
                                break;
                            case 48:
                                booking.setFreightTerms(nextLine[i]);
                                break;
                            case 49:
                                booking.setManufacturer(nextLine[i]);
                                break;
                            case 50:
                                booking.setStuffingLocation(nextLine[i]);
                                break;
                            case 51:
                                if (nextLine[i].startsWith("Container Type & QTY :")) {
                                    booking.setContainer(nextLine[i].substring(22).trim());
                                }
                                break;
                            case 52:
                                booking.setShipToLocation(nextLine[i]);
                                break;
                            default:
                        }
                    }
                    if (row == 4 || row == 5) {
                        switch (i) {
                            case 3:
                                bookingPurchaseOrder.setPoNumber(nextLine[i].trim());
                                break;
                            case 4:
                                bookingPurchaseOrder.setaSin(nextLine[i].trim());
                                break;
                            case 5:
                                if (booking.getPoDest() != null && booking.getPoDest().contains(nextLine[i])) {
                                    booking.setPoDest(booking.getPoDest() + "/" + nextLine[i]);
                                } else {
                                    booking.setPoDest(nextLine[i]);
                                }
                                break;
                            case 6:
                                bookingPurchaseOrder.setQuantity((int) Double.parseDouble(nextLine[i]));
                                break;
                            case 7:
                                bookingPurchaseOrder.setQuantityCtns(Double.parseDouble(nextLine[i]));
                                break;
                            case 11:
                                if (nextLine[i].startsWith("Ship To Location:")) {
                                    bookingPurchaseOrder.setShipLocation(nextLine[i].substring(17));
                                }
                                break;
                            default:
                        }
                    }
                    if (row > 7) {
                        switch (i) {
                            case 19:
                                bookingPurchaseOrder.setPoNumber(nextLine[i].trim());
                                break;
                            case 20:
                                bookingPurchaseOrder.setaSin(nextLine[i].trim());
                                break;
                            case 21:
                                if (booking.getPoDest() != null && booking.getPoDest().contains(nextLine[i])) {
                                    booking.setPoDest(booking.getPoDest() + "/" + nextLine[i]);
                                } else {
                                    booking.setPoDest(nextLine[i]);
                                }
                                break;
                            case 22:
                                bookingPurchaseOrder.setQuantity((int) Double.parseDouble(nextLine[i]));
                                break;
                            case 23:
                                bookingPurchaseOrder.setQuantityCtns(Double.parseDouble(nextLine[i]));
                                break;
                            case 27:
                                if (nextLine[i].startsWith("Ship To Location:")) {
                                    bookingPurchaseOrder.setShipLocation(nextLine[i].substring(17));
                                }
                                break;
                            default:
                        }
                    }

                }
                if (row == 4 || row == 5 || row > 7 && (bookingPurchaseOrder.getPoNumber() != null)) {
                    bookingPurchaseOrderSet.add(bookingPurchaseOrder);

                }
                row++;
            }
            Optional<Booking> oBooking = bookingRepository.findByBookingConfirmationAndStatusNot(booking.getBookingConfirmation(), 4);
            if (oBooking.isPresent()) {
                throw new BusinessException(String.format("The bookingConfirmation %s already exists in another Booking.", booking.getBookingConfirmation()));
            }
            if (!bookingPurchaseOrderSet.isEmpty()) {
                Set<BookingPurchaseOrder> finalBookingPurchaseOrderSet = bookingPurchaseOrderSet;
                bookingPurchaseOrderSet = bookingPurchaseOrderSet.stream().map(bookingPurchaseOrder -> {
                    List<ProformaInvoiceDetail> proformaInvoiceDetails = proformaInvoiceDetailRepository.findByFromSoAndIsDeletedAndAsinAndQtyGreaterThan(bookingPurchaseOrder.getPoNumber(), false, bookingPurchaseOrder.getaSin(), 0);
                    // get list ProformaInvoiceDetail with aSin, poNumber, sku because have much version
                    if (proformaInvoiceDetails.isEmpty()) {
                        throw new BusinessException(String.format("{Sku= %s ,Po Number= %s ,ASin= %s } not exists Proforma Invoice .", bookingPurchaseOrder.getSku(), bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getaSin()));
                    }
                    //get proforma invoice from detail with status not cancel
                    ProformaInvoice proformaInvoice = null;

                    Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceDetails.stream().map(ProformaInvoiceDetail::getProformaInvoice).findFirst();

                    // Can not find Proforma Invoice with key ASin ,Po Number or PI cancel
                    if (oProformaInvoice.isEmpty()) {
                        throw new BusinessException(String.format("{Sku= %s ,Po Number= %s ,ASin= %s } Can not find Proforma Invoice .", bookingPurchaseOrder.getSku(), bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getaSin()));
                    }
                    proformaInvoice = oProformaInvoice.get();
                    if (!proformaInvoice.getStatus().equals(GlobalConstant.STATUS_PI_CONFIRMED) && Boolean.FALSE.equals(proformaInvoice.getIsConfirmed())) {
                        throw new BusinessException(String.format("The status Proforma Invoice %s must be confirmed.", proformaInvoice.getOrderNo()));
                    }

                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoice.getProformaInvoiceDetail();
                    Long cdcVersionMax = proformaInvoiceDetailSet.stream().mapToLong(ProformaInvoiceDetail::getCdcVersion).max().orElseThrow(
                        () -> {
                            throw new BusinessException("Can not find new version detail proforma invoice");
                        }
                    );
                    // get detail proforma invoice latest Version
                    proformaInvoiceDetailSet = proformaInvoiceDetailSet.stream().filter(i -> Objects.equals(i.getCdcVersion(), cdcVersionMax)).collect(Collectors.toSet());
                    //set value to purchase order detail
                    ProformaInvoiceDetail proformaInvoiceDetail = proformaInvoiceDetailSet.stream().filter(i -> i.getQty() > 0 && i.getAsin().equals(bookingPurchaseOrder.getaSin()) && i.getFromSo().equals(bookingPurchaseOrder.getPoNumber())).findFirst().orElseThrow(() -> {
                        throw new BusinessException(String.format("{Po Number= %s ,ASin= %s   } not exists Proforma Invoice.", bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getaSin()));
                    });
                    //quantity import can not greater than quantity of PI
                    if (bookingPurchaseOrder.getQuantity() > proformaInvoiceDetail.getQty()) {
                        throw new BusinessException(String.format("{Po Number= %s ,ASin= %s ,Quantity PI= %s  } can not greater than quantity of Proforma Invoice.", bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getaSin(), proformaInvoiceDetail.getQty()));
                    }
                    //Default is  Select reason
                    bookingPurchaseOrder.setStatusDetail("");
                    bookingPurchaseOrder.setQuantityPrevious(proformaInvoiceDetail.getQty());
                    bookingPurchaseOrder.setQuantityCtnsPrevious(proformaInvoiceDetail.getTotalBox());
                    bookingPurchaseOrder.setFobPrice(proformaInvoiceDetail.getUnitPrice());
                    bookingPurchaseOrder.setCbm(proformaInvoiceDetail.getTotalBox());
                    bookingPurchaseOrder.setGrossWeight(proformaInvoiceDetail.getGrossWeight());
                    bookingPurchaseOrder.setSku(proformaInvoiceDetail.getSku());
                    bookingPurchaseOrder.setTitle(proformaInvoiceDetail.getProductName());
                    bookingPurchaseOrder.setUsCustomPrice(proformaInvoiceDetail.getUnitPrice());
                    // if quantity or ctn value in file import <> quantity and ctn in Proforma Invoice set status change for user choose reason
                    if (!bookingPurchaseOrder.getQuantity().equals(proformaInvoiceDetail.getQty())
                        || !bookingPurchaseOrder.getQuantityCtns().equals(proformaInvoiceDetail.getTotalBox())) {
                        bookingPurchaseOrder.setStatusDetail(GlobalConstant.STATUS_BOOKING_DETAIL_REASON_DEFAULT);
                    }
                    //add list PO key <OrderNO,Location>
                    listSO.put(proformaInvoiceDetail.getFromSo(), bookingPurchaseOrder.getShipLocation());

                    //if PI created booking then upload fail
                    List<BookingProformaInvoice> bookingProformaInvoiceList = bookingProformaInvoiceRepository.findAllByProformaInvoiceNo(proformaInvoice.getOrderNo());
                    if (!bookingProformaInvoiceList.isEmpty() && (bookingProformaInvoiceList.stream().anyMatch(i -> !Objects.equals(i.getBooking().getStatus(), STATUS_BOOKING_CANCEL)))) {
                        throw new BusinessException(String.format("Proforma Invoice %s created Booking", proformaInvoice.getOrderNo()));

                    }
                    bookingPurchaseOrder.setSupplier(proformaInvoice.getSupplier());
                    String proformaInvoiceNo = proformaInvoice.getOrderNo();

                    // create packing list
                    BookingPackingListDetail bookingPackingListDetail = new BookingPackingListDetail();
                    bookingPackingListDetail.setPoNumber(proformaInvoiceDetail.getFromSo());
                    bookingPackingListDetail.setProformaInvoiceNo(proformaInvoiceNo);
                    bookingPackingListDetail.setSku(proformaInvoiceDetail.getSku());
                    bookingPackingListDetail.setaSin(proformaInvoiceDetail.getAsin());
                    bookingPackingListDetail.setTitle(proformaInvoiceDetail.getProductName());
                    bookingPackingListDetail.setQuantity(bookingPurchaseOrder.getQuantity());
                    bookingPackingListDetail.setQuantityPrevious(bookingPurchaseOrder.getQuantity());
                    bookingPackingListDetail.setQtyEachCarton(proformaInvoiceDetail.getPcs());
                    bookingPackingListDetail.setQtyEachCartonPrevious(proformaInvoiceDetail.getPcs());
                    bookingPackingListDetail.setCbm(proformaInvoiceDetail.getTotalVolume());
                    bookingPackingListDetail.setCbmPrevious(proformaInvoiceDetail.getTotalVolume());
                    bookingPackingListDetail.setTotalCarton(proformaInvoiceDetail.getTotalBox());
                    bookingPackingListDetail.setTotalCartonPrevious(proformaInvoiceDetail.getTotalBox());
                    bookingPackingListDetail.setNetWeight(proformaInvoiceDetail.getNetWeight());
                    bookingPackingListDetail.setGrossWeight(proformaInvoiceDetail.getGrossWeight());
                    bookingPackingListDetail.setNetWeightPrevious(proformaInvoiceDetail.getNetWeight());
                    bookingPackingListDetail.setGrossWeightPrevious(proformaInvoiceDetail.getGrossWeight());
                    //get purchase order
                    PurchaseOrders purchaseOrders = proformaInvoiceDetail.getProformaInvoice().getPurchaseOrders();
                    String poNumber = purchaseOrders.getPoNumber();
                    // check line data of must in PI
                    long count = proformaInvoiceDetailSet.stream().filter(detail -> detail.getFromSo().equals(bookingPurchaseOrder.getPoNumber()) && detail.getAsin().equals(bookingPurchaseOrder.getaSin())).count();
                    if (count == 0) {
                        throw new BusinessException(String.format("{Po Number= %s ,ASin= %s  } not exists Proforma Invoice.", bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getaSin()));
                    }
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_BOOKING_CREATED);
                    purchaseOrders.setEta(booking.getDischargeEta());
                    purchaseOrders.setEtd(booking.getOriginEtd());
                    proformaInvoice.setStatus(GlobalConstant.STATUS_PI_CREATED);
                    listSupplier.add(proformaInvoice.getSupplier());
                    purchaseOrdersRepository.save(purchaseOrders);

                    //define booking packing list
                    if (mapPackingList.get(proformaInvoiceNo) == null) {
                        BookingPackingList bookingPackingList = new BookingPackingList();
                        bookingPackingList.setInvoice(proformaInvoiceNo);
                        bookingPackingList.setDate(LocalDate.now());
                        Set<BookingPackingListDetail> bookingPackingListDetails = new HashSet<>();
                        bookingPackingListDetails.add(bookingPackingListDetail);
                        bookingPackingList.setBookingPackingListDetail(bookingPackingListDetails);
                        bookingPackingList.setPoNumber(poNumber);
                        bookingPackingList.setStatus(GlobalConstant.STATUS_PKL_NEW);
                        bookingPackingList.setSoldToCompany(GlobalConstant.SOLD_TO_COMPANY);
                        bookingPackingList.setSoldToAddress(GlobalConstant.SOLD_TO_ADDRESS);
                        bookingPackingList.setSoldToTelephone(GlobalConstant.SOLD_TO_TELEPHONE);
                        bookingPackingList.setSoldToFax(GlobalConstant.SOLD_TO_FAX);
                        bookingPackingList.setSupplier(proformaInvoice.getSupplier());
                        mapPackingList.put(proformaInvoiceNo, bookingPackingList);
                    } else {
                        //add row to booking packing list detail
                        BookingPackingList bookingPackingList = mapPackingList.get(proformaInvoiceNo);
                        Set<BookingPackingListDetail> bookingPackingListDetails = bookingPackingList.getBookingPackingListDetail();
                        bookingPackingListDetails.add(bookingPackingListDetail);
                        bookingPackingList.setBookingPackingListDetail(bookingPackingListDetails);
                        bookingPackingList.setStatus(GlobalConstant.STATUS_PKL_NEW);
                        //save all distinct po number
                        String poNumberCurrent = bookingPackingList.getPoNumber() == null ? "" : bookingPackingList.getPoNumber();
                        if (!poNumberCurrent.contains(poNumber)) {
                            if (poNumberCurrent.length() == 0) {
                                poNumberCurrent = poNumber;
                            } else {
                                poNumberCurrent = poNumberCurrent + "/" + poNumber;
                            }
                            bookingPackingList.setPoNumber(poNumberCurrent);
                        }
                        bookingPackingList.setSupplier(proformaInvoice.getSupplier());
                        bookingPackingList.setSoldToCompany(GlobalConstant.SOLD_TO_COMPANY);
                        bookingPackingList.setSoldToAddress(GlobalConstant.SOLD_TO_ADDRESS);
                        bookingPackingList.setSoldToTelephone(GlobalConstant.SOLD_TO_TELEPHONE);
                        bookingPackingList.setSoldToFax(GlobalConstant.SOLD_TO_FAX);
                        mapPackingList.put(proformaInvoiceNo, bookingPackingList);
                    }
                    proformaInvoiceDetailSet.parallelStream().forEach(item -> {
                            long checkCount = finalBookingPurchaseOrderSet.parallelStream().filter(packingList -> packingList.getPoNumber().equals(item.getFromSo()) && packingList.getaSin().equals(item.getAsin())).count();
                            if (checkCount == 0 && item.getQty() > 0) {
                                throw new BusinessException(String.format("{Sku= %s ,Po Number= %s ,ASin= %s   } missing data.", item.getSku(), item.getFromSo(), item.getAsin()));
                            }
                        }
                    );
                    //add data booking proforma invoice
                    BookingProformaInvoice bookingProformaInvoice = new BookingProformaInvoice();
                    if (mapPI.get(proformaInvoiceNo) != null) {
                        bookingProformaInvoice = mapPI.get(proformaInvoiceNo);
                    }
                    bookingProformaInvoice.setCbm((bookingProformaInvoice.getCbm() == null ? 0.0 : bookingProformaInvoice.getCbm()) + proformaInvoiceDetail.getTotalVolume());
                    bookingProformaInvoice.setCtn((bookingProformaInvoice.getCtn() == null ? 0 : bookingProformaInvoice.getCtn()) + proformaInvoiceDetail.getTotalBox());
                    bookingProformaInvoice.setQuantity((bookingProformaInvoice.getQuantity() == null ? 0 : bookingProformaInvoice.getQuantity()) + Long.valueOf(proformaInvoiceDetail.getQty()));
                    bookingProformaInvoice.setShipDate(proformaInvoiceDetail.getShipDate());
                    bookingProformaInvoice.setProformaInvoiceNo(proformaInvoiceNo);
                    bookingProformaInvoice.setBookingPackingList(mapPackingList.get(proformaInvoiceNo));
                    // save str from so
                    String fromSo = bookingProformaInvoice.getPoAmazon() == null ? "" : bookingProformaInvoice.getPoAmazon();
                    if (!fromSo.contains(proformaInvoiceDetail.getFromSo())) {
                        if (fromSo.length() == 0) {
                            fromSo = proformaInvoiceDetail.getFromSo();
                        } else {
                            fromSo = fromSo + "," + proformaInvoiceDetail.getFromSo();
                        }
                        bookingProformaInvoice.setPoAmazon(fromSo);
                    }
                    //save supplier to each row detail for check permission user login
                    bookingProformaInvoice.setSupplier(proformaInvoice.getSupplier());
                    mapPI.put(proformaInvoiceNo, bookingProformaInvoice);

                    return bookingPurchaseOrder;
                }).collect(Collectors.toSet());
                if (!listSO.isEmpty()) {
                    for (Map.Entry<String, String> entry : listSO.entrySet()) {
                        BookingPurchaseOrderLocation bookingPurchaseOrderLocation = new BookingPurchaseOrderLocation();
                        bookingPurchaseOrderLocation.setPoNumber(entry.getKey());
                        bookingPurchaseOrderLocation.setShipLocation(entry.getValue());
                        bookingPurchaseOrderLocationSet.add(bookingPurchaseOrderLocation);
                    }
                }
                if (!mapPI.isEmpty()) {
                    for (Map.Entry<String, BookingProformaInvoice> entry : mapPI.entrySet()) {
                        BookingProformaInvoice bookingProformaInvoice = entry.getValue();
                        bookingProformaInvoices.add(bookingProformaInvoice);
                    }
                }
            }
            booking.setUpdatedBy(userId);
            booking.setUpdatedAt(new Date().toInstant());
            booking.setBookingProformaInvoice(bookingProformaInvoices);
            booking.setBookingPurchaseOrderLocation(bookingPurchaseOrderLocationSet);
            booking.setBookingPurchaseOrder(bookingPurchaseOrderSet);
            booking.setStatus(GlobalConstant.STATUS_BOOKING_UPLOAD);
            if (listSupplier.size() > 1) {
                booking.setType("Combined");
            } else {
                booking.setType("Normal");
            }
            bookingRepository.saveAndFlush(booking);
            resultUploadBookingDTO.setId(booking.getId());

            resultUploadBookingDTO.setMessage("");
            return resultUploadBookingDTO;

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }

    }

    @Override
    public void updateStatusResource(Booking booking) {
        if (!booking.getFreightTerms().equals("MPP")) {
            completedBooking(booking.getId());
        }
    }

}
