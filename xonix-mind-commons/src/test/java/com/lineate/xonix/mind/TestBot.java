package com.lineate.xonix.mind;

import java.util.Optional;
import java.util.Random;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.GameState;
import com.lineate.xonix.mind.model.Move;

public final class TestBot implements Bot {
    private final String path;
    private int iter = 0;
    private final Optional<Integer> idx;
    private final Optional<Random> random;

    public TestBot(String path) {
        this.path = path;
        this.idx = Optional.empty();
        this.random = Optional.empty();
    }

    public TestBot(int idx, String path) {
        this.path = path;
        this.idx = Optional.of(idx);
        this.random = Optional.empty();
    }

    public TestBot(int idx, String path, Random random) {
        this.path = path;
        this.idx = Optional.of(idx);
        this.random = Optional.of(random);
    }

    @Override
    public String getName() {
        return idx
            .map(id -> String.valueOf((char)('A' + id)) + ":" + path)
            .orElse("?:" + path);
    }

    @Override
    public Move move(GameState gs) {
        Move move;
        if (iter >= path.length()) {
            return random
                .map(r -> Move.values()[r.nextInt(4)])
                .orElse(Move.STOP);
        }
        else {
            switch (path.charAt(iter)) {
                case 'u': case 'U': move = Move.UP; break;
                case 'd': case 'D': move = Move.DOWN; break;
                case 'l': case 'L': move = Move.LEFT; break;
                case 'r': case 'R': move = Move.RIGHT; break;
                case 's': case 'S': move = Move.STOP; break;
                default:
                    throw new RuntimeException("Invalid symbol: " + path.charAt(iter));
            }
            iter += 1;
        }
        return move;
    }

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        final StringBuilder sb = new StringBuilder("TestBot(");
        sb.append(getName()).append(')');
        return sb.toString();
    }
}
