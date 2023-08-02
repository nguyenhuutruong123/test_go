package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.ProformaInvoice;
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
public interface CommercialInvoiceRepository extends JpaRepository<CommercialInvoice, Integer> {

    @Query(value = "SELECT p.* FROM commercial_invoice p " +
        " where " +
        " p.is_deleted=false and ( length(:fromSO) =0 or exists (select 1 from commercial_invoice_detail b" +
        " where p.id=b.commercial_invoice_id and upper(b.from_so) like '%'||:fromSO||'%') ) "+
        " and ( length(:invoiceNo) =0  or upper(p.invoice_no) like '%'||:invoiceNo||'%' ) "+
        " and ( upper(p.term) like '%'||:term||'%' or length(:term) =0) " +
        " and ( length(:shipDateFrom) =0 or p.ship_date >=TO_DATE(:shipDateFrom, 'yyyy-mm-dd')  ) " +
        " and ( length(:shipDateTo) =0 or p.ship_date <=TO_DATE(:shipDateTo, 'yyyy-mm-dd')  ) " +
        " and (cast(:status as integer) =-1 or p.status =cast(:status as integer) ) " +
        " and ( p.amount >=cast(:amountFrom as double precision) or cast(:amountFrom as double precision)=0) " +
        " and ( p.amount <=cast(:amountTo as double precision) or cast(:amountTo as double precision)=0) " +
        " and ( length(:updatedDateFrom) =0 or p.updated_date >=to_date(to_char(p.updated_date , '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:updatedDateTo) =0 or p.updated_date <=to_date(to_char(p.updated_date , '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  )  "+
        " and ( ( length(:supplier) =0 and p.status!=0 )  or ( upper(p.supplier)=:supplier) )"
        , nativeQuery = true)
    Page<CommercialInvoice> findByCondition(@Param("fromSO") String fromSO
        , @Param("invoiceNo") String invoiceNo, @Param("term") String term
        , @Param("shipDateFrom") String shipDateFrom, @Param("shipDateTo") String shipDateTo
        , @Param("amountFrom") String amountFrom, @Param("amountTo") String amountTo
        , @Param("status") String status, @Param("updatedDateFrom") String updatedDateFrom
        , @Param("updatedDateTo") String updatedDateTo
        , @Param("supplier") String supplier
        , Pageable pageable);

    @Query(value = "SELECT a.* from commercial_invoice a" +
        " inner join commercial_invoice_detail b on b.commercial_invoice_id=a.id" +
        " where b.proforma_invoice_id=:id  " +
        " and b.is_deleted=false and b.sku=:sku and b.from_so=:fromSo and b.purchase_order_no=:purchaseOrderNo  ", nativeQuery = true)
    List<CommercialInvoice> findAllDetailCIWithPI(@Param("id") Integer id,@Param("fromSo") String fromSo, @Param("sku") String sku, @Param("purchaseOrderNo") String purchaseOrderNo);

    @Query(value = "SELECT a.* from commercial_invoice a" +
        " inner join commercial_invoice_detail b on b.commercial_invoice_id=a.id" +
        " where b.proforma_invoice_id=:id  " +
        " and b.is_deleted=false and (0=:invoiceId or a.id!=:invoiceId )", nativeQuery = true)
    Optional<CommercialInvoice> findAllCIWithPI(@Param("id") Integer id,@Param("invoiceId") Integer invoiceId);
    Optional<CommercialInvoice> findByInvoiceNoAndIsDeleted(String invoiceNo,Boolean isDeleted);

    Optional<CommercialInvoice> findByBookingPackingListId(Integer bookingPackingListId);
}
