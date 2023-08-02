package com.yes4all.repository;


import com.yes4all.domain.ShipmentsPurchaseOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;


import java.util.Optional;



@Repository
public interface ShipmentsPurchaseOrdersRepository extends JpaRepository<ShipmentsPurchaseOrders, Integer> {

    @Query(value = "select sd.* from shipment s inner join shipment_purchase_orders sd on s.id=sd.shipment_id " +
        "where sd.purchase_order_id=:purchaseOrderId and (:id=-1 or s.id<>:id)", nativeQuery = true)
    List<ShipmentsPurchaseOrders> findAllByPurchaseOrderIdAndId(@Param("purchaseOrderId") Integer purchaseOrderId, @Param("id") Integer id);

    Optional<ShipmentsPurchaseOrders> findOneByPurchaseOrderId(Integer purchaseOrderId);

}
