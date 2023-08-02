package com.yes4all.repository;

import com.yes4all.domain.SupplierCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierCountryRepository extends JpaRepository<SupplierCountry, Long> {
    @Query("select p from SupplierCountry p where   p.supplier=:supplier and   p.country=:country ")
    Optional<SupplierCountry> findBySupplierAndCountry(@Param("supplier")  String supplier,@Param("country")  String country);

    List<SupplierCountry> findBySupplier(@Param("supplier")  String supplier);
}
