package com.lineate.xonix.mind.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartMatchDto {
    @ApiModelProperty(notes = "Insert a delay in milliseconds between different steps during match")
    Integer delay;

    @ApiModelProperty(notes = "For debugging: skip git version check and maven build, just use cached jar")
    Boolean skipBuild;

    @ApiModelProperty(notes = "For debugging: skip creating video")
    Boolean skipVideo;
}
