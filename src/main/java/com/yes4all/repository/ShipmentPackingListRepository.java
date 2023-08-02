package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.Shipment;
import com.yes4all.domain.ShipmentsPackingList;
import com.yes4all.domain.ShipmentsPurchaseOrders;
import com.yes4all.service.IShipmentProformaInvoiceDTO;
import com.yes4all.service.IShipmentPurchaseOrdersDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ShipmentPackingListRepository extends JpaRepository<ShipmentsPackingList, Integer> {




}
