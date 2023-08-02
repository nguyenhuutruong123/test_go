package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.CommercialInvoiceWH;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommercialInvoiceWHRepository extends JpaRepository<CommercialInvoiceWH, Integer> {

}
