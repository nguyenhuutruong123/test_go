package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingProformaInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookingProformaInvoiceRepository extends JpaRepository<BookingProformaInvoice, Integer> {
    @Query(value = "select b.* from booking_proforma_invoice b where b.booking_id=:bookingId and ( length(:supplier)=0 or b.supplier=:supplier) ",nativeQuery = true)
    Page<BookingProformaInvoice> findAllByBookingAndSupplierContainingIgnoreCase(@Param("bookingId") Integer bookingId,@Param("supplier") String supplier, Pageable pageable );

    List<BookingProformaInvoice> findAllByProformaInvoiceNo(String proformaInvoiceNo);

    List<BookingProformaInvoice> findByBookingIdIn(List<Integer> listId);

}
