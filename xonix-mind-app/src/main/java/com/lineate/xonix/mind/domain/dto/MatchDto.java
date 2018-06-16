package com.lineate.xonix.mind.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDto {

    @ApiModelProperty(notes = "The match id")
    Integer id;

    @ApiModelProperty(notes = "The percent when match is completed")
    Double percent;

    @ApiModelProperty(notes = "The duration of match")
    Long duration;

}
