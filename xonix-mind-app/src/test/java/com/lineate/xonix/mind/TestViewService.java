package com.lineate.xonix.mind;


import com.google.gson.Gson;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.TournamentDb;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StartMatchDto;
import com.lineate.xonix.mind.domain.dto.StateBotDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.mapper.TypeMapper;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.PlayParam;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotMatchRepository;
import com.lineate.xonix.mind.repositories.BotRepository;
import com.lineate.xonix.mind.service.GamePlayService;
import com.lineate.xonix.mind.service.ViewService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(classes = XonixApplication.class)
@RunWith(SpringRunner.class)
public class TestViewService {

    @Autowired
    ViewService viewService;

    @Autowired
    TypeMapper mapper;
    @MockBean
    private BotRepository botRepository;
    @MockBean
    private BotMatchRepository botMatchRepository;
    @MockBean
    private GamePlayService gamePlayService;
    @MockBean
    private ExecutorService executorService;

    @Test
    public void testAddScores() {
        StateBotDto botState1 = StateBotDto.builder()
                .id(1)
                .name("name1")
                .srcUrl("url1")
                .build();
        StateBotDto botState2 = StateBotDto.builder()
                .id(2)
                .name("name2")
                .srcUrl("url2")
                .build();
        StateMatchDto stateMatch = StateMatchDto.builder()
                .id(1)
                .status(Status.Finish)
                .bots(Arrays.asList(botState1, botState2))
                .build();

        MatchDb matchDb = new MatchDb(1, Status.Finish, Lists.newArrayList(), new TournamentDb(),
                90L, 0.9D, LocalDateTime.now());
        BotDb bot1 = new BotDb(Arrays.asList(matchDb), "bot1", "test-url-1");
        bot1.setId(1);
        BotDb bot2 = new BotDb(Arrays.asList(matchDb), "bot2", "test-url-2");
        bot2.setId(2);

        BotMatchDb botMatch1 = new BotMatchDb(bot1, matchDb);
        botMatch1.setScore(5);
        botMatch1.setId(1);

        BotMatchDb botMatch2 = new BotMatchDb(bot2, matchDb);
        botMatch2.setScore(10);
        botMatch2.setId(2);

        matchDb.addBot(bot1);
        matchDb.addBot(bot2);

        when(botMatchRepository.findByMatchId(stateMatch.getId())).thenReturn(Arrays.asList(botMatch1, botMatch2));
        stateMatch = viewService.addScores(stateMatch);
        Assert.assertEquals(stateMatch.getBots(), Arrays.asList(botState1, botState2));
        Assert.assertEquals(botState1.getScore(), botMatch1.getScore());
        Assert.assertEquals(botState2.getScore(), botMatch2.getScore());
    }

    @Test
    public void testCreateMatch() {
        MatchDto matchDto = MatchDto.builder()
                .id(1)
                .percent(90D)
                .duration(1000L)
                .build();
        MatchDb matchDb = mapper.mapToMatch(matchDto);
        matchDb.setCreatedAt(LocalDateTime.now());
        when(gamePlayService.createMatch(any(MatchDb.class))).thenReturn(matchDb);
        Assert.assertEquals(viewService.createMatch(matchDto), mapper.mapToStateMatchDto(matchDb));
    }

    private MatchDb setMatchId(MatchDb matchDb, Integer id) {
        matchDb.setId(id);
        return matchDb;
    }

    @Test
    public void testStartMatch() {
        doAnswer((Answer) invocation -> {
            Runnable task = (Runnable) invocation.getArgument(0);
            task.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        StartMatchDto startMatchDto = new StartMatchDto(10, false, false);
        viewService.startMatch(1, startMatchDto);

        verify(gamePlayService, times(1)).addAllBotsToMatch(anyInt(), any(BuildParam.class));
        verify(gamePlayService, times(1)).startMatch(anyInt(), any(PlayParam.class));
    }

    @Test
    public void testGetStateOfMatch() {
        Integer matchId = 1;
        MatchDb matchDb = new MatchDb(Status.New, Collections.EMPTY_LIST, 300L, 90D);
        matchDb.setId(matchId);
        StateMatchDto stateMatch = mapper.mapToStateMatchDto(matchDb);
        when(gamePlayService.getMatch(matchId)).thenReturn(matchDb);
        Assert.assertEquals(stateMatch, viewService.getStateOfMatch(matchId));
    }

    @Test
    public void testGetMatchesByState() {
        MatchDb matchDb = new MatchDb();
        matchDb.setStatus(Status.Finish);
        matchDb.setId(1);
        ViewService spyViewService = spy(viewService);
        when(gamePlayService.getAllMatches()).thenReturn(Arrays.asList(matchDb));
        when(spyViewService.prepareMatchesForView(anyList())).thenReturn(new HashMap<>());
        Assert.assertNotNull(spyViewService.getMatchesByState(Status.Finish));
        verify(gamePlayService, times(1)).getAllMatches();
        verify(spyViewService, times(1)).prepareMatchesForView(anyList());
    }

    @Test
    public void testPrepareMatchesForView() {
        MatchDb match1 = new MatchDb(Status.Finish, Lists.newArrayList(), 400L, 85D);
        match1.setId(1);
        MatchDb match2 = new MatchDb(Status.Finish, Lists.newArrayList(), 500L, 90D);
        match2.setId(2);
        List<MatchDb> matches = Arrays.asList(match1, match2);
        BotDb bot1 = new BotDb(Arrays.asList(match1, match2), "bot1", "test-url-1");
        bot1.setId(1);
        BotDb bot2 = new BotDb(Arrays.asList(match1, match2), "bot2", "test-url-2");
        bot2.setId(2);
        List<BotDb> bots = Arrays.asList(bot1, bot2);
        BotMatchDb bot1Match1 = new BotMatchDb(bot1, match1);
        bot1Match1.setScore(300);
        bot1Match1.setVersion("11");
        BotMatchDb bot2Match1 = new BotMatchDb(bot2, match2);
        bot2Match1.setScore(400);
        bot2Match1.setVersion("21");
        BotMatchDb bot1Match2 = new BotMatchDb(bot1, match2);
        bot1Match2.setScore(500);
        bot1Match2.setVersion("12");
        BotMatchDb bot2Match2 = new BotMatchDb(bot2, match2);
        bot2Match2.setScore(600);
        bot2Match2.setVersion("22");
        bot2Match2.setIsBuildSuccess(false);

        bots.forEach(bot -> {
            match1.addBot(bot);
            match2.addBot(bot);
        });
        when(botMatchRepository.findByMatchId(1)).thenReturn(Arrays.asList(bot1Match1, bot2Match1));
        when(botMatchRepository.findByMatchId(2)).thenReturn(Arrays.asList(bot1Match2, bot2Match2));

        Map<String, List<List<String>>> result = new HashMap<>();
        result.put("headers", Arrays.asList(Arrays.asList("Created at", "bot1", "bot2", "Video")));
        result.put("rows", Arrays.asList(
                Arrays.asList(String.valueOf(match1.getCreatedAt()),
                        String.format("%s:%s:%s", bot1Match1.getVersion(), bot1Match1.getScore(),
                                bot1Match1.getIsBuildSuccess()),
                        String.format("%s:%s:%s", bot2Match1.getVersion(), bot2Match1.getScore(),
                                bot2Match1.getIsBuildSuccess()),
                        String.valueOf(match1.getId())),
                Arrays.asList(String.valueOf(match2.getCreatedAt()),
                        String.format("%s:%s:%s", bot1Match2.getVersion(), bot1Match2.getScore(),
                                bot1Match2.getIsBuildSuccess()),
                        String.format("%s:%s:%s", bot2Match2.getVersion(), bot2Match2.getScore(),
                                bot2Match2.getIsBuildSuccess()),
                        String.valueOf(match2.getId()))
        ));
        Gson gson = new Gson();
        Assert.assertEquals(gson.toJson(viewService.prepareMatchesForView(matches)), gson.toJson(result));
    }
}
