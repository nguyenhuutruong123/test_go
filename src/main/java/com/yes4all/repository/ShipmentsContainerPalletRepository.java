package com.yes4all.repository;


import com.yes4all.domain.ShipmentsContQty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShipmentsContainerPalletRepository extends JpaRepository<ShipmentsContQty, Integer> {

}
