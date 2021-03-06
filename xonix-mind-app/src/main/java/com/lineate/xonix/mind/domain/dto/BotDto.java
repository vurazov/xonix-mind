package com.lineate.xonix.mind.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotDto {

    @ApiModelProperty(notes = "The bot id")
    Integer id;

    @ApiModelProperty(notes = "The bot name")
    String name;

    @ApiModelProperty(notes = "The bot url")
    String srcUrl;
}
