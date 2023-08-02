package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrdersMainSplitDTO {
    private Integer id;
    private String rootFile;
    private String updatedBy;
    private Integer status;
    private Instant updatedDate;
}
