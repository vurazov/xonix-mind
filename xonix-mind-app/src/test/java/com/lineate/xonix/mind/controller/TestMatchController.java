package com.lineate.xonix.mind.controller;

import com.lineate.xonix.mind.XonixApplication;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.model.Status;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.JUnitBDDSoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = XonixApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestMatchController {
    @LocalServerPort
    int port;

    @Rule
    public TestName name = new TestName();

    @Rule
    public final JUnitBDDSoftAssertions softly = new JUnitBDDSoftAssertions();

    @Value("${xonix.test.user.url}")
    String testUrl;
    private MatchApi api;


    @Test
    public void testMatchController() {
        MatchDto newMatchDto = MatchDto.builder()
                .id(-1)
                .duration(100L)
                .percent(0.9)
                .build();
        StateMatchDto savedMatchDto = api.createMatch(newMatchDto);
        softly.then(savedMatchDto)
                .isNotNull()
                .isEqualToIgnoringNullFields(StateMatchDto.builder()
                        .duration(newMatchDto.getDuration())
                        .percent(newMatchDto.getPercent())
                        .status(Status.New).build());
        softly.then(savedMatchDto.getId()).isNotNull().isEqualTo(1);

        BotDto botDto = BotDto.builder().name("Test").srcUrl(testUrl).build();
        BotDto newBot = api.addBot(botDto);
        botDto.setId(newBot.getId());
        softly.then(botDto).isEqualToIgnoringNullFields(newBot);
        BotDto botDto2 = BotDto.builder().name("Test2").srcUrl(testUrl + "2").build();
        softly.then(api.addBot(botDto2)).isEqualToIgnoringNullFields(botDto2);
        softly.then(api.addBot(botDto2)).isEqualToIgnoringNullFields(botDto2);
        List<BotDto> allBots = api.getAllBots();
        softly.then(botDto).isEqualToIgnoringNullFields(allBots.get(0));
        softly.then(botDto).isEqualToIgnoringNullFields(allBots.get(0));
        botDto2.setId(allBots.get(1).getId());
        softly.then(botDto2).isEqualToIgnoringNullFields(allBots.get(1));


        savedMatchDto = api.getStateOfMatch(savedMatchDto.getId());
        Assert.assertNotNull(savedMatchDto);
        Assert.assertNotNull(savedMatchDto.getBots());
        Assert.assertTrue(savedMatchDto.getBots().isEmpty());

        Map<String, List<List<String>>> response = new HashMap<>();
        response.put("headers", Arrays.asList(Arrays.asList("Created at", "Video")));
        response.put("rows", Collections.EMPTY_LIST);
        Assert.assertEquals(response, api.getFinishedMatches());
    }

    @Before
    public void apiSetup() {
        this.api = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logLevel(Logger.Level.BASIC)
                .target(MatchApi.class, "http://localhost:" + port);
    }

    @Headers("Accept: application/json")
    public interface MatchApi {

        @RequestLine("GET /match/{id}/state")
        StateMatchDto getStateOfMatch(@Param("id") Integer matchId);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /match/create")
        StateMatchDto createMatch(MatchDto matchDto);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /match/{id}/start")
        Response startMatch(@Param("id") Integer matchId);

        @Headers("Content-Type: application/json")
        @RequestLine("POST /match/add")
        BotDto addBot(BotDto botDto);

        @Headers("Content-Type: application/json")
        @RequestLine("GET /match/all-bots")
        List<BotDto> getAllBots();

        @Headers("Content-Type: application/json")
        @RequestLine("GET /match/list")
        Map<String, List<List<String>>> getFinishedMatches();

    }
}
