package com.lineate.xonix.mind.controller;

import com.google.gson.Gson;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.GetAllBotsDto;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StartMatchDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.service.ViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/match")
@Api(value = "match", description = "Operations pertaining to match in Xonix Game Work", tags = "Match API")
public class MatchController {

    private final ViewService viewService;
    private final Gson gson;

    @Autowired
    public MatchController(ViewService viewService, Gson gson) {
        this.viewService = viewService;
        this.gson = gson;
    }

    @ApiOperation(value = "Create match")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Match is added successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(path = "/create", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String createMatch(@RequestBody MatchDto matchDto) {

        return gson.toJson(viewService.createMatch(matchDto));
    }

    @ApiOperation(value = "Start match")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Match is started successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(path = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity startMatch(
            @PathVariable(name = "id") Integer matchId,
            @RequestBody StartMatchDto startMatchDto
    ) {
        viewService.startMatch(matchId, startMatchDto);
        return ResponseEntity.ok("Match " + matchId + " is started successfully");
    }

    @ApiOperation(value = "Return list of all bots.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of all bots"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/all-bots", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GetAllBotsDto> getAllBots() {
        List<GetAllBotsDto> botDtos = viewService.getAllBotDto();
        log.info("getAllBots() : {}", botDtos);
        return botDtos;
    }

    @ApiOperation(value = "Return state of match")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved state of match"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/{id}/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getStateOfMatch(@PathVariable(name = "id") Integer matchId) {
        StateMatchDto stateMatchDto = viewService.getStateOfMatch(matchId);
        log.info("getStateOfMatch({}) : {}", matchId, stateMatchDto);
        return gson.toJson(stateMatchDto);
    }

    @ApiOperation(value = "Add bot.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Bot is added successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(path = "/add", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public BotDto addBot(@RequestBody BotDto botDto) {
        BotDto responseBot = viewService.saveBot(botDto);
        log.info("addBot({}) : {}", responseBot.getName(), responseBot.getSrcUrl());
        return responseBot;
    }

    @ApiOperation(value = "View a list of finished matches")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of matches"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<List<String>>> getFinishedMatches() {
        return viewService.getMatchesByState(Status.Finish);
    }
}
