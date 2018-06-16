package com.lineate.xonix.mind;


import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotMatchRepository;
import com.lineate.xonix.mind.repositories.MatchRepository;
import com.lineate.xonix.mind.service.BotService;
import com.lineate.xonix.mind.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(classes = XonixApplication.class)
@RunWith(SpringRunner.class)
public class TestMatchService {

    @Autowired
    MatchService matchService;
    @Value("${xonix.default.match.duration}")
    Long defaultMatchDuration;
    @Value("${xonix.default.field.filled}")
    Double defaultFieldFilled;
    @Value("${xonix.test.user.url}")
    String testUrl;

    @MockBean
    MatchRepository matchRepository;
    @MockBean
    BotService botService;
    @MockBean
    BotMatchRepository botMatchRepository;


    @Before
    public void setUp() {
        when(botService.getBots(any(), any(BuildParam.class))).thenReturn(Arrays.asList(mock(Bot.class), mock(Bot.class)));
        when(botService.save(any(BotDb.class))).thenReturn(new BotDb());
    }

    @Test
    public void testCreate() {
        val matchDb = new MatchDb();
        matchDb.setPercent(90D);
        matchDb.setDuration(300L);
        when(matchRepository.save(matchDb)).thenReturn(matchDb);

        Assert.assertEquals(matchService.create(matchDb), matchDb);
        Assert.assertEquals(matchDb.getStatus(), Status.New);

        val wrongMatch = new MatchDb();
        wrongMatch.setPercent(-1D);
        wrongMatch.setDuration(-1L);
        when(matchRepository.save(wrongMatch)).thenReturn(wrongMatch);

        matchService.create(wrongMatch);
        Assert.assertEquals(wrongMatch.getStatus(), Status.New);
        Assert.assertEquals(wrongMatch.getPercent(), defaultFieldFilled);
        Assert.assertEquals(wrongMatch.getDuration(), defaultMatchDuration);
    }

    @Test(expected = ServiceException.class)
    public void testAddBot() {
        val matchDb = new MatchDb();
        matchDb.setPercent(90D);
        matchDb.setDuration(300L);
        matchDb.setStatus(Status.New);


        val botDb = new BotDb();
        botDb.setName("bot-name");
        botDb.setSrcUrl(testUrl);

        matchService.addBot(matchDb, botDb, new BuildParam(false));

        Assert.assertEquals(matchDb.getBots(), Arrays.asList(botDb));
        Assert.assertEquals(botDb.getMatches(), Arrays.asList(matchDb));
        matchDb.setStatus(Status.Finish);
        matchService.addBot(matchDb, botDb, new BuildParam(false));
    }
}
