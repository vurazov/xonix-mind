package com.lineate.xonix.mind.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.lineate.xonix.mind.model.Status;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateMatchDto {

    @ApiModelProperty(notes = "The match ID")
    Integer id;

    @ApiModelProperty(notes = "The status of match", required = true)
    Status status;

    @ApiModelProperty(notes = "The percent when match is completed ")
    Double percent;

    @ApiModelProperty(notes = "The duration of match")
    Long duration;

    @ApiModelProperty(notes = "Time-date when match created")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime createdAt;

    @ApiModelProperty(notes = "The bots in the match")
    List<StateBotDto> bots;

}
