package com.yes4all.repository;

import com.yes4all.domain.ShipmentProformaInvoicePKL;
import com.yes4all.domain.ShipmentsPurchaseOrdersDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShipmentsProformaInvoicePKLRepository extends JpaRepository<ShipmentProformaInvoicePKL, Integer> {

}
