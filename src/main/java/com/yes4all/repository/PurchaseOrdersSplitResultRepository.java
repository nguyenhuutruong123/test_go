package com.yes4all.repository;

import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.PurchaseOrdersSplitData;
import com.yes4all.domain.PurchaseOrdersSplitResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseOrdersSplitResultRepository extends JpaRepository<PurchaseOrdersSplitResult, Integer> {

    @Query(value = "SELECT coalesce(max(number_order_no),0) max " +
        " FROM split_purchase_order_result p where  vendor=:vendor", nativeQuery = true)
    Long findMaxNumberOrderNo(@Param("vendor") String vendor);


    Page<PurchaseOrdersSplitResult> findByPurchaseOrdersSplit(PurchaseOrdersSplit purchaseOrdersSplit,Pageable pageable);

}
