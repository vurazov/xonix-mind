package com.lineate.xonix.mind.service;


import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.GetAllBotsDto;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StartMatchDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.model.Status;

import java.util.List;
import java.util.Map;

public interface ViewService {

    StateTournamentDto createTournament(TournamentDto tournamentDto);

    List<GetAllBotsDto> getAllBotDto();

    BotDto saveBot(BotDto botDto);

    Map<String, List<List<String>>> prepareMatchesForView(List<MatchDb> matches);

    StateMatchDto addScores(StateMatchDto stateMatch);

    StateMatchDto createMatch(MatchDto matchDto);

    void startTournamentMatch(Integer tournamentId);

    void startMatch(Integer matchId, StartMatchDto startMatchDto);

    StateMatchDto getStateOfMatch(Integer matchId);

    Map<String, List<List<String>>> getTournamentMatches(Integer tournamentId);

    Map<String,List<List<String>>> getMatchesByState(Status finish);
}
