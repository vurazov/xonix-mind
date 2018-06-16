package com.lineate.xonix.mind.controller;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * It returns index as the name of the template, which Spring Boot’s autoconfigured view
 * resolver will map to src/main/resources/templates/*.
 */
@Slf4j
@Controller
@RequestMapping("/ui")
@Api(value = "match", description = "Operations pertaining to ui of Xonix Game Work", tags = "UI API")
public class ViewController {
    @GetMapping(value = "/match/create")
    public String createMatch() {
        return "createMatch";
    }

    @GetMapping(value = "/match/{id}")
    public String getBotsStatus() {
        return "botsStatus";
    }

    @GetMapping(value = "/match/add")
    public String addBot() {
        return "addBot";
    }

    @GetMapping(value = "/match/{id}/state")
    public String getResult() {
        return "result";
    }

    @GetMapping(value = "/match/states")
    public String getAllResults() {
        return "allResults";
    }

    @GetMapping(value = "/tournament/create")
    public String createTournament() {
        return "createTournament";
    }

    @GetMapping(value = "/tournament/{id}/state")
    public String getTournamentResults() {
        return "tournamentResults";
    }


}