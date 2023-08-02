package com.yes4all.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsPurchaseOrdersDetailDTO {

    private Integer id;

    private String sku;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double amount;

    private Double totalVolume;

    private Double grossWeight;

    private Double netWeight;

    private String containerType;

    private String containerNo;

}
