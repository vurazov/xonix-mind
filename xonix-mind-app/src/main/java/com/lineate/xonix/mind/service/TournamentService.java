package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;

import java.util.List;

public interface TournamentService {
    MatchDb createTournamentMatch(Integer tournamentId);

    List<MatchDb> getTournamentMatches(Integer tournamentId);

    StateTournamentDto createTournament(TournamentDto tournamentDto);
}
