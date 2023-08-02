package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingMainDTO {
    private Integer id;
    private String bookingConfirmation;
    private String invoice;
    private String pOAmazon;
    private String type;
    private String consolidator;
    private String freightMode;
    private String updatedBy;
    private Instant updatedAt;
    private Integer status;

}
