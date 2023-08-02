package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListFilterShipmentDTO {
    List<WareHouseDTO> wareHouseDTOs;
    List<PortsDTO> portsOfLoadings;
    List<PortsDTO> portsOfDischarges;
    Set<String> suppliers;
}
