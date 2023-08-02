package com.yes4all.service;

import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

/**
 * Service Interface for managing {@link PurchaseOrders}.
 */
public interface PurchaseOrdersSplitService {
    List<PurchaseOrdersSplit>  createPurchaseOrdersSplit(List<ResultUploadDTO> data, String user);
    void export(String filename,Integer id) throws IOException;
    Page<PurchaseOrdersMainSplitDTO> getAll(BodyListingDTO request);
    PurchaseOrdersSplit splitPurchaseOrder(Integer id);

    List<Integer> createPurchaseOrder(Integer id,String userId);
    boolean removePurchaseOrdersSplit(List<Integer> listPurchaseOrderId, String userName);
    PurchaseOrderDataPageDTO  getPurchaseOrdersSplitData(BodyListingDTO request);
    PurchaseOrderResultPageDTO getPurchaseOrdersSplitResult(BodyListingDTO request);
    String getNameFile(Integer id);
    PurchaseOrderSplitResultDetailsDTO getPurchaseOrdersSplitResultDetail(BodyListingDTO request);
}
