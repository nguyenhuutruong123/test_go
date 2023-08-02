package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyCloakAttributes {
    private String[] vendor;
    private String[] supplier;
    private String[] company;
    private String[] address;
    private String[] telephone;
    private String[] fax;
    private String[] isYes4all;
}
