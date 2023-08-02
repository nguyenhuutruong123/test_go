package com.yes4all.service;

import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link com.yes4all.domain.PurchaseOrdersWH}.
 */
public interface PurchaseOrdersWHService {

    Page<PurchaseOrdersWHMainDTO> listingPurchaseOrdersWHWithCondition(Integer page, Integer limit, Map<String, String> filterParams);


    boolean sendPurchaseOrdersWH(List<Integer> purchaseOrderDetailId,String userName);


    ProformaInvoiceWHDTO createProformaInvoiceWH(Integer purchaseOrderId,String userName);

    boolean removePurchaseOrders(List<Integer> purchaseOrderDetailId, String userName);


    void export(String filename,Integer id) throws IOException;
    PurchaseOrderWHDetailPageDTO getPurchaseOrdersDetailWithFilter(BodyGetDetailDTO request);
    PurchaseOrderWHDetailPageDTO getPurchaseOrdersDetailWithFilterOrderNo( String oderNo,Integer page,Integer limit);

     ListDetailPOWHDTO getListSkuFromPO(List<Integer> purchaseOrderDetailId);



    boolean updateShipDate(LogUpdateDateRequestDTO request);


}
