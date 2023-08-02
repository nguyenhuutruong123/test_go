package com.yes4all.domain.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPackingListMainDTO {
    List<ShipmentsPackingListDetailDTO> shipmentsPackingListDetail=new ArrayList<>();
    Set<ShipmentsContPalletDTO> shipmentsContPallet =new HashSet<>();
    CommercialInvoiceWHDTO commercialInvoiceWH;
    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;
    private Integer id;
 }
