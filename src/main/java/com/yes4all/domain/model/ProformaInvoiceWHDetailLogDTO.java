package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.common.enums.EnumColumn;
import com.yes4all.common.enums.EnumUserType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProformaInvoiceWHDetailLogDTO {
    private Integer id;
    private String updatedNameBy;
    private String updatedBy;
    private Instant updatedDate;
    private Double valueAfter;
    private Double valueBefore;
    private EnumColumn columnChange;
    private EnumUserType userType;
    private Integer version;
}
