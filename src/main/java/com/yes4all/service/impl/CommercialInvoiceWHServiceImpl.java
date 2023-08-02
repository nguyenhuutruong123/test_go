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
import com.yes4all.service.CommercialInvoiceWHService;
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
public class CommercialInvoiceWHServiceImpl implements CommercialInvoiceWHService  {

    private final Logger log = LoggerFactory.getLogger(CommercialInvoiceWHServiceImpl.class);
     private static final DecimalFormat df = new DecimalFormat("0.00");

    @Autowired
    private CommercialInvoiceWHRepository commercialInvoiceWHRepository;


    @Autowired
    private CommercialInvoiceDetailWHRepository commercialInvoiceDetailWHRepository;
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


    public FileDTO exportExcelByIdCI(Integer id) {
        try {
            FileDTO fileDTO = new FileDTO();
            byte[] content = null;

            // get data CI
            Optional<CommercialInvoiceWH> optCommercialInvoice = commercialInvoiceWHRepository.findById(id);
            if (optCommercialInvoice.isPresent()) {
                // file name
                fileDTO.setFileName(optCommercialInvoice.get().getInvoiceNo() + ".xlsx");

                // header excel
                List<String> listHeader = getHeaderExcel();

                // detail excel

                Map<Long, List<Object>> mapData = getDetailExcel(optCommercialInvoice.get().getCommercialInvoiceWHDetail());
                Optional<Vendor> oVendor = vendorRepository.findByVendorCode(optCommercialInvoice.get().getSupplier());
                Vendor vendor;
                if (oVendor.isPresent()) {
                    vendor = oVendor.get();
                } else {
                    throw new BusinessException("Can not find vendor");
                }
                content = ExcelUtil.generateExcelFileWH(null, 0, listHeader, mapData,optCommercialInvoice.get(),vendor);
            } else {
                throw new BusinessException("Can not find Commercial Invoice");
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



    private List<String> getHeaderExcel() {
        List<String> listHeader = new ArrayList<>();
        listHeader.add("SKU");
        listHeader.add("ASIN");
        listHeader.add("DESCRIPTION");
        listHeader.add("QUANTITY");
        listHeader.add("UNIT PRICE (USD)");
        listHeader.add("AMOUNT");

        return listHeader;
    }

    private Map<Long, List<Object>> getDetailExcel(Set<CommercialInvoiceWHDetail> listCommercialInvoiceDetail) {
        // Detail excel
        Long row = 10L;
        Map<Long, List<Object>> mapData = new HashMap<>();

        for (CommercialInvoiceWHDetail detail : listCommercialInvoiceDetail) {
            List<Object> listData = new ArrayList<>();
            listData.add(CommonDataUtil.toEmpty(detail.getSku()));
            listData.add(CommonDataUtil.toEmpty(detail.getaSin()));
            listData.add(CommonDataUtil.toEmpty(detail.getProductTitle()));
            listData.add(CommonDataUtil.toZero(detail.getQty()));
            listData.add(CommonDataUtil.toZero(detail.getUnitPriceAllocated()));
            listData.add(CommonDataUtil.toZero(detail.getAmount()));
            mapData.put(row++, listData);
        }

        return mapData;
    }


}
