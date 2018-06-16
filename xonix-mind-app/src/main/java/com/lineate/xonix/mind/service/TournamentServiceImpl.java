package com.lineate.xonix.mind.service;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.TournamentDb;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.exception.TournamentNotFoundEcxeption;
import com.lineate.xonix.mind.mapper.TypeMapper;
import com.lineate.xonix.mind.model.Status;
import com.lineate.xonix.mind.repositories.BotRepository;
import com.lineate.xonix.mind.repositories.MatchRepository;
import com.lineate.xonix.mind.repositories.TournamentRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TournamentServiceImpl implements TournamentService {

    @NonNull
    private final
    TournamentRepository tournamentRepository;

    @NonNull
    private final
    BotRepository botRepository;

    @NonNull
    private final
    MatchRepository matchRepository;

    @NonNull
    private final
    MatchService matchService;

    @NonNull
    private final
    TypeMapper mapper;

    @Value("${xonix.default.match.duration}")
    Long defaultMatchDuration;

    @Value("${xonix.default.field.filled}")
    Double defaultFieldFilled;

    Predicate<TournamentDb> validator = tournament -> new DefaultTournamentValidator().test(tournament);

    @Autowired
    public TournamentServiceImpl(
            TournamentRepository tournamentRepository,
            BotRepository botRepository,
            MatchRepository matchRepository,
            MatchService matchService,
            TypeMapper mapper
    ) {
        this.tournamentRepository = tournamentRepository;
        this.botRepository = botRepository;
        this.matchRepository = matchRepository;
        this.matchService = matchService;
        this.mapper = mapper;
    }

    @Override
    public MatchDb createTournamentMatch(Integer tournamentId) {
        TournamentDb tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(()
                        -> new TournamentNotFoundEcxeption("Not found tournament by id: {" + tournamentId + "}"));
        MatchDb tournamentMatch = new MatchDb();
        tournamentMatch.setDuration(tournament.getDuration());
        tournamentMatch.setPercent(tournament.getPercent());
        tournamentMatch.setTournament(tournament);
        return matchService.create(tournamentMatch);
    }

    @Override
    public List<MatchDb> getTournamentMatches(Integer tournamentId) {
        TournamentDb tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(()
                        -> new TournamentNotFoundEcxeption("Not found tournament by id: {" + tournamentId + "}"));
        return tournament.getMatches().stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public StateTournamentDto createTournament(TournamentDto tournamentDto) {
        TournamentDb tournamentDb = mapper.mapToTournamentDb(tournamentDto);
        if (!validator.test(tournamentDb)) {
            tournamentDb.setDuration(defaultMatchDuration);
            tournamentDb.setPercent(defaultFieldFilled);
        }
        tournamentDb.setBots(Lists.newArrayList());
        for (Integer botId: tournamentDto.getBots()) {
            botRepository.findById(botId).ifPresent(tournamentDb.getBots()::add);
        }
        tournamentDb.setStatus(Status.New);

        return mapper.mapToStateTournamentDto(tournamentRepository.save(tournamentDb));
    }


    class DefaultTournamentValidator implements Predicate<TournamentDb> {
        @Override
        public boolean test(TournamentDb tournament) {
            try {
                Objects.requireNonNull(tournament.getDuration());
                Objects.requireNonNull(tournament.getPercent());
            } catch (NullPointerException e) {
                return false;
            }
            return tournament.getDuration() > 0 && tournament.getPercent() > 0d;
        }
    }

}
