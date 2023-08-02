package com.yes4all.repository;

import com.yes4all.domain.VendorCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorCountryRepository extends JpaRepository<VendorCountry, Long> {
    @Query("select p from VendorCountry p where (length(:vendorCode) =0 or p.vendorCode=:vendorCode ) ")
    Optional<VendorCountry> findByVendorCode(@Param("vendorCode")  String vendorCode);
}
