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
public class StateBotDto {

    @ApiModelProperty(notes = "The id of bot")
    private Integer id;

    @ApiModelProperty(notes = "The name of bot", required =true)
    private String name;

    @ApiModelProperty(notes = "The url of bot")
    private String srcUrl;

    @ApiModelProperty(notes = "The score of bot")
    private Integer score;

    @ApiModelProperty(notes = "The flag of build")
    private Boolean isBuildSuccess;

    @ApiModelProperty(notes = "The version of bot")
    private String version;

}
