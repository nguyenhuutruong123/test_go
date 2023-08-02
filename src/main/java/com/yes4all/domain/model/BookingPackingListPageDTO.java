package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPackingListPageDTO {
    private Integer id;
    private String fromCompany;
    private String soldToCompany;
    private String fromAddress;
    private String soldToAddress;
    private String fromFax;
    private String soldToFax;
    private String invoice;
    private LocalDate date;
    private String poNumber;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private String soldToTelephone;
    private String fromTelephone;
    @JsonProperty("details")
    private Page<BookingPackingListDetailsDTO> bookingPackingListDetailsDTO;


}
