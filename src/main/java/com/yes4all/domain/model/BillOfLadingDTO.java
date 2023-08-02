package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillOfLadingDTO extends BillOfLadingMainDTO {
    private Double handlingCharge;
    private Double customsClearanceFree;
    private Double usCustom;
    private Double otherFee;
    private LocalDate atd;
    private LocalDate eta;
    private String brokerId;
    private Integer status;
    private String reason;
    @JsonProperty("details")
    private Set<BookingMainDTO> booking;
    @JsonProperty("invoice")
    @JsonIgnore
    private Page<ResourceDTO> invoice;
    @JsonProperty("duty")
    @JsonIgnore
    private Page<ResourceDTO> duty;
    @JsonProperty("other")
    @JsonIgnore
    private Page<ResourceDTO> other;
    @JsonIgnore
    @JsonProperty("bolAttachments")
    private Page<ResourceDTO> bolAttachments;
}
