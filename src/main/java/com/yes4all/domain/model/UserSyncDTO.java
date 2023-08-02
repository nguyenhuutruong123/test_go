package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSyncDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Double createdTimestamp ;
    private Double notBefore ;
    private boolean enabled ;
    private boolean totp ;
    private boolean emailVerified ;
    private String vendor;
    private KeyCloakAttributes attributes;

}
