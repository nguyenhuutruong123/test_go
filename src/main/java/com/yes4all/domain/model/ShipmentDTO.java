package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


import com.yes4all.domain.ShipmentsContQty;


import com.yes4all.domain.ShipmentLogUpdateDate;
import com.yes4all.domain.ShipmentsContQty;
import com.yes4all.domain.ShipmentsPackingList;

import com.yes4all.domain.ShipmentsContQty;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


import java.util.HashSet;


import java.util.List;
import java.util.Set;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentDTO extends ShipmentMainDTO {
    @JsonProperty("details")
    private List<ShipmentsPurchaseOrdersDTO> shipmentsPurchaseOrders = new ArrayList<>();
    @JsonProperty("containers")
    private List<ShipmentsContQty> shipmentsContQty = new ArrayList<>();

    @JsonProperty("packingLists")
    private List<ShipmentsPackingListDTO> shipmentsPackingList = new ArrayList<>();
    @JsonProperty("orderedQuantity")
    private Set<ShipmentsQuantityDTO> shipmentsQuantity = new HashSet<>();
    @JsonProperty("etdLogUpdates")
    private List<LogUpdateDateDTO> etdLogUpdates= new ArrayList<>();

    @JsonProperty("contLogUpdates")
    private List<LogChangeFieldDTO> LogChangeFieldCont= new ArrayList<>();

    private String toWarehouse;
    private String toWarehouseName;

    private Integer statusUsBroker;

    private String address;
    private String consolidator;
    private Boolean isRequest;
}
