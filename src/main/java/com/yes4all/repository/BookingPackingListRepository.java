package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingPackingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookingPackingListRepository extends JpaRepository<BookingPackingList, Integer> {

 }
