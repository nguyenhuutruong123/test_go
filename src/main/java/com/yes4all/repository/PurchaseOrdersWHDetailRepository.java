package com.yes4all.repository;


import com.yes4all.domain.PurchaseOrdersWH;
import com.yes4all.domain.PurchaseOrdersWHDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseOrdersWHDetailRepository extends JpaRepository<PurchaseOrdersWHDetail , Long> {
    @Query("SELECT p FROM PurchaseOrdersWHDetail p where " +
        "   p.purchaseOrdersWH=:purchaseOrder ")
    List<PurchaseOrdersWHDetail> findByCondition(@Param("purchaseOrder") PurchaseOrdersWH purchaseOrdersWH);


 }
