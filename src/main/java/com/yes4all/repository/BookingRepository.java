package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersDetail;
import com.yes4all.domain.PurchaseOrdersSplit;
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
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query(value = "SELECT p.* FROM booking p where " +
        " ( upper(p.booking_confirmation) like '%'||:booking||'%' or :booking='') " +
        " and( upper(p.invoice) like '%'||:masterPO||'%' or :masterPO='') " +
        " and ( (p.id in (select booking_id from booking_purchase_order a where upper(a.po_number) like '%'||:poAmazon||'%' )) or :poAmazon='') " +
        " and (     length(:supplier) =0 or ((p.id in (select booking_id from booking_purchase_order a where upper(a.supplier) like '%'||:supplier||'%' )) and p.status<>-1 ))  "
        , nativeQuery = true)
    Page<Booking> findByCondition(@Param("booking") String booking,
                                  @Param("poAmazon") String poAmazon,
                                  @Param("masterPO") String masterPO,
                                  @Param("supplier") String supplier,
                                  Pageable pageable);

    @Query(value = "SELECT p.* FROM booking p where " +
        "  ( (p.id in (select booking_id from booking_purchase_order_location a where upper(a.po_number) like '%'||:poNumber||'%' )) or :poNumber='') "
        , nativeQuery = true)
    Optional<Booking> findByPONumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT p.* FROM booking p where " +
        "   (p.id in (select booking_id from booking_proforma_invoice a where a.id=:id)) and p.status!=2 "
        , nativeQuery = true)
    Optional<Booking> findByPIId(@Param("id") Integer id);

    Optional<Booking> findByBookingConfirmationAndStatusNot(String bookingConfirmation, Integer status);

    @Query(value = "SELECT p.* FROM booking p where  p.booking_confirmation in :listBookingNo"
        , nativeQuery = true)
    List<Booking> findAllByBookingNos(@Param("listBookingNo") List<String> listBookingNo);

    //get booking status confirmed
    @Query(value = "SELECT p.* FROM booking p where  (p.status=1 and p.freight_terms='MPP') or ( :id is not null and :id=p.bill_of_lading_id) " +
        " and ( p.booking_confirmation not in :listBookingNo or :listBookingNo is null)  "
        , nativeQuery = true)
    List<Booking> findAllByNotInBookingNos(@Param("listBookingNo") List<String> listBookingNo, @Param("id") Integer id);


    @Query(value = "SELECT p.* FROM booking p where exists (select booking_id from booking_proforma_invoice a where a.booking_packing_list_id =:packingListId and a.booking_id=p.id)"
        , nativeQuery = true)
    Optional<Booking> findAllByPackingListId(@Param("packingListId") Integer id);

}
