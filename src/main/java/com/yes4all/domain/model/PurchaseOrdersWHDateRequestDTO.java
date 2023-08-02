package com.yes4all.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrdersWHDateRequestDTO {
    private Integer id;

    private String date;

    private String userId;

    private String typeUpdateDate;
}
