package com.yes4all.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseOrdersShipWindowRequestDTO {
    List<String> listSO;
}
