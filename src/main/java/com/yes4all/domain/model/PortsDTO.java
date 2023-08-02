package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortsDTO {
    private Integer id;

    private String title;

    private Integer canImport;

    private Integer canExport;

    private String country;

    private Integer published;
}
