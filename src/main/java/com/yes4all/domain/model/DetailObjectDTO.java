package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailObjectDTO {
    private Integer id;
    private Integer page ;
    private Integer size;
    private String userId;
    private String reason;


}
