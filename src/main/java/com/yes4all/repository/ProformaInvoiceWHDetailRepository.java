package com.yes4all.repository;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceWH;
import com.yes4all.domain.ProformaInvoiceWHDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
public interface ProformaInvoiceWHDetailRepository extends JpaRepository<ProformaInvoiceWHDetail, Long> {
    Set<ProformaInvoiceWHDetail> findByIsDeletedAndProformaInvoiceWHOrderByCdcVersionDesc(Boolean isDeleted, ProformaInvoiceWH proformaInvoice);

    Optional<ProformaInvoiceWHDetail> findTop1CdcVersionByProformaInvoiceWHOrderByCdcVersionDesc(ProformaInvoiceWH proformaInvoice);


    @Query(value = " select pid.* from proforma_invoice_wh_detail pid \n" +
        "            inner join proforma_invoice_wh pi2 on pi2.id=pid.proforma_invoice_wh_id  \n" +
        "            where sku=:sku and pi2.status<>11 and( pid.qty >0 or (pid.qty=0 and (pi2.status<>2 or pi2.status<>3 ))) \n" +
        "            and cdc_version =(select max(cdc_version) from proforma_invoice_wh_detail de \n" +
        "            where de.proforma_invoice_wh_id=pid.proforma_invoice_wh_id) " +
        " and ( 0=:id or pi2.id<>:id)",nativeQuery = true)
    Optional<ProformaInvoiceWHDetail> findOneWithSkuNewVersion(@Param("sku") String sku,
                                                                @Param("id") Integer id);

}
