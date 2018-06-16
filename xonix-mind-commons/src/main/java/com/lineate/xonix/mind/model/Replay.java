package com.lineate.xonix.mind.model;

import java.util.List;
import java.util.Optional;
import lombok.Value;

/**
 * @apiNote all these lists are immutable
 * @apiNote botNames should be unique
 */
@Value
public class Replay {
    int height;
    int width;
    double percent;
    long duration;
    List<String> botNames;
    List<List<Move>> moves;
    ImmutableStats stats;
    Optional<Long> randomSeed;
}
