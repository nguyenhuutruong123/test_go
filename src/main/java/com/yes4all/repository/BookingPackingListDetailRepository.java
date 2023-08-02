package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingPackingList;
import com.yes4all.domain.BookingPackingListDetail;
import com.yes4all.domain.BookingProformaInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookingPackingListDetailRepository extends JpaRepository<BookingPackingListDetail, Integer> {

    Page<BookingPackingListDetail> findAllByBookingPackingList(BookingPackingList booking, Pageable pageable );

}
