package com.yes4all.repository;

import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseOrdersDetailRepository extends JpaRepository<PurchaseOrdersDetail, Long> {
    Page<PurchaseOrdersDetail> findByIsDeletedAndPurchaseOrders(Boolean isDeleted, PurchaseOrders purchaseOrders, Pageable pageable);

    @Query("SELECT p FROM PurchaseOrdersDetail p where " +
        "  isDeleted=false and p.purchaseOrders=:purchaseOrder ")
    List<PurchaseOrdersDetail> findByCondition(@Param("purchaseOrder") PurchaseOrders purchaseOrder);


    @Query(value = "SELECT coalesce(max(cdc_version),0) max FROM purchase_orders_detail p where  purchase_order_id=:id and is_deleted=true  ", nativeQuery = true)
    Long findMaxCdcVersion(@Param("id") Integer id);

    Optional<PurchaseOrdersDetail> findByFromSoAndQtyAndPcsAndIsDeletedAndAsin(String fromSo, Long qty, Integer pcs, Boolean isDeleted, String asin);
    Optional<PurchaseOrdersDetail> findByFromSoAndAsinAndIsDeleted(String fromSo, String asin, Boolean isDeleted);



}
