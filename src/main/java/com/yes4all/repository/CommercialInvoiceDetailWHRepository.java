package com.yes4all.repository;

import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.CommercialInvoiceDetail;
import com.yes4all.domain.CommercialInvoiceWH;
import com.yes4all.domain.CommercialInvoiceWHDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommercialInvoiceDetailWHRepository extends JpaRepository<CommercialInvoiceWHDetail, Long> {

}
