package com.lineate.xonix.mind.controller;


import com.lineate.xonix.mind.service.GameStateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@Slf4j
@Controller
@RequestMapping("/ui/match")
@Api(value = "match", description = "Operations pertaining to replay of matches in Xonix Game Work", tags = "Media API")
public class MediaController {

    private final GameStateService gameStateService;

    @Autowired
    public MediaController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @ApiOperation(value = "Return replay of match.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved replay of match"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/{id}/replay", produces = "video/webm")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getMatchReplay(
            @PathVariable(name = "id") Integer matchId) {
        final InputStream videoFileStream = gameStateService.retrieveMatchReplay(matchId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/webm"))
                .body(outputStream -> IOUtils.copy(videoFileStream, outputStream));
    }

    @ApiOperation(value = "Return log of bot assembling.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved log of bot assembling"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/{hash}/log", produces = "text/plain")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getBotMvnLog(
            @PathVariable(name = "hash") String commitHash) {
        final InputStream mvnLogStream = gameStateService.retrieveMvnBotLog(commitHash);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .body(outputStream -> IOUtils.copy(mvnLogStream, outputStream));
    }
}
