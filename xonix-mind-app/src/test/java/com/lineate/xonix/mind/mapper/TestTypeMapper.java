package com.lineate.xonix.mind.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineate.xonix.mind.XonixApplication;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.StateBotDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.model.Status;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@SpringBootTest(classes = XonixApplication.class)
@RunWith(SpringRunner.class)
public class TestTypeMapper {

    TypeMapper mapper = new TypeMapper();

    @Test
    public void testEmptyMatchDto() {
        val stateMatchDto = StateMatchDto.builder()
                .id(-1)
                .duration(100l)
                .percent(.9)
                .status(Status.Work)
                .build();
        val matchDb = mapper.mapToMatch(stateMatchDto);
        Assert.assertEquals(matchDb.getId(), stateMatchDto.getId());
        Assert.assertEquals(matchDb.getDuration(), stateMatchDto.getDuration());
        Assert.assertEquals(matchDb.getPercent(), stateMatchDto.getPercent());
        Assert.assertEquals(matchDb.getStatus(), stateMatchDto.getStatus());
    }

    @Test
    public void testBotDtoWithOutMatch() {
        val botDto = BotDto.builder()
                .id(0)
                .srcUrl("https://global")
                .name("testBot1").build();
        val botDb = mapper.mapToBot(botDto);
        Assert.assertEquals(botDto.getSrcUrl(), botDb.getSrcUrl());
        Assert.assertEquals(botDb.getMatches().size(), 0);
    }

    @Test
    public void testBotDbToBotDto() {
        val botDb = new BotDb(Collections.emptyList(), "Bot1", "url1");
        val botDto = mapper.mapToBotDto(botDb);
        Assert.assertEquals(botDto.getSrcUrl(), botDb.getSrcUrl());
        Assert.assertEquals(botDto.getName(), botDb.getName());
    }

    @Test
    public void testMatchDtoWithBotDto() {
        val bots = new ArrayList<StateBotDto>() {{
            add(StateBotDto.builder()
                    .srcUrl("https://global")
                    .name("testBot1")
                    .score(-2).build());
            add(StateBotDto.builder()
                    .srcUrl("https://local")
                    .name("testBot0")
                    .score(-1).build());
        }};
        StateMatchDto stateMatchDto = StateMatchDto.builder()
                .id(-1)
                .status(Status.Work)
                .bots(bots)
                .duration(100L)
                .percent(.9d)
                .build();
        MatchDb matchDb = mapper.mapToMatch(stateMatchDto);
        List<BotDb> botDbsFromStates = mapper.toBotDdbList(stateMatchDto.getBots());
        matchDb.setBotMatches(Lists.newArrayList());
        botDbsFromStates.forEach(matchDb::addBot);
        Assert.assertEquals(matchDb.getId(), stateMatchDto.getId());
        Assert.assertEquals(matchDb.getDuration(), stateMatchDto.getDuration());
        Assert.assertEquals(matchDb.getPercent(), stateMatchDto.getPercent());
        Assert.assertEquals(matchDb.getStatus(), stateMatchDto.getStatus());
        Assert.assertNotNull(matchDb.getBots());
        Assert.assertEquals(matchDb.getBots().size(), 2);
        List<BotDb> botDbs = new ArrayList<>(matchDb.getBots());
        Assert.assertEquals(botDbs.get(0).getName(), bots.get(0).getName());
        Assert.assertEquals(botDbs.get(0).getSrcUrl(), bots.get(0).getSrcUrl());
    }

    @Test
    public void testMatchDbToMatchDto() throws Exception {
        MatchDb matchDb = new MatchDb(Status.New, Lists.newArrayList(), 10l, 0.9D);
        BotDb bot1 = new BotDb(Lists.newArrayList(), "Bot1", "url1");
        BotDb bot2 = new BotDb(Lists.newArrayList(), "Bot2", "url2");

        matchDb.addBot(bot1);
        matchDb.addBot(bot2);

        StateMatchDto stateMatchDto = mapper.mapToStateMatchDto(matchDb);

        Assert.assertNull(stateMatchDto.getId());
        Assert.assertEquals(stateMatchDto.getStatus(), matchDb.getStatus());
        Assert.assertEquals(stateMatchDto.getDuration(), matchDb.getDuration());
        Assert.assertEquals(stateMatchDto.getPercent(), matchDb.getPercent());
        Assert.assertEquals(stateMatchDto.getCreatedAt(), matchDb.getCreatedAt());
        Assert.assertNotNull(stateMatchDto.getBots());
        Assert.assertEquals(stateMatchDto.getBots().size(), 2);

        Assert.assertEquals(stateMatchDto.getBots().get(0).getName(), bot1.getName());
        Assert.assertEquals(stateMatchDto.getBots().get(0).getSrcUrl(), bot1.getSrcUrl());

        ObjectMapper jsonMapper = new ObjectMapper();
        String jsonMatchDto = jsonMapper.writeValueAsString(stateMatchDto);
        StateMatchDto pojoMatchDto = jsonMapper.readValue(jsonMatchDto, StateMatchDto.class);

        Assert.assertEquals(pojoMatchDto.getStatus(), matchDb.getStatus());
        Assert.assertEquals(pojoMatchDto.getDuration(), matchDb.getDuration());
        Assert.assertEquals(pojoMatchDto.getPercent(), matchDb.getPercent());
        Assert.assertEquals(pojoMatchDto.getCreatedAt(), matchDb.getCreatedAt());
        Assert.assertNotNull(pojoMatchDto.getBots());
        Assert.assertEquals(pojoMatchDto.getBots().size(), 2);

        Assert.assertEquals(pojoMatchDto.getBots().get(0).getName(), bot1.getName());
        Assert.assertEquals(pojoMatchDto.getBots().get(0).getSrcUrl(), bot1.getSrcUrl());
    }

}
