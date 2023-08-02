package com.yes4all.repository;


import com.yes4all.domain.Ports;
import com.yes4all.domain.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PortsRepository extends JpaRepository<Ports, Integer> {
}
