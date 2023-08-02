package com.yes4all.repository;


import com.yes4all.domain.ShipmentProformaInvoicePKL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ShipmentProformaInvoiceWHRepository extends JpaRepository<ShipmentProformaInvoicePKL, Integer> {

    Optional<ShipmentProformaInvoicePKL> findByProformaInvoiceId(Integer proformaInvoiceId);
}
