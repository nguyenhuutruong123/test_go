package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyCloakGroupDTO {

    private String id;
    private String name;
    private String path;
    private KeyCloakSubGroupDTO[] subGroups;

}
