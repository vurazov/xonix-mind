package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.TournamentDb;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.GetAllBotsDto;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StartMatchDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.mapper.TypeMapper;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.PlayParam;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotMatchRepository;
import com.lineate.xonix.mind.repositories.BotRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ViewServiceImpl implements ViewService {

    private final TypeMapper mapper;
    private final BotRepository botRepository;
    private final BotMatchRepository botMatchRepository;
    private final GamePlayService gamePlayService;
    private final ExecutorService executorService;

    @Autowired
    public ViewServiceImpl(TypeMapper mapper, BotRepository botRepository,
                           GamePlayService gamePlayService,
                           BotMatchRepository botMatchRepository,
                           ExecutorService executorService) {
        this.mapper = mapper;
        this.botRepository = botRepository;
        this.botMatchRepository = botMatchRepository;
        this.gamePlayService = gamePlayService;
        this.executorService = executorService;
    }

    @Override
    public StateTournamentDto createTournament(TournamentDto tournamentDto) {
        StateTournamentDto stateTournamentDto  = gamePlayService.createTournament(tournamentDto);
        log.info("createTournament({}) : {}", stateTournamentDto.getId(), stateTournamentDto);
        return  stateTournamentDto;
    }

    @Override
    public List<GetAllBotsDto> getAllBotDto() {
        List<BotDb> botDbs = botRepository.findAll();
        return mapper.toGetAllBotsDtoList(botDbs);
    }

    @Override
    public BotDto saveBot(BotDto botDto) {
        BotDb botDb = mapper.mapToBot(botDto);
        botDb.setSrcUrl(StringUtils.removeEnd(botDb.getSrcUrl(), "/"));
        botDb = botRepository.save(botDb);
        return mapper.mapToBotDto(botDb);
    }

    @Override
    public StateMatchDto addScores(StateMatchDto stateMatch) {
        if (Status.Finish.equals(stateMatch.getStatus())) {
            List<BotMatchDb> botMatches = botMatchRepository.findByMatchId(stateMatch.getId());
            stateMatch.getBots().forEach(bot -> {
                Optional<BotMatchDb> botMatchOpt = botMatches.stream()
                        .filter(botMatch -> botMatch.getBotId().equals(bot.getId()))
                        .findFirst();
                if (botMatchOpt.isPresent() && botMatchOpt.get().getScore() != null){
                    BotMatchDb botMatch = botMatchOpt.get();
                    bot.setScore(botMatch.getScore());
                    bot.setIsBuildSuccess(botMatch.getIsBuildSuccess());
                    bot.setVersion(botMatch.getVersion());
                } else {
                    bot.setScore(-1);
                }
            });
        } else {
            stateMatch.getBots().forEach(bot -> bot.setScore(-1));
        }
        return stateMatch;
    }

    @Override
    public StateMatchDto createMatch(MatchDto matchDto) {
        //TODO: Add height and width in Dto
        MatchDb match = mapper.mapToMatch(matchDto);
        match.setCreatedAt(LocalDateTime.now());
        match = gamePlayService.createMatch(match);
        StateMatchDto stateMatchDto = mapper.mapToStateMatchDto(match);
        log.info("createMatch({}) : {}", match.getId(), stateMatchDto);
        return stateMatchDto;
    }

    @Override
    public void startTournamentMatch(Integer tournamentId) {
        MatchDb match = gamePlayService.createTournamentMatch(tournamentId);
        PlayParam playParam = new PlayParam(false, Optional.empty()); // default
        executorService.execute(() -> {
            gamePlayService.addBotsToTournamentMatch(match.getId());
            gamePlayService.startMatch(match.getId(), playParam);
            log.info("startTournamentMatch({}) : {}", match.getId());
        });
    }

    @Override
    public void startMatch(Integer matchId, StartMatchDto startMatchDto) {
        executorService.execute(() -> {
            boolean skipBuild = Optional.ofNullable(startMatchDto.getSkipBuild()).orElse(false);
            boolean skipVideo = Optional.ofNullable(startMatchDto.getSkipVideo()).orElse(false);
            Optional<Integer> delay = Optional.ofNullable(startMatchDto.getDelay());
            BuildParam buildParam = new BuildParam(skipBuild);
            PlayParam playParam = new PlayParam(skipVideo, delay);
            gamePlayService.addAllBotsToMatch(matchId, buildParam);
            gamePlayService.startMatch(matchId, playParam);
            log.info("startMatch({}) : {}", matchId);
        });
    }

    @Override
    public StateMatchDto getStateOfMatch(Integer matchId) {
        MatchDb match = gamePlayService.getMatch(matchId);
        StateMatchDto stateMatchDto = mapper.mapToStateMatchDto(match);
        addScores(stateMatchDto);

        return stateMatchDto;
    }

    @Override
    public Map<String, List<List<String>>> getTournamentMatches(Integer tournamentId) {
        List<MatchDb> matches = gamePlayService.getTournamentMatches(tournamentId);
        log.info("Tournament: {} , Matches is finished: {} ", tournamentId, matches.size());
        return prepareMatchesForView(matches);
    }

    @Override
    public Map<String, List<List<String>>> getMatchesByState(Status state) {
        List<MatchDb> matches = gamePlayService.getAllMatches().stream()
                .filter(matchDb -> matchDb.getStatus() == state)
                .collect(Collectors.toList());
        log.info("Matches is finished: {} ", matches.size());
        return prepareMatchesForView(matches);
    }

    @Override
    public Map<String, List<List<String>>> prepareMatchesForView(List<MatchDb> matches) {
        List<StateMatchDto> stateMatchDtos = mapper.toMatchDtoList(matches);
        List<List<String>> rows = new ArrayList<>();
        for (StateMatchDto stateMatchDto : stateMatchDtos) {
            this.addScores(stateMatchDto);
            List<String> row = new ArrayList<>();
            row.add(stateMatchDto.getCreatedAt().toString());
            row.addAll(stateMatchDto.getBots().stream()
                    .distinct()
                    .map(stateBotDto -> {
                        Integer score = stateBotDto.getScore();
                        if (score != -1) {
                            return String.format("%s:%s:%s", stateBotDto.getVersion(), score,
                                    stateBotDto.getIsBuildSuccess());
                        } else {
                            return "-";
                        }
                    })
                    .collect(Collectors.toList())
            );
            row.add(stateMatchDto.getId().toString());
            rows.add(row);
        }
        HashMap<String, List<List<String>>> response = new HashMap<>();
        List<String> headers = new ArrayList<>();
        headers.add("Created at");
        if (!matches.isEmpty()) {
            TournamentDb tournament = matches.get(0).getTournament();
            if (tournament != null) {
                tournament.getBots().stream()
                        .distinct()
                        .map(BotDb::getName)
                        .forEachOrdered(headers::add);
            } else {
                matches.get(0).getBots().stream()
                        .distinct()
                        .map(BotDb::getName)
                        .forEachOrdered(headers::add);
            }
        }
        headers.add("Video");

        response.put("headers", Arrays.asList(headers));
        response.put("rows", rows);
        log.info("getFinishedMatches() : {}", response);
        return response;
    }
}
