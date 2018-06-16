package com.lineate.xonix.mind.controller;


import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/tournament")
@Api(value = "tournament", description = "Operations pertaining to tournament in Xonix Game Work", tags = "Tournament API")
public class TournamentController {

    private final ViewService viewService;

    @Autowired
    public TournamentController(ViewService viewService) {
        this.viewService = viewService;
    }

    @ApiOperation(value = "Create tournament.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created tournament"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(path = "/create", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity createTournament(
            @RequestBody TournamentDto tournamentDto) {
        return ResponseEntity.ok(viewService.createTournament(tournamentDto));
    }

    @ApiOperation(value = "Start tournament match.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully added math into queue"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(path = "/startmatch", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity startMatch(@RequestParam(name = "id") Integer tournamentId) {
        viewService.startTournamentMatch(tournamentId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Return list of finished matches by tournament id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of finished tournament matches"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(path = "/{id}/list")
    public Map<String, List<List<String>>> getTournamentFinishedMatches(
            @PathVariable(name = "id") Integer tournamentId) {
        return viewService.getTournamentMatches(tournamentId);
    }

}
