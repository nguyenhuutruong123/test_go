package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchBookingDTO extends BillOfLadingMainDTO {
    private List<String> listBookingNo;
    private Boolean isSearch;
    private Integer id;
}
