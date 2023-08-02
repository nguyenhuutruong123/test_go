package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceDetail;
import com.yes4all.domain.PurchaseOrders;
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
public interface ProformaInvoiceRepository extends JpaRepository<ProformaInvoice, Integer> {

    @Query(value = "SELECT p.* FROM proforma_invoice p " +
        " LEFT JOIN PURCHASE_ORDERS PO ON PO.proforma_invoice_id=p.id " +
        " where " +
        " p.is_deleted=false " +
        " and ( p.updated_by in (select id from jhi_user where upper(unaccent(concat(last_name,' ',first_name)))  like '%'||:updatedBy||'%') or length(:updatedBy) =0) " +
        " and ( length(:invoiceNoPI) =0  or upper(p.order_no) like '%'||:invoiceNoPI||'%' )" +
        " and ( length(:shipDatePIFrom) =0 or p.ship_date >=TO_DATE(:shipDatePIFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:shipDatePITo) =0 or p.ship_date <=TO_DATE(:shipDatePITo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( p.status =cast(:status as integer) or  cast(:status as integer) =-1) " +
        " and ( p.amount >=cast(:amountPIFrom as double precision) or cast(:amountPIFrom as double precision)=0) " +
        " and ( p.amount <=cast(:amountPITo as double precision) or cast(:amountPITo as double precision)=0) " +
        " and ( length(:createdDatePIFrom) =0 or to_date(to_char(p.created_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') >=TO_DATE(:createdDatePIFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:createdDatePITo) =0 or to_date(to_char(p.created_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') <=TO_DATE(:createdDatePITo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')   )  " +
        " and ( ( length(:supplier) =0 and p.status!=0 )  or ( upper(p.supplier)=:supplier) )"+
        " and ( ( length(:supplierSearch) =0  )  or ( upper(p.supplier)=:supplierSearch) )"+
        " and ( p.order_no in (select bpo.proforma_invoice_no from booking b inner join booking_proforma_invoice bpo on b.id=bpo.booking_id where b.booking_confirmation like '%'||:bookingNumber||'%' ) or length(:bookingNumber) =0) "
        , nativeQuery = true)
    Page<ProformaInvoice> findByCondition(@Param("invoiceNoPI") String invoiceNoPI
        , @Param("supplierSearch") String supplierSearch
        , @Param("bookingNumber") String bookingNumber
        , @Param("shipDatePIFrom") String shipDatePIFrom, @Param("shipDatePITo") String shipDatePITo
        , @Param("amountPIFrom") String amountPIFrom, @Param("amountPITo") String amountPITo
        , @Param("createdDatePIFrom") String createdDatePIFrom, @Param("createdDatePITo") String createdDatePITo
        , @Param("status") String status, @Param("updatedBy") String updatedBy
        , @Param("supplier") String supplier
        , Pageable pageable);

    List<ProformaInvoice> findByOrderNoIn(List<String> orderNo);

    Optional<ProformaInvoice> findByOrderNo(String orderNo);
}
