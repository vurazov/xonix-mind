package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.exception.BotNotFoundException;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotMatchRepository;
import com.lineate.xonix.mind.repositories.MatchRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
@ConfigurationProperties
public class MatchServiceImpl implements MatchService {

    final @NonNull
    MatchRepository matchRepository;

    final @NonNull
    BotService botService;

    @Value("${xonix.default.match.duration}")
    Long defaultMatchDuration;

    @Value("${xonix.default.field.filled}")
    Double defaultFieldFilled;

    @NonNull
    private final
    BotMatchRepository botMatchRepository;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository,
                            BotService botService,
                            BotMatchRepository botMatchRepository) {
        this.matchRepository = matchRepository;
        this.botService = botService;
        this.botMatchRepository = botMatchRepository;
    }

    Predicate<MatchDb> validator = match -> new DefaultMatchValidator().test(match);

    @Override
    public MatchDb create(MatchDb match) {
        if (!validator.test(match)){
            match.setPercent(defaultFieldFilled);
            match.setDuration(defaultMatchDuration);
        }

        match.setStatus(Status.New);
        match.setCreatedAt(LocalDateTime.now());
        return matchRepository.save(match);
    }

    @Override
    public Optional<MatchDb> getMatch(Integer matchId) {
        return matchRepository.findById(matchId);
    }

    @Override
    public BotDb addBot(MatchDb matchDb, BotDb botDb, BuildParam buildParam) {
        if (!matchDb.getStatus().equals(Status.New))
            throw new ServiceException("The bot:" + botDb.getId() + " could not be added into the match:" + matchDb);

        List<String> singleUrl = Collections.singletonList(botDb.getSrcUrl());
        BotMatchDb botMatchDb = new BotMatchDb(botDb, matchDb);
        try {
            List<Bot> bots = botService.getBots(singleUrl, buildParam);
            if (bots.isEmpty()) {
                throw new BotNotFoundException("The bot :" + botDb.getName() + " not found!");
            }
            log.info("The bot {} was build successfully!", botDb.getName());
            botMatchDb.setIsBuildSuccess(true);
        } catch (ServiceException e) {
            botMatchDb.setIsBuildSuccess(false);
            botMatchDb.setScore(0);
            log.info("The bot {} was build failed!", botDb.getName());
        }
        botDb.addBotMatch(botMatchDb);
        matchDb.addBotMatch(botMatchDb);
        botMatchDb.setVersion(botService.getLocalHeadHashByUrl(botDb.getSrcUrl()));
        botMatchRepository.save(botMatchDb);
        if (botMatchDb.getIsBuildSuccess()) {
            log.info("The bot {} version {} was added to match {} successfully!", botDb.getName(),
                    botMatchDb.getVersion(),  matchDb);
        } else {
            log.info("The bot {} version {} was failed to added to match {}", botDb.getName(),
                    botMatchDb.getVersion(), matchDb);
        }

        return botDb;
    }

    @Override
    public void save(MatchDb matchDb) {
        matchRepository.save(matchDb);
    }

    class DefaultMatchValidator implements Predicate<MatchDb> {
        @Override
        public boolean test(MatchDb match) {
            try {
                Objects.requireNonNull(match.getDuration());
                Objects.requireNonNull(match.getPercent());
            } catch (NullPointerException e) {
                return false;
            }
            return match.getDuration() > 0 && match.getPercent() > 0d;
        }
    }

    @Override
    public List<MatchDb> availableMatches() {
        return matchRepository.findAll();
    }

    @Override
    public List<BotMatchDb> getBotMatchDb(MatchDb matchDb) {
        return botMatchRepository.findByMatchId(matchDb.getId());
    }

    @Override
    public void save(List<BotMatchDb> botMatches) {
        botMatchRepository.saveAll(botMatches);
    }
}
