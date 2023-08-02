package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.PurchaseOrders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseOrdersRepository extends JpaRepository<PurchaseOrders, Integer> {


    @Query(value = "SELECT p.* FROM purchase_orders p where p.is_deleted=false  and ( upper(p.po_number) like '%'||:poNumber||'%'  or :poNumber ='')" +
        " and ( p.id in (select  purchase_order_id from purchase_orders_detail where from_so  like '%'||:poAmazon||'%') or length(:poAmazon) =0) " +
        " and ( p.proforma_invoice_id in (select pi2.id  from booking b inner join booking_proforma_invoice bpi on b.id=bpi.booking_id" +
        "  inner join proforma_invoice pi2 on pi2.order_no =bpi.proforma_invoice_no where b.booking_confirmation like '%'||:bookingNumber||'%' ) or length(:bookingNumber) =0) " +
        " and ( p.created_by in (select id from jhi_user where upper(unaccent(concat(last_name,' ',first_name)))  like '%'||:updatedBy||'%') or length(:updatedBy) =0) " +
        " and ( upper(p.country) like '%'||:country||'%' or length(:country) =0) " +
        " and ( upper(p.fulfillment_center) like '%'||:fulfillmentCenter||'%' or length(:fulfillmentCenter) =0) " +
        " and ( p.status =cast(:status as integer) or  cast(:status as integer) =-1) " +
        " and ( ( upper(p.vendor_id) =:supplier  and p.status!=0 ) or length(:supplier) =0) " +
        " and (  upper(p.vendor_id) =:supplierSearch or length(:supplierSearch) =0  )  " +
        " and ( length(:updatedDateFrom) =0 or to_date(to_char(p.updated_date  AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') >=to_date(:updatedDateFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:updatedDateTo) =0 or to_date(to_char(p.updated_date  AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') <=to_date(:updatedDateTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')   ) " +
        " and ( length(:expectedShipDateFrom) =0 or p.expected_ship_date >=to_date(:expectedShipDateFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')   ) " +
        " and ( length(:expectedShipDateTo) =0 or p.expected_ship_date <=to_date(:expectedShipDateTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:actualShipDateFrom) =0 or p.actual_ship_date >=to_date(:actualShipDateFrom , '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') ) " +
        " and ( length(:actualShipDateTo) =0 or p.actual_ship_date <=to_date(:actualShipDateTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etdFrom) =0 or p.etd >=to_date(:etdFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etdTo) =0 or p.etd <=to_date(:etdTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etaFrom) =0 or p.eta >=to_date(:etaFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etaTo) =0 or p.eta <=to_date(:etaTo , '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') ) " +
        " and ( length(:atdFrom) =0 or p.atd >=to_date(:atdFrom , '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') ) " +
        " and ( length(:atdTo) =0 or p.atd <=to_date(:atdTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:ataFrom) =0 or p.ata >=to_date(:ataFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:ataTo) =0 or p.ata <=to_date(:ataTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:deadlineSubmitBookingFrom) =0 or p.ship_window_start >=to_date(:deadlineSubmitBookingFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:deadlineSubmitBookingTo) =0 or p.ship_window_start <=to_date(:deadlineSubmitBookingTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) "
        , nativeQuery = true)
    Page<PurchaseOrders> findByCondition(@Param("poNumber") String poNumber
        , @Param("poAmazon") String poAmazon
        , @Param("bookingNumber") String bookingNumber, @Param("updatedBy") String updatedBy
        , @Param("country") String country, @Param("fulfillmentCenter") String fulfillmentCenter
        , @Param("status") String status, @Param("supplier") String supplier
        , @Param("supplierSearch") String supplierSearch
        , @Param("updatedDateFrom") String updatedDateFrom, @Param("updatedDateTo") String updatedDateTo
        , @Param("expectedShipDateFrom") String expectedShipDateFrom, @Param("expectedShipDateTo") String expectedShipDateTo
        , @Param("actualShipDateFrom") String actualShipDateFrom, @Param("actualShipDateTo") String actualShipDateTo
        , @Param("etdFrom") String etdFrom, @Param("etdTo") String etdTo
        , @Param("etaFrom") String etaFrom, @Param("etaTo") String etaTo
        , @Param("atdFrom") String atdFrom, @Param("atdTo") String atdTo
        , @Param("ataFrom") String ataFrom, @Param("ataTo") String ataTo
        , @Param("deadlineSubmitBookingFrom") String deadlineSubmitBookingFrom, @Param("deadlineSubmitBookingTo") String deadlineSubmitBookingTo
        , Pageable pageable);

    Page<PurchaseOrders> findByIsDeleted(Boolean isDeleted, Pageable pageable);


    Optional<PurchaseOrders> findByPoNumberAndIsDeleted(String poNumber, Boolean isDeleted);

    List<PurchaseOrders> findAllByPoNumber(String poNumber);
    @Query(value = " select * from purchase_orders p where p.status!=13 and p.id in(select purchase_order_id from purchase_orders_detail where from_so in :listFromSo)", nativeQuery = true)
    List<PurchaseOrders> findAllByFromSo(@Param("listFromSo") List<String> listFromSo);

    @Query(value = " select count(1) as row from   proforma_invoice_detail a" +
        " inner join purchase_orders_detail b on b.purchase_order_id=a.purchase_order_id" +
        " and a.sku=b.sku and a.from_so=b.from_so and b.is_deleted=false " +
        " join LATERAL  (" +
        " select sum(qty) total_used from proforma_invoice_detail c where c.purchase_order_id=a.purchase_order_id" +
        "     and a.id<>c.id  and a.sku=c.sku and a.from_so=c.from_so and c.is_deleted=false  and a.purchase_order_no =c.purchase_order_no  " +
        " ) as total" +
        " on true" +
        " where a.proforma_invoice_id=:id and a.is_deleted=false " +
        " and (  b.qty_used>b.qty_ordered or total.total_used+a.qty>b.qty_used  )", nativeQuery = true)
    Integer countRowWrongData(@Param("id") Integer id);

    @Query(value = " select * from purchase_orders p where p.id = :id and p.status != :status"
        , nativeQuery = true)
    Optional<PurchaseOrders> findByIdAndActive(@Param("id") Integer id, @Param("status") Integer status);

    @Query(value = " select *" +
        " from purchase_orders po" +
        " where" +
        "     po.is_deleted = false" +
        "     and po.is_sendmail = false" +
        "     and po.status = :status" +
        "     and date_trunc('minute', po.created_date) <= date_trunc('minute', NOW() - INTERVAL '48 hours');"
        , nativeQuery = true)
    List<PurchaseOrders> findAllNewPO(@Param("status") Integer status);
}
