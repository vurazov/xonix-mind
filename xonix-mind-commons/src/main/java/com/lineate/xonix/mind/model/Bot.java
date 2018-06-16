package com.lineate.xonix.mind.model;

public interface Bot {

    String getName();

    Move move(GameState gs);
}
