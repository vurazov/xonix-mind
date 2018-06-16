package com.lineate.xonix.mind.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

/**
 * Mutable object to count statistics on the fly
 */
@Data
@Wither
@AllArgsConstructor
public class Stats {
    int iteration;
    int filledCount;
    int headToHeadCount;
    int selfBiteCount;
    int biteCount;
    List<Integer> scores;
}
