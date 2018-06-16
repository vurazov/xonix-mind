package com.lineate.xonix.mind.domain.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentDto {

    @ApiModelProperty(notes = "The tournament id")
    Integer id;

    @ApiModelProperty(notes = "The percent when matches is completed")
    Double percent;

    @ApiModelProperty(notes = "The duration of matches")
    Long duration;

    @ApiModelProperty(notes = "The bots ids")
    List<Integer> bots;
}
