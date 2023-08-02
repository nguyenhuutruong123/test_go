package com.yes4all.repository;

import com.yes4all.domain.PurchaseOrders;
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
public interface PurchaseOrdersSplitRepository extends JpaRepository<PurchaseOrdersSplit, Integer> {
   @Query("select p from PurchaseOrdersSplit p where p.status!=3")
    Page<PurchaseOrdersSplit> findAll(Pageable pageable);
    @Query("select p from PurchaseOrdersSplit p where p.rootFile=:filename and p.status!=3")
    Optional<PurchaseOrdersSplit> findByRootFile(@Param("filename") String filename);

 }
