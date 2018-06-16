package com.lineate.xonix.mind.model;

import java.util.List;

@FunctionalInterface
public interface MatchLogger {
    void accept(ModelGameState gameState, List<String> botNames);
}
