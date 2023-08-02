package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;



@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentLocalBrokerDTO extends  ShipmentLocalBrokerMainDTO {
    private Set<ResourceDTO> resources=new HashSet<>();
}
