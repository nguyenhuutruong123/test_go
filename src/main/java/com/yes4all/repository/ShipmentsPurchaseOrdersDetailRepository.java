package com.yes4all.repository;

import com.yes4all.domain.ShipmentsPurchaseOrders;
import com.yes4all.domain.ShipmentsPurchaseOrdersDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ShipmentsPurchaseOrdersDetailRepository extends JpaRepository<ShipmentsPurchaseOrdersDetail, Integer> {

}
