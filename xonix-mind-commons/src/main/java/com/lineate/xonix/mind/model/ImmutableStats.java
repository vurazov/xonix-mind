package com.lineate.xonix.mind.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImmutableStats {
    int iteration;
    int filledCount;
    int headToHeadCount;
    int selfBiteCount;
    int biteCount;
    List<Integer> scores;
}
