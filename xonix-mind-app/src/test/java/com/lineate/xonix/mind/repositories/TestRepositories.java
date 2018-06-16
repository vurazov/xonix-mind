package com.lineate.xonix.mind.repositories;


import com.lineate.xonix.mind.XonixApplication;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Slf4j
@DataJpaTest
@SpringBootTest(classes = XonixApplication.class)
@RunWith(SpringRunner.class)
public class TestRepositories {

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private BotMatchRepository botMatchRepository;

    @Before
    public void setUp(){
        matchRepository.deleteAll();
        botRepository.deleteAll();
    }

    @Test
    public void testCreateMatch() {
        MatchDb match = new MatchDb(Status.New, Lists.newArrayList(), 10l, 0.9D);

        //save match, verify has ID value after save
        assertNull(match.getId()); //null before save

        matchRepository.save(match);
        assertNotNull(match.getId()); //not null after save

        //fetch from DB
        assertThat(matchRepository.findAll()).extracting(MatchDb::getId).containsOnly(match.getId());
    }

    @Test
    public void testCreateMatchWithBots() {
        MatchDb match = new MatchDb(Status.New, Lists.newArrayList(), 10l, 0.9D);
        BotDb bot1 = new BotDb(Lists.newArrayList(match), "Bot1", "url1");
        BotDb bot2 = new BotDb(Lists.newArrayList(match), "Bot2", "url2");

        List<BotDb> bots = new ArrayList<>();
        bots.add(bot1);
        bots.add(bot2);

        bots.forEach(match::addBot);

        matchRepository.save(match);

        assertThat(matchRepository.findAll()).extracting(MatchDb::getId).containsOnly(match.getId());

        assertThat(botRepository.findAll()).extracting(BotDb::getName).contains(bot1.getName());
    }

    @Test
    public void testUpdateMatchWithBots() {
        MatchDb match = new MatchDb(Status.New, Lists.newArrayList(), 10l, 0.9D);
        BotDb bot1 = new BotDb(Lists.newArrayList(match), "Bot1", "url1");
        BotDb bot2 = new BotDb(Lists.newArrayList(match), "Bot2", "url2");
        List<BotDb> bots = new ArrayList<>();
        bots.add(bot1);
        bots.add(bot2);
        bots.forEach(match::addBot);

        matchRepository.save(match);

        BotDb bot3 = new BotDb(Lists.newArrayList(match), "Bot3", "url3");
        match.addBot(bot3);

        botRepository.save(bot3);

        assertThat(botRepository.findAll()).containsExactlyInAnyOrder(bot1, bot2, bot3);

        matchRepository.findById(match.getId()).ifPresent(m -> assertThat(m.getBots())
                .contains(bot1, bot2, bot3));
    }

    @Test
    public void testBotMatch() {
        MatchDb match = new MatchDb(Status.New, Lists.newArrayList(), 10l, 0.9D);
        BotDb bot1 = new BotDb(Lists.newArrayList(), "Bot1", "url1");
        match.setId(1);

        matchRepository.save(match);
        botRepository.save(bot1);

        assertThat(botMatchRepository.findAll()).isEmpty();

        match.addBot(bot1);
        botRepository.save(bot1);

        assertThat(botMatchRepository.findByMatchId(match.getId()))
                .extracting(BotMatchDb::getBotId)
                .contains(bot1.getId());
    }
}
