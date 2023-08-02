package com.yes4all.service;

import com.yes4all.common.enums.Tab;
import com.yes4all.common.enums.TabAction;
import com.yes4all.domain.CommercialInvoice;

import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import java.util.Set;


import java.util.Set;


/**
 * Service Interface for managing {@link CommercialInvoice}.
 */
public interface ShipmentService {

    Page<ShipmentMainDTO> listingWithCondition(Integer page, Integer limit, Map<String, String> filterParams);



    List<IShipmentPurchaseOrdersDTO> searchPO(ListSearchALLPOShipmentDTO request);

    List<IShipmentProformaInvoiceDTO> searchPI(ActionSingleIdDTO request);


    ShipmentsPurchaseOrdersMainDTO searchPODetail(ListSearchPOShipmentDTO request);

    ShipmentDTO createPKL(ListSearchPIShipmentDTO request);

    boolean confirmPKL(ActionSingleIdDTO request);

    boolean rejectPKL(ActionSingleIdDTO request);

    boolean confirmUSBroker(ActionSingleIdDTO request);

    boolean rejectUSBroker(ActionSingleIdDTO request);

    boolean actionInbound(RequestInboundDTO request);

    boolean sendRequest(ActionSingleIdDTO request);
    ShipmentsPackingListDTO updatePKL(ShipmentsPackingListDTO request);
    ShipmentsPackingListDTO generateCommercialInvoice(Integer id);

    boolean deleteCommercialInvoice(ActionSingleIdDTO request);



    List<ShipmentToIMSDTO> getAllShipment(InputGetShipmentDTO request);

    List<ShipmentContainerDetailToIMSDTO> getDetailShipmentContainer(InputGetContainerShipmentDTO request);
    boolean deleteShipment(ListIdDTO id);

    boolean deleteDetailPurchaseOrder(ListIdDTO id);
    boolean deletePackingList(ListIdDTO id);
    boolean sendPackingList(ActionSingleIdDTO id);
    String checkPermissionUser(String userId, TabAction action, Tab tab);
    ShipmentMainDTO getShipmentDetail(DetailObjectDTO request);
    boolean updateDetailContainer(InputUpdateContainerDTO request);
    ShipmentLocalBrokerDTO getLocalBrokerDetail(DetailObjectDTO request);

    ShipmentLocalBrokerDTO saveLocalBrokerDetail(ShipmentLocalBrokerDTO request);
    boolean  saveUSBrokerDetail(ActionSingleIdDTO request);
    USBrokerDTO getUSBrokerDetail(DetailObjectDTO request);
    ShipmentLogisticsInfoDTO saveLogisticsInfo(ShipmentLogisticsInfoDTO request);
    ShipmentLogisticsInfoDTO getLogisticsInfoDetail(DetailObjectDTO request);

    Set<ShipmentsQuantityDTO> getOrderQuantity(ActionSingleIdDTO request);

    Set<ShipmentsPackingListDTO> getAllPackingList(DetailObjectDTO request);
    ShipmentsPackingListDTO getPackingListDetail(DetailObjectDTO request);
    ShipmentsContainersDTO getContainersDetail(DetailObjectDTO request);
    Set<ShipmentsContainersMainDTO> getAllContainers(DetailObjectDTO request);
    ListFilterShipmentDTO getListFilter();
    ShipmentDTO createShipment(ShipmentDTO shipmentDTO) throws IOException, URISyntaxException;
    ShipmentDTO updateShipment(ShipmentDTO shipmentDTO) throws IOException, URISyntaxException;
    ShipmentsContainersDTO updateContainers(ShipmentsContainersDTO shipmentDTO) throws IOException, URISyntaxException;

    void export(String filename,ActionSingleIdDTO request) throws IOException;

    void exportCI(String filename,ActionSingleIdDTO request) throws IOException;

    FileDTO exportPKL(ActionSingleIdDTO request) throws IOException;

    FileDTO exportCI(ActionSingleIdDTO request) throws IOException;

    boolean updateDate(LogUpdateDateRequestDTO request);

    byte[]  exportMultiPKLCI(ListIdDTO request);


 }

