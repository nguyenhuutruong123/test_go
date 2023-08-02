package com.yes4all.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPurchaseOrdersMainDTO    {
    private Set<String> consolidatorList;
    List<ShipmentsContQtyDTO> shipmentsContQtyDTO=new ArrayList<>();
    List<ShipmentsPurchaseOrdersDTO> shipmentsPurchaseOrdersDTOList=new ArrayList<>();
 }
