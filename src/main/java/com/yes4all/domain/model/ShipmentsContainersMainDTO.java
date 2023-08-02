package com.yes4all.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsContainersMainDTO {


    private Integer id;

    private String containerNumber;

    private Double totalWeight;

    private Double totalVolume;

    private Integer totalItems;

    private Double totalAmount;

    private Integer status;

    private String type;

    private String whLocation;

    private String note;

    private LocalDateTime whDelivered;

    private LocalDateTime whRelease;

    private LocalDate whGateOut;
    private String unloadingType;
    private LocalDateTime etaWh;
    private LocalDateTime containerDischargeDate;
    private String gateOutPort;
    private String userId;
 }
