package com.lineate.xonix.mind.model;

import java.util.Optional;
import lombok.Value;

@Value
public class PlayParam {
    boolean skipVideo;
    Optional<Integer> delay;
}
