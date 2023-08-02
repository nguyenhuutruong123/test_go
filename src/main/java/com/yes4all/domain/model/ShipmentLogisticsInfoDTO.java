package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentLogisticsInfoDTO  {

    private Integer shipmentId;

    private Integer id;

    private LocalDate cutOffSi;

    private String nameOfShip;

    private String oceanFreight;

    private String localBroker;

    private String usBroker;

    private String updatedBy;

    private Instant updatedDate;

    private String createdBy;

    private Instant createdDate;

    private LocalDate cutOffSy;

    private LocalDate releaseSo;
}
