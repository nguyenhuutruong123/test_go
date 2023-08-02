package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDTO {
    private Integer id;
    private String path;
    private String module;
    private String name;
    private Long size;
    private String type;
    private String createdBy;
    private Instant uploadDate;
}
