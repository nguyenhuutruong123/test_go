package com.yes4all.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogChangeFieldDTO {
    private Instant updatedDate;

    private String updatedBy;

    private String valueBefore;

    private String valueAfter;

    private String fieldValue;
}
