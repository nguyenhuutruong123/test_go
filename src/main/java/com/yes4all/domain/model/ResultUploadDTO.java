package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.common.utils.ResultUploadPackingListDetail;
import com.yes4all.common.utils.UploadPurchaseOrder;
import com.yes4all.common.utils.UploadResultDetail;
import com.yes4all.common.utils.UploadPurchaseOrderSplitStatus;
import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.PurchaseOrdersSplitData;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultUploadDTO {
    private List<UploadPurchaseOrder> uploadPurchaseOrder;
    private List<UploadPurchaseOrderSplitStatus> uploadPurchaseOrderSplitStatus;
    private List<PurchaseOrdersSplit>   uploadPurchaseOrdersSplit;
    private List<UploadResultDetail> uploadResultDetail;
    private List<ResultUploadPackingListDetail> resultUploadPackingListDetail;
    @JsonIgnore
    private Map<String,List<PurchaseOrdersSplitData>> purchaseOrdersSplit;
    private String message;
    private PurchaseOrderDTO purchaseOrderDTO;
    private BookingPackingListDTO bookingPackingListDTO;
    private ShipmentsPackingListDTO shipmentsPackingListDTO;
    private Map<String, Set<ProformaInvoiceDetailDTO>> uploadProformaInvoiceDetail;
    private ProformaInvoiceDTO proformaInvoiceDTO;

    private CommercialInvoiceWHDTO  commercialInvoiceWHDTO;


    private ProformaInvoiceWHDTO  proformaInvoiceWHDTO;
}
