package com.yes4all.repository;


import com.yes4all.domain.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Integer> {
    Optional<WareHouse> findByWarehouseCode(String warehouseCode);
}
