package com.yes4all.repository;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceDetail;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProformaInvoiceDetailRepository extends JpaRepository<ProformaInvoiceDetail, Long> {
    Set<ProformaInvoiceDetail> findByIsDeletedAndProformaInvoiceOrderByCdcVersionDesc(Boolean isDeleted, ProformaInvoice proformaInvoice);

   // @Query(value = "SELECT coalesce(max(cdc_version),0) max FROM proforma_invoice_detail p where  proforma_invoice_id=:id and is_deleted=true  ", nativeQuery = true)
    Optional<ProformaInvoiceDetail> findTop1CdcVersionByProformaInvoiceOrderByCdcVersionDesc(ProformaInvoice proformaInvoice);

    List<ProformaInvoiceDetail> findByFromSoAndIsDeletedAndAsinAndQtyGreaterThan(String fromSo, Boolean isDeleted, String asin,Integer qty);

    @Query(value = " select pid.* from proforma_invoice_detail pid \n" +
        "            inner join proforma_invoice pi2 on pi2.id=pid.proforma_invoice_id  \n" +
        "            where a_sin=:aSin and from_so =:fromSo and pi2.status<>11 and( pid.qty >0 or (pid.qty=0 and (pi2.status<>2 or pi2.status<>3 ))) \n" +
        "            and cdc_version =(select max(cdc_version) from proforma_invoice_detail de \n" +
        "            where de.proforma_invoice_id=pid.proforma_invoice_id) " +
        " and ( 0=:id or pi2.id<>:id)",nativeQuery = true)
    Optional<ProformaInvoiceDetail> findOneWithASinSoNewVersion(@Param("aSin") String aSin,
                                                                @Param("fromSo") String fromSo,
                                                                @Param("id") Integer id);

}
