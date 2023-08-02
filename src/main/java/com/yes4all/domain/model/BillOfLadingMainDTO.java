package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillOfLadingMainDTO {
    private Integer id;
    private String billOfLadingNo;
    private Instant updatedAt;
    private String updatedBy;
    private List<ListBookingIds> listBookingIds;
}
