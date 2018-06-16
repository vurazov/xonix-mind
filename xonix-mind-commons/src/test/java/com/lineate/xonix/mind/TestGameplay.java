package com.lineate.xonix.mind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.Cell;
import com.lineate.xonix.mind.model.Field;
import com.lineate.xonix.mind.model.Gameplay;
import com.lineate.xonix.mind.model.MatchLogger;
import com.lineate.xonix.mind.model.ModelGameState;
import com.lineate.xonix.mind.model.Move;
import com.lineate.xonix.mind.model.Player;
import com.lineate.xonix.mind.model.Point;
import com.lineate.xonix.mind.model.Replay;
import com.lineate.xonix.mind.model.Stats;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TestGameplay {

    private final Gameplay game = Gameplay.IT;

    @Test
    public void testBorder() {
        // test on the different cells sizes
        val fieldSizes = Arrays.asList(Pair.of(2, 2), Pair.of(9, 3), Pair.of(3, 4), Pair.of(4, 7));
        //         n
        //   * * * * * * *
        // m *           *
        //   *           *
        //   * * * * * * *
        for (val sz : fieldSizes) {
            val m = sz.getLeft();
            val n = sz.getRight();
            val perimeter = new ArrayList<Point>();
            perimeter.addAll(points(0, IntStream.range(0, n)));
            perimeter.addAll(points(IntStream.rangeClosed(1, m - 2), n - 1));
            perimeter.addAll(points(m - 1, revRangeClosed(n - 1, 0)));
            perimeter.addAll(points(revRangeClosed(m - 2, 1), 0));
            val size = 2 * (m + n - 2);
            assertEquals(size, perimeter.size());
            for (int l = 0; l <= 2 * size; l++) {
                assertEquals(perimeter.get(l % size), game.border2Point(m, n, l));
            }
        }
    }

    @Test
    public void testCreateOrigins() {
        Field field = game.createDefaultField(7, 9);
        // 2 bots - opposite corners
        val o2 = game.createOrigins(field.getHeight(), field.getWidth(), 2);
        assertEquals(Arrays.asList(
            Point.of(0, 0), Point.of(6, 8)
        ), o2);
        // 4 bots - all corners
        val o4 = game.createOrigins(field.getHeight(), field.getWidth(), 4);
        assertEquals(Arrays.asList(
            Point.of(0, 0), Point.of(6, 8), Point.of(0, 8), Point.of(6, 0)
        ), o4);
        // otherwise - spread in the perimeter
        val o8 = game.createOrigins(field.getHeight(), field.getWidth(), 8);
        assertEquals(Arrays.asList(
            Point.of(0, 0), Point.of(0, 3), Point.of(0, 6), Point.of(1, 8),
            Point.of(4, 8), Point.of(6, 7), Point.of(6, 4), Point.of(6, 1)
        ), o8);
    }

    @Test
    public void testBotScores() {
        val gs = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*.0. A1.1.1.*.\n" +
            "*. a a B b2D*.\n" +
            "*.3C3.3. .2.*.\n" +
            "*.*.* *.*.*.*.\n"
        );
        val scores = game.calculateScores(gs);
        assertEquals(Arrays.asList(1, 3, 2, 3), scores);
    }

    @Test
    public void testFlood() {
        val gs = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*. . .1. . .*.\n" +
            "*a a a a a A*.\n" +
            "*. .1. . . .*.\n" +
            "*.*.*.*.*.*.*B\n"
        );
        val bodies = gs.getPlayers().stream()
            .map(Player::getBody)
            .flatMap(Collection::stream)
            .collect(toSet());
        val points1 = game.flood(gs.getField(), bodies, Point.of(1, 1));
        val points2 = game.flood(gs.getField(), bodies, Point.of(3, 2));
        val points3 = game.flood(gs.getField(), bodies, Point.of(3, 3));
        assertEquals(Sets.newHashSet(Point.of(1, 1), Point.of(1, 2)), points1);
        assertEquals(Sets.newHashSet(), points2);
        assertEquals(Sets.newHashSet(Point.of(3, 3), Point.of(3, 4), Point.of(3, 5)), points3);
    }

    @Test
    public void testFloodBehavior() {
        val gs0 = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*. b B . A .*.\n" +
            "*. a a a a .*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*.\n"
        );
        val a = testBot("u");
        val b = testBot("");
        val gs1 = play(gs0, a, b);
        val gsExp = gameState("" +
            "*.*.*.*.*A*.*.\n" +
            "*.0.0B0.0. .*.\n" +
            "*.0.0.0.0. .*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*.\n"
        );
        gsExp.getStats().setIteration(1);
        assertEquals(gsExp.getStats(), gs1.getStats());
        assertEquals(gsExp, gs1);
    }

    @Test
    public void testTinyFlood() {
        val gs0 = gameState("" +
            "*A*.*.*.*.*.*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*C*.*.*.*.*.*B\n"
        );
        val a = testBot("dru");
        val b = testBot("ulr");
        val c = testBot("rrurd");
        val gs1 = play(gs0, a, b, c);
        val gsExp = gameState("" +
            "*.*A*.*.*.*.*.\n" +
            "*.0. . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. .2.2. .1.*B\n" +
            "*.*.*.*C*.*.*.\n"
        );
        gsExp.getStats().setIteration(5);
        assertEquals(gsExp.getStats(), gs1.getStats());
        assertEquals(gsExp, gs1);
    }

    @Test
    public void testHeadToHead() {
        val gs0 = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*. . . . . .*.\n" +
            "*A . . . . .*B\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*.\n"
        );
        val a = testBot("rrrrr");
        val b = testBot("lllll");
        val gs1 = play(gs0, a, b);
        val gsExp = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*. . . . . .*.\n" +
            "*. a a A B b*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*.\n"
        );
        gsExp.getStats().setIteration(5);
        gsExp.getStats().setHeadToHeadCount(5);
        assertEquals(gsExp.getStats(), gs1.getStats());
        assertEquals(gsExp, gs1);
    }

    @Test
    public void testBite() {
        val gs0 = gameState("" +
            "*.*.*.*B*.*.*.\n" +
            "*A . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*C\n"
        );
        val a = testBot("rrrrr");
        val b = testBot("ddddd");
        val c = testBot("");
        val gs1 = play(gs0, a, b, c);
        val gsExp = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*. a a a a A*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*B\n" +
            "*.*.*.*.*.*.*C\n"
        );
        gsExp.getStats().setBiteCount(1);
        gsExp.getStats().setIteration(5);
        gsExp.getStats().setHeadToHeadCount(3);
        assertEquals(gsExp.getStats(), gs1.getStats());
        assertEquals(gsExp, gs1);
    }

    @Test
    public void testSelfBite() {
        val gs0 = gameState("" +
            "*A*.*.*.*.*.*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*B\n"
        );
        val a = testBot("drrrrddlluu");
        val b = testBot("");
        val gs1 = play(gs0, a, b);
        val gsExp = gameState("" +
            "*A*.*.*.*.*.*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*.*.*.*.*.*.*B\n"
        );
        gsExp.getStats().setSelfBiteCount(1);
        gsExp.getStats().setIteration(11);
        assertEquals(gsExp.getStats(), gs1.getStats());
        assertEquals(gsExp, gs1);
    }

    @Test
    public void testDescribeGS() {
        val gs0 = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*.0. A .1.1.*.\n" +
            "*. a a B b2D*.\n" +
            "*.3C3.3. .2.*.\n" +
            "*.*.* *.*.*.*.\n"
        );
        val bns = Arrays.asList("bot_a", "bot_b", "bot_c", "bot_d");
        val exp = "" +
            "             \n" +
            "A: \"bot_a\" - 1\n" +
            "B: \"bot_b\" - 2\n" +
            "C: \"bot_c\" - 2\n" +
            "D: \"bot_d\" - 3\n" +
            "*.*.*.*.*.*.*\n" +
            "*.0.A. .1.1.*\n" +
            "*.a.a.B.b.D.*\n" +
            "*.C.3.3. .2.*\n" +
            "*.*.*.*.*.*.*\n";
        assertEquals(exp, game.describeGameState(gs0, bns, false, false));
    }

    @Test
    public void testClientGameState() {
        val gs = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*.0. a a a A*.\n" +
            "*.0. . . . .*.\n" +
            "*.0. . .1.1B*.\n" +
            "*.*.*.*.*.*.*.\n"
        );
        val cells = new Cell[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 || i == 4 || j == 0 || j == 6)
                    cells[i][j] = Cell.border();
                else if (j == 1) {
                    cells[i][j] = Cell.owned(0);
                } else if (i == 3 && (j == 4 || j == 5)) {
                    cells[i][j] = Cell.owned(1);
                } else {
                    cells[i][j] = Cell.empty();
                }
            }
        }

        val cgs0 = game.getClientGameState(gs, 0);
        assertEquals(0, cgs0.botId);
        assertEquals("[(1,2), (1,3), (1,4), (1,5)]", cgs0.me.getBody().toString());
        assertEquals("[Tail(it=[(1,2), (1,3), (1,4)]), Tail(it=[])]", cgs0.others.toString());
        assertArrayEquals(cells, cgs0.cells);

        val cgs1 = game.getClientGameState(gs, 1);
        assertEquals(1, cgs1.botId);
        assertEquals("[(3,5)]", cgs1.me.getBody().toString());
        assertEquals("[Tail(it=[(1,2), (1,3), (1,4)]), Tail(it=[])]", cgs1.others.toString());
        assertArrayEquals(cells, cgs1.cells);
    }

    @Test
    public void testCopyShuffled() {
        val perm0 = game.createDefaultPermutation(4);
        assertEquals("[0, 1, 2, 3]", perm0.toString());
        val random = new Random(123);
        assertEquals("[0, 1, 3, 2]", game.copyShuffledPermutation(perm0, random).toString());
        assertEquals("[2, 3, 0, 1]", game.copyShuffledPermutation(perm0, random).toString());
        assertEquals("[0, 3, 1, 2]", game.copyShuffledPermutation(perm0, random).toString());
    }

    @Test
    public void testRunMatchWithReordering() {
        val a = new TestBot(0, "dlu");
        val b = new TestBot(1, "llurr");
        val c = new TestBot(2, "urd");
        val d = new TestBot(3, "rrrdlll");
        Supplier<List<Bot>> botsFactory = () -> Arrays.asList(a, b, c, d);
        val match = game.createMatch(5, 7, botsFactory, 20, 90.0, Optional.of(42L));
        val gs = gameState("" +
            "*D*.*.*.*.*.*A\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*. . . . . .*.\n" +
            "*C*.*.*.*.*.*B\n" +
            "reordering=[2,1,3,0]"
        ).withOrigins(
            ImmutableList.of(Point.of(0,6),Point.of(4,6),Point.of(4,0),Point.of(0,0))
        );
        assertEquals(gs, match.getGameState());
        MatchLogger logger = (gameState, botNames) -> { };
        game.runMatch(match, Optional.empty(), logger);
        val finalGs = gameState("" +
            "*.*.*.*.*.*A*.\n" +
            "*D3.3.3. .0.*.\n" +
            "*. . . . . .*.\n" +
            "*.2. . .1.1.*B\n" +
            "*.*C*.*.*.*.*.\n" +
            "reordering=[2,1,3,0]\n" +
            "stats=Stats(20,27,0,0,0,[1,2,1,3])"
        ).withOrigins(gs.getOrigins());
        assertEquals(match.getGameState(), finalGs);
    }

    @Test
    public void testRunReplay() {
        val random = new Random(42);
        Supplier<List<Bot>> botFactory = () -> {
            val a = new TestBot(0, "dllll", random);
            val b = new TestBot(1, "luuuu", random);
            val c = new TestBot(2, "urrrr", random);
            val d = new TestBot(3, "rdddd", random);
            return Arrays.asList(a, b, c, d);
        };
        MatchLogger logger = (gameState, botNames) -> { };
        // to be sure it is deterministic
        for (int k = 0; k < 10; k++) {
            // run match
            val match = game.createMatch(7, 7, botFactory, 32, 90.0, Optional.of(random.nextLong()));
            val replay = game.runMatch(match, Optional.empty(), logger);
            // run replay
            val replayGs1 = game.runReplay(replay, Optional.empty(), logger);
            val replayGs2 = game.runReplay(replay, Optional.empty(), logger);
            // compare
            assertEquals(match.getGameState(), replayGs1);
            assertEquals(match.getGameState(), replayGs2);
        }
    }

    @Test
    public void testRunTournament() {
        val random = new Random(42);
        Supplier<List<Bot>> botFactory = () -> {
            val a = new TestBot(0, "dlu", random);
            val b = new TestBot(1, "lur", random);
            val c = new TestBot(2, "urd", random);
            val d = new TestBot(3, "rdl", random);
            return Arrays.asList(a, b, c, d);
        };
        MatchLogger logger = (gameState, botNames) -> { };
        val matchCount = 100;
        val gameStates = new ArrayList<ModelGameState>(100);
        val replays = new ArrayList<Replay>(100);
        for (int it = 0; it < matchCount; it++) {
            val seed = random.nextLong();
            val match = game.createMatch(5, 7, botFactory, 30, 90.0, Optional.of(seed));
            val replay = game.runMatch(match, Optional.empty(), logger);
            replays.add(replay);
            gameStates.add(match.getGameState());
        }
        // tournament is done, check some matches
        val exp66 = gameState("" +
            "*.*.*.*.*.*.*.\n" +
            "*.0.1B . .2.*.\n" +
            "*A0. . . .3D*.\n" +
            "*.1.1.1C0.3.*.\n" +
            "*.*.*.*.*.*.*.\n" +
            "reordering=[1,3,0,2]\n" +
            "stats=Stats(30,30,5,2,0,[3,4,1,2])\n" +
            "origins=[(0,0),(4,0),(0,6),(4,6)]\n"
        );
        assertEquals(exp66, gameStates.get(66));
        MatchLogger logger2 = (gameState, botNames) -> {
            // System.out.println("iteration = " + gameState.getStats().getIteration()
            //     + ", gameState = \n" + gameState);
        };
        val rgs66 = game.runReplay(replays.get(66), Optional.empty(), logger2);
        assertEquals(exp66, rgs66);
    }

    @Test
    public void testParseString() {
        val seed = 42L;
        val random = new Random(96);
        List<Bot> bots = IntStream.range(0, 4)
            .mapToObj(i -> new TestBot(i, "", random))
            .collect(toList());
        Supplier<List<Bot>> botFactory = () -> bots;
        val match = game.createMatch(5, 7, botFactory, 20, 90.0, Optional.of(seed));
        MatchLogger logger = (gs, ns) -> {};
        game.runMatch(match, Optional.empty(), logger);
        String str = match.getGameState().toString();
        assertEquals("" +
            "*.*.*.*.*A*.*.\n" +
            "*.3.2.2.2.0.*.\n" +
            "*.2D2.2C2.1.*.\n" +
            "*.2.2. . .1B*.\n" +
            "*.*.*.*.*.*.*.\n" +
            "reordering=[2,1,3,0]\n" +
            "stats=Stats(19,33,2,1,0,[1,2,9,1])\n" +
            "origins=[(0,6),(4,6),(4,0),(0,0)]\n", str);
        val gs2 = gameState(str);
        assertEquals(match.getGameState(), gs2);
    }

    // utils

    private ModelGameState gameState(String picture) {
        return ModelGameState.parseString(picture);
    }

    private Bot testBot(String path) {
        return new TestBot(path);
    }

    private ModelGameState play(ModelGameState initGs, Bot... bots) {
        ModelGameState igs = deepCopy(initGs);
        boolean progressing = true;
        int iteration = 0;
        while (progressing) {
            igs.getStats().setIteration(iteration++);
            val moves = new ArrayList<Move>();
            for (int k = 0; k < bots.length; k++) {
                val idx = igs.getReordering().get(k);
                val gs = game.getClientGameState(igs, idx);
                val m = bots[idx].move(gs);
                moves.add(m);
                igs = game.step(igs, idx, m);
            }
            if (moves.stream().allMatch(it -> it == Move.STOP))
                progressing = false;
        }
        return igs;
    }

    private List<Point> points(int i, IntStream jj) {
        return jj.mapToObj(j -> Point.of(i, j)).collect(toList());
    }

    private List<Point> points(IntStream ii, int j) {
        return ii.mapToObj(i -> Point.of(i, j)).collect(toList());
    }

    private static IntStream revRangeClosed(int from, int to) {
        return IntStream.rangeClosed(to, from)
            .map(i -> to - i + from);
    }

    private Field deepCopy(Field field) {
        val cells = new HashMap<Point, Cell>(field.getCells());
        return new Field(field.getHeight(), field.getWidth(), cells);
    }

    private ModelGameState deepCopy(ModelGameState gs) {
        val field = deepCopy(gs.getField());
        val origins = ImmutableList.copyOf(gs.getOrigins());
        ImmutableList.Builder<Player> pb = ImmutableList.builder();
        for (Player player : gs.getPlayers()) {
            pb.add(new Player(new ArrayList<>(player.getBody())));
        }
        val players = pb.build();
        val stats = new Stats(
            gs.getStats().getIteration(),
            gs.getStats().getFilledCount(),
            gs.getStats().getHeadToHeadCount(),
            gs.getStats().getSelfBiteCount(),
            gs.getStats().getBiteCount(),
            Lists.newArrayList(gs.getStats().getScores())
        );
        val reordering = ImmutableList.copyOf(gs.getReordering());
        return ModelGameState.of(field, origins, players, stats, reordering);
    }

}
