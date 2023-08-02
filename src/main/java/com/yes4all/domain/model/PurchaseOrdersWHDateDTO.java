package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrdersWHDateDTO {
    private Instant updatedDate;

    private String updatedBy;

    private LocalDate dateBefore;

    private LocalDate dateAfter;
}
