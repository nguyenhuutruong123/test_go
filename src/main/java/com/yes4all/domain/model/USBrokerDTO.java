package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class USBrokerDTO {
    private Integer status;
    private Set<ResourceDTO> resources;

}
