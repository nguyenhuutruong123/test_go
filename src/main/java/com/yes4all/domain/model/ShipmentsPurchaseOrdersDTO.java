package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPurchaseOrdersDTO {
    private Integer id;
    private String purchaseOrderNo;
    private Integer purchaseOrderId;
    List<ShipmentsPurchaseOrdersDetailDTO> shipmentsPurchaseOrdersDetail=new ArrayList<>();
 }
