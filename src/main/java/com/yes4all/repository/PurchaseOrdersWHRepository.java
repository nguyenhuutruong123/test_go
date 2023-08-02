package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.PurchaseOrdersWH;
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
public interface PurchaseOrdersWHRepository extends JpaRepository<PurchaseOrdersWH, Integer> {


    @Query(value = "SELECT p.* FROM purchase_orders_wh p where ( upper(p.po_number) like '%'||:poNumber||'%'  or :poNumber ='')" +
        " and ( p.created_by in (select id from jhi_user where upper(unaccent(concat(last_name,' ',first_name)))  like '%'||:updatedBy||'%') or length(:updatedBy) =0) " +
        " and ( upper(p.country) like '%'||:country||'%' or length(:country) =0) " +
        " and ( p.status =cast(:status as integer) or  cast(:status as integer) =-1) " +
        " and ( ( upper(p.vendor_id) =:supplier  and p.status!=0 ) or length(:supplier) =0) " +
        " and (  upper(p.vendor_id) =:supplierSearch or length(:supplierSearch) =0  )  " +
        " and ( length(:updatedDateFrom) =0 or to_date(to_char(p.updated_date , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "'), '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') >=to_date(:updatedDateFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:updatedDateTo) =0 or to_date(to_char(p.updated_date , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "'), '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') <=to_date(:updatedDateTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')   ) " +
        " and ( length(:expectedShipDateFrom) =0 or p.expected_ship_date >=to_date(:expectedShipDateFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')   ) " +
        " and ( length(:expectedShipDateTo) =0 or p.expected_ship_date <=to_date(:expectedShipDateTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:actualShipDateFrom) =0 or p.actual_ship_date >=to_date(:actualShipDateFrom , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') ) " +
        " and ( length(:actualShipDateTo) =0 or p.actual_ship_date <=to_date(:actualShipDateTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:etdFrom) =0 or p.etd >=to_date(:etdFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:etdTo) =0 or p.etd <=to_date(:etdTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:etaFrom) =0 or p.eta >=to_date(:etaFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:etaTo) =0 or p.eta <=to_date(:etaTo , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') ) " +
        " and ( length(:atdFrom) =0 or p.atd >=to_date(:atdFrom , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') ) " +
        " and ( length(:atdTo) =0 or p.atd <=to_date(:atdTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:ataFrom) =0 or p.ata >=to_date(:ataFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:ataTo) =0 or p.ata <=to_date(:ataTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and (   length(:shipmentId) =0   )  " +
        " and (  length(:orderedDateFrom) =0  or (length(:orderedDateFrom)>0 and p.ordered_date is not null  and to_date(to_char(p.ordered_date , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "'), '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') >=TO_DATE(:orderedDateFrom, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') ) ) " +
        " and (  length(:orderedDateTo) =0  or (length(:orderedDateFrom)>0 and p.ordered_date is not null   and to_date(to_char(p.ordered_date , '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "'), '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "') <=TO_DATE(:orderedDateTo, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')   ) )  "

        , nativeQuery = true)
    Page<PurchaseOrdersWH> findByCondition(@Param("poNumber") String poNumber, @Param("updatedBy") String updatedBy
        , @Param("country") String country
        , @Param("status") String status
        , @Param("supplier") String supplier
        , @Param("supplierSearch") String supplierSearch
        , @Param("updatedDateFrom") String updatedDateFrom, @Param("updatedDateTo") String updatedDateTo
        , @Param("expectedShipDateFrom") String expectedShipDateFrom, @Param("expectedShipDateTo") String expectedShipDateTo
        , @Param("actualShipDateFrom") String actualShipDateFrom, @Param("actualShipDateTo") String actualShipDateTo
        , @Param("etdFrom") String etdFrom, @Param("etdTo") String etdTo
        , @Param("etaFrom") String etaFrom, @Param("etaTo") String etaTo
        , @Param("atdFrom") String atdFrom, @Param("atdTo") String atdTo
        , @Param("ataFrom") String ataFrom, @Param("ataTo") String ataTo
        , @Param("shipmentId") String shipmentId
        , @Param("orderedDateFrom") String orderedDateFrom, @Param("orderedDateTo") String orderedDateTo
        , Pageable pageable);

    Optional<PurchaseOrdersWH> findAllByPoNumber(String poNumber);

    @Query(value = " select * from purchase_orders_wh p where p.id = :id and p.status != :status"
        , nativeQuery = true)
    Optional<PurchaseOrdersWH> findByIdAndActive(@Param("id") Integer id, @Param("status") Integer status);


}
