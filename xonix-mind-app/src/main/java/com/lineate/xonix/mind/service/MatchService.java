package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.BotMatchDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.model.BuildParam;

import java.util.List;
import java.util.Optional;

public interface MatchService {
    MatchDb create(MatchDb match);

    Optional<MatchDb> getMatch(Integer matchId);

    BotDb addBot(MatchDb match, BotDb botDb, BuildParam buildParam);

    void save(MatchDb matchDb);

    List<MatchDb> availableMatches();

    List<BotMatchDb> getBotMatchDb(MatchDb matchDb);

    void save(List<BotMatchDb> botMatches);
}
