package com.yes4all.domain.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsContainersDTO extends  ShipmentsContainersMainDTO {
    @JsonProperty("details")
    List<ShipmentsContainerDetailDTO> shipmentsContainersDetail=new ArrayList<>();
  }
