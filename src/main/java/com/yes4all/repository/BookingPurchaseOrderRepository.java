package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.BookingPurchaseOrder;
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
public interface BookingPurchaseOrderRepository extends JpaRepository<BookingPurchaseOrder, Integer> {
    List<BookingPurchaseOrder> findAllByBookingAndSupplierContainingIgnoreCase(Booking booking , String supplier);

    @Query(value = "SELECT string_agg( distinct p.po_number,', ') as po_number " +
        " FROM booking_purchase_order p where  booking_id=:id ", nativeQuery = true)
    String findAllFromSOByBookingId(@Param("id") Integer id);
}
