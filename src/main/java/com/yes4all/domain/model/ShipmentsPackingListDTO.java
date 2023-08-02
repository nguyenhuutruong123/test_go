package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPackingListDTO extends  ShipmentsPackingListMainDTO {
    private Integer packingListId;
    private String invoice;
    private String userId;
    private Integer status;
 }
