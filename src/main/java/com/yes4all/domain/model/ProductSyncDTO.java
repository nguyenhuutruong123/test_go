package com.yes4all.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSyncDTO {
    private Long id;
    private String product_sku;
    private String title;
    private String company;
    private String sell_type;
    private Integer group_id;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String life_cycle;
}
