package com.lineate.xonix.mind.domain.dto;


import com.lineate.xonix.mind.model.Status;
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
public class StateTournamentDto {
    @ApiModelProperty(notes = "The tournament id")
    Integer id;

    @ApiModelProperty(notes = "The status of tournament", required =true)
    Status status;

    @ApiModelProperty(notes = "The percent when matches is completed")
    Double percent;

    @ApiModelProperty(notes = "The duration of matches")
    Long duration;

    @ApiModelProperty(notes = "The matches in the tournament")
    List<StateMatchDto> matches;

    @ApiModelProperty(notes = "The bots in the tournament")
    List<BotDto> bots;

}
