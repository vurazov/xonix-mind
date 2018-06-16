package com.lineate.xonix.mind.model;

import java.util.Arrays;
import java.util.List;

/**
 * Safe to be modified, will be reset before the next move.
 */
public class GameState {

    public final int botId;

    // the last element is the player's head
    public final Player me;

    // others are without their heads
    public final List<Tail> others;

    public final Cell[][] cells;

    public GameState(int botId, Player me, List<Tail> others, Cell[][] cells) {
        this.botId = botId;
        this.me = me;
        this.others = others;
        this.cells = cells;
    }

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder sb = new StringBuilder("GameState(");
        sb.append("botId=").append(botId);
        sb.append(",me=").append(me);
        sb.append(",others=").append(others);
        sb.append(",cells=").append(Arrays.toString(cells));
        sb.append(')');
        return sb.toString();
    }
}
