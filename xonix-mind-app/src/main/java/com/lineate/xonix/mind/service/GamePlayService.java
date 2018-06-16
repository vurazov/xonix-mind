package com.lineate.xonix.mind.service;

import java.util.List;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.model.PlayParam;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface GamePlayService {

    void addAllBotsToMatch(Integer matchId, BuildParam buildParam) throws ServiceException;

    void addBotsToTournamentMatch(Integer matchId) throws ServiceException;

    MatchDb createMatch(MatchDb match);

    void startMatch(Integer matchId, PlayParam playParam);

    StateTournamentDto createTournament(TournamentDto tournamentDto);

    MatchDb createTournamentMatch(Integer tournamentId);

    List<MatchDb> getTournamentMatches(Integer tournamentId);

    MatchDb getMatch(Integer matchId);

    BotDb addBotToMatch(Integer matchId, BotDb botDb, BuildParam buildParam) throws ServiceException;

    List<MatchDb> getAllMatches();
}
