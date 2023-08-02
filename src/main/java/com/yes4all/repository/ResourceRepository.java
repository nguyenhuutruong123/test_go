package com.yes4all.repository;

import com.yes4all.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Photo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {
    List<Resource> findByFileTypeAndPackingListWhId(String fileType, Integer packingListWhId);

    List<Resource> findByFileTypeAndCommercialInvoiceWHId(String fileType, Integer commercialInvoiceWHId);
    List<Resource> findByFileTypeAndProformaInvoiceId(String fileType, Integer proformaInvoiceId);
    List<Resource> findByFileTypeAndCommercialInvoiceId(String fileType, Integer commercialInvoiceId);
    Page<Resource> findByFileTypeAndBookingId(String fileType, Integer bookingId, Pageable pageable);
    Page<Resource> findByFileTypeAndBillOfLadingIdAndModule(String fileType, Integer billOfLadingId,String module, Pageable pageable);
    List<Resource> findByFileTypeAndShipmentId(String fileType, Integer billOfLadingId);

    Optional<Resource> findByFileTypeAndNameAndBookingIdAndModule(String fileType,String name,Integer bookingId,String module);

    Optional<Resource> findByFileTypeAndModule(String fileType,String module);

}
