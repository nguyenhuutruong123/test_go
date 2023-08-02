package com.yes4all.repository;


import com.yes4all.domain.LogService;
import com.yes4all.domain.Ports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LogServiceRepository extends JpaRepository<LogService, Integer> {
}
