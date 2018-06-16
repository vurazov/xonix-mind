package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.ModelGameState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public interface GameStateService {

    void keepGameState(Integer matchId, ModelGameState state);

    void clearGameState(Integer id);

    Optional<ModelGameState> takeGameState(Integer matchId);

    void createGameStateFrame(Path pathVideoDir, String gameState, Integer matchId, boolean useColors);

    void createGameMatchVideo(Integer id, Path pathVideoDir) throws IOException;

    InputStream retrieveMatchReplay(Integer matchId);

    InputStream retrieveMvnBotLog(String matchId) throws ServiceException;
}
