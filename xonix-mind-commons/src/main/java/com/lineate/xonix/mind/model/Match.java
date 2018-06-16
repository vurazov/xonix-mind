package com.lineate.xonix.mind.model;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Match {
    double percent;
    long duration;
    ModelGameState gameState;
    List<Bot> bots;
    Optional<Long> randomSeed;
}
