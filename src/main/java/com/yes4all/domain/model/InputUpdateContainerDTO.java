package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputUpdateContainerDTO {
    @NotNull
    @JsonProperty("container_id")
    private Integer containerId;
    @NotNull
    @JsonProperty("details")
    List<InputUpdateDetailContainerDTO> inputUpdateDetailContainerDTOList;

}
