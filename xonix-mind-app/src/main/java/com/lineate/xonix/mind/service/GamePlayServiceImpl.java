package com.lineate.xonix.mind.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.exception.BotNotFoundException;
import com.lineate.xonix.mind.exception.GamePlayException;
import com.lineate.xonix.mind.exception.MatchNotFoundException;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.Gameplay;
import com.lineate.xonix.mind.model.MatchLogger;
import com.lineate.xonix.mind.model.ModelGameState;
import com.lineate.xonix.mind.model.PlayParam;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotRepository;
import com.lineate.xonix.mind.utils.ServiceUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Slf4j
@Service
@ConfigurationProperties
public class GamePlayServiceImpl implements GamePlayService {

    private static final Logger matchLog = LoggerFactory.getLogger("match-logger");

    @Value("${xonix.default.field.height}")
    Integer defaultFieldHeight;

    @Value("${xonix.default.field.width}")
    Integer defaultFieldWidth;

    @Value("${xonix.default.log.video.dir}")
    String logVideoDir;

    @NonNull
    private final BotService botService;

    @NonNull
    private final MatchService matchService;

    @NonNull
    private final GameStateService gameStateService;

    @NotNull
    private Gameplay gameplay;

    @NonNull
    private final
    TournamentService tournamentService;

    @NonNull
    private final
    BotRepository botRepository;

    @Autowired
    public GamePlayServiceImpl(BotService botService,
                               MatchService matchService,
                               GameStateService gameStateService,
                               TournamentService tournamentService,
                               BotRepository botRepository,
                               Gameplay gameplay
                               ) {
        this.botService = botService;
        this.matchService = matchService;
        this.gameStateService = gameStateService;
        this.tournamentService = tournamentService;
        this.gameplay = gameplay;
        this.botRepository = botRepository;
    }

    @Override
    public List<MatchDb> getAllMatches() {
        return matchService.availableMatches();
    }

    @Override
    public MatchDb createMatch(MatchDb match) {
        return matchService.create(match);
    }

    @Override
    public StateTournamentDto createTournament(TournamentDto tournamentDto) {
        return tournamentService.createTournament(tournamentDto);
    }

    @Override
    public MatchDb createTournamentMatch(Integer tournamentId) {
        return tournamentService.createTournamentMatch(tournamentId);
    }

    @Override
    public List<MatchDb> getTournamentMatches(Integer tournamentId) {
        return tournamentService.getTournamentMatches(tournamentId);
    }

    @Override
    public MatchDb getMatch(Integer matchId) {
        MatchDb matchDb = matchService.getMatch(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match " + matchId + " not found!!"));
        if (matchDb.getStatus() == Status.Finish)
            return matchDb;
        // botIds and scores sizes are corresponding to each other
        gameStateService.takeGameState(matchDb.getId()).ifPresent(
                wgs -> gameplay.calculateScores(wgs));
        return matchDb;
    }

    @Override
    public BotDb addBotToMatch(Integer matchId, BotDb botDb, BuildParam buildParam) throws ServiceException {
        MatchDb matchDb = matchService.getMatch(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match " + matchId + " not found!!"));
        return matchService.addBot(matchDb, botDb, buildParam);
    }

    @Override
    @Transactional
    public void addAllBotsToMatch(Integer matchId, BuildParam buildParam) throws ServiceException {
        List<BotDb> allBots = botRepository.findAll();
        allBots.forEach(bot -> addBotToMatch(matchId, bot, buildParam));
    }

    @Override
    @Transactional
    public void addBotsToTournamentMatch(Integer matchId) throws ServiceException {
        MatchDb matchDb = matchService.getMatch(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match " + matchId + " not found!!"));
        List<BotDb> bots = matchDb.getTournament().getBots().stream()
                .distinct()
                .collect(Collectors.toList());
        bots.forEach(botDb -> matchService.addBot(matchDb, botDb, new BuildParam(false)));
    }

    private void logState(Integer matchId, PlayParam playParam, ModelGameState gameState, List<String> botNames) {
        MDC.put("matchId", matchId.toString());
        if (matchLog.isDebugEnabled()) {
            matchLog.debug(gameplay.describeGameState(gameState, botNames, true, true));
        }
        try {
            if (!playParam.isSkipVideo()) {
                //Directory for generated frames and video
                Path pathVideoDir = ServiceUtils.getVideoDir(matchId, logVideoDir);
                //Directory for video and frames
                gameStateService.createGameStateFrame(pathVideoDir,
                    gameplay.describeGameState(gameState, botNames, false, false),
                    matchId, true);
            }
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    @Transactional
    public void startMatch(Integer matchId, PlayParam playParam) {
        if (playMatch(matchId, playParam).handle((matchDb, ex) -> {
            Status status = (ex != null) ? Status.Failed : Status.Finish;
            matchDb.setStatus(status);
            endMatch(matchDb, playParam);
            return status;
        }).join() == Status.Failed) {
            throw new GamePlayException("The match {} was failed!" + matchId);
        } else {
            log.info("The match {} was finished successfully!", matchId);
        }
    }

    @Async
    public CompletableFuture<MatchDb> playMatch(Integer matchId, PlayParam playParam) {
        MatchDb matchDb = getMatch(matchId);

        if (CollectionUtils.isEmpty(matchDb.getBots())) {
            throw new BotNotFoundException("Have no any bot in the match!");
        }

        val botDbs = matchDb.getBotMatches().stream()
                .distinct()
                .filter(BotMatchDb::getIsBuildSuccess)
                .map(BotMatchDb::getBot)
                .collect(Collectors.toList());
        List<Bot> bots = botService.mapBots(botDbs);
        Supplier<List<Bot>> botsFactory = () -> bots;
        val match = gameplay.createMatch(defaultFieldHeight, defaultFieldWidth,
            botsFactory, matchDb.getDuration(), matchDb.getPercent(), Optional.empty());

        // WARN: we put the _mutable_ game state into the cache
        gameStateService.keepGameState(matchId, match.getGameState());

        matchDb.setStatus(Status.Work);
        matchService.save(matchDb);

        MatchLogger logger = (gameState, botNames) ->
            logState(matchId, playParam, gameState, botNames);
        gameplay.runMatch(match, playParam.getDelay(), logger);

        return CompletableFuture.completedFuture(matchDb);
    }

    private void endMatch(MatchDb matchDb, PlayParam playParam) {
        gameStateService.takeGameState(matchDb.getId()).ifPresent( gameState -> {
            List<Integer> botIds = matchDb.getBotMatches().stream()
                .distinct()
                .filter(BotMatchDb::getIsBuildSuccess)
                .map(BotMatchDb::getBotId)
                .collect(Collectors.toList());
            // the key is index bot's index
            List<Integer> scores = gameplay.calculateScores(gameState);
            Map<Integer, Integer> scoresById = new HashMap<>(scores.size());
            AtomicInteger idx = new AtomicInteger(0);
            for (Integer botId : botIds) {
                scoresById.put(botId, scores.get(idx.getAndIncrement()));
            }
            List<BotMatchDb> botMatches = matchService.getBotMatchDb(matchDb);

            botMatches.forEach(bot ->
                scoresById.keySet().stream()
                    .filter(botId -> botId.equals(bot.getBotId()))
                    .findFirst()
                    .ifPresent(botId -> bot.setScore(scoresById.get(botId)))
            );
            try {
                if (!playParam.isSkipVideo()) {
                    Path pathVideoDir = ServiceUtils.getVideoDir(matchDb.getId(), logVideoDir);
                    gameStateService.createGameMatchVideo(matchDb.getId(), pathVideoDir);
                }
            } catch (Exception e) {
                throw new ServiceException(e);
            } finally {
                matchService.save(botMatches);
                matchService.save(matchDb);
                gameStateService.clearGameState(matchDb.getId());
            }
        });

    }
}

