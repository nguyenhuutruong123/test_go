package com.yes4all.repository;


import com.yes4all.domain.Shipment;
import com.yes4all.domain.ShipmentsContQty;
import com.yes4all.domain.ShipmentsContainers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ShipmentsContainersRepository extends JpaRepository<ShipmentsContainers, Integer> {

}
