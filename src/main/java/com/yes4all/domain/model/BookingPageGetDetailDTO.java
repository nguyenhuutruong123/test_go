package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPageGetDetailDTO {
    @NotNull
    private Integer id;
    private Integer pageProduct ;
    private Integer sizeProduct ;
    private Integer pagePI ;
    private Integer sizePI ;
    private Integer pageResource ;
    private Integer sizeResource ;
    private String vendor;


}
