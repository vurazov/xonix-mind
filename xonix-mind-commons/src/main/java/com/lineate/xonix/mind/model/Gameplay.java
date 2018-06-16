package com.lineate.xonix.mind.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Tis is the whole game model in one class without external dependencies.
 * The intent is to use it inside services.
 * @implNote _Must_ be stateless or have immutable state to be used safely.
 * @implNote not thread-safe
 */
@Slf4j
public class Gameplay {

    public static final Gameplay IT = new Gameplay();

    private final List<String> colors =
        ImmutableList.of(
            "\u001B[91m", "\u001B[92m", "\u001B[93m", "\u001B[94m",
            "\u001B[95m", "\u001B[96m", "\u001B[97m", "\u001B[90m",
            "\u001B[31m", "\u001B[32m", "\u001B[33m", "\u001B[34m",
            "\u001B[35m", "\u001B[36m", "\u001B[37m", "\u001B[30m"
        );

    public List<Point> createOrigins(int rows, int cols, int botsCount) {
        val perm = createDefaultPermutation(botsCount);
        return createOrigins(rows, cols, perm);
    }

    public List<Point> createOrigins(int rows, int cols, List<Integer> permutation) {
        Function<Integer, Point> b2p = l -> border2Point(rows, cols, l);
        val botsCount = permutation.size();
        val corners = Arrays.asList(b2p.apply(0),
            b2p.apply(rows + cols - 2),
            b2p.apply(cols - 1),
            b2p.apply(rows + 2 * cols - 3));
        switch (botsCount) {
            case 0: {
                return ImmutableList.of();
            }
            case 1: {
                val p0 = permutation.indexOf(0);
                return ImmutableList.of(corners.get(p0));
            }
            case 2: {
                val p0 = permutation.indexOf(0);
                val p1 = permutation.indexOf(1);
                return ImmutableList.of(corners.get(p0), corners.get(p1));
            }
            case 3: {
                val p0 = permutation.indexOf(0);
                val p1 = permutation.indexOf(1);
                val p2 = permutation.indexOf(2);
                return ImmutableList.of(corners.get(p0), corners.get(p1), corners.get(p2));
            }
            case 4: {
                val p0 = permutation.indexOf(0);
                val p1 = permutation.indexOf(1);
                val p2 = permutation.indexOf(2);
                val p3 = permutation.indexOf(3);
                return ImmutableList.of(corners.get(p0), corners.get(p1), corners.get(p2), corners.get(p3));
            }
            default:
                // uniformly distribute across the perimeter
                val step = 2 * (rows + cols - 2) / botsCount;
                Point[] origins = new Point[botsCount];
                for (int i = 0; i < botsCount; i++) {
                    origins[permutation.indexOf(i)] = b2p.apply(i * step);
                }
                return ImmutableList.copyOf(origins);
        }
    }

    public Field createDefaultField(int height, int width) {
        Map<Point, Cell> cells = new HashMap<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1)
                    cells.put(Point.of(i, j), Cell.border());
                else if (j == 0 || j == width - 1)
                    cells.put(Point.of(i, j), Cell.border());
                else
                    cells.put(Point.of(i, j), Cell.empty());
            }
        }
        return new Field(height, width, cells);
    }

    public GameState getClientGameState(ModelGameState gs, int botIdx) {
        Field field = gs.getField();
        Cell[][] cells = new Cell[field.getHeight()][field.getWidth()];
        for (Map.Entry<Point, Cell> entry : field.getCells().entrySet()) {
            Point p = entry.getKey();
            Cell cell = entry.getValue();
            cells[p.getRow()][p.getCol()] = cell;
        }
        val players = gs.getPlayers();
        val me = new Player(Lists.newArrayList(players.get(botIdx).getBody()));
        val others = new ArrayList<Tail>();
        for (final Player player : players) {
            // TODO Insert the link to the documentation that says not to show the heads to the Bot implementations
            if (!player.isEmpty()) {
                val it = ImmutableList.copyOf(player.tail().orElseGet(Lists::newArrayList));
                others.add(new Tail(it));
            } else {
                log.warn("Broken invariant, player is empty," +
                    " gs=" + gs.toString() +
                    " botIdx=" + botIdx);
                others.add(new Tail(ImmutableList.of()));
            }
        }
        return new GameState(botIdx, me, others, cells);
    }

    public double getFillPercentage(ModelGameState gameState) {
        Field field = gameState.getField();
        int square = field.getHeight() * field.getWidth();
        // int occupiedArea = 0;
        // for (Cell cell : field.getCells().values()) {
        //     if (!cell.isEmpty())
        //         occupiedArea += 1;
        // }
        // return ((double) occupiedArea / square) * 100;
        return (double) gameState.getStats().getFilledCount() / square * 100;
    }

    public List<Integer> calculateScores(ModelGameState gs) {
        return ImmutableList.copyOf(gs.getStats().getScores());
    }

    public Point border2Point(int rows, int width, int absPos) {
        val lengthPerimeter = absPos % (2 * (rows + width) - 4);
        if (lengthPerimeter < width)
            return Point.of(0, lengthPerimeter);
        else if (lengthPerimeter < width + rows - 2)
            return Point.of(lengthPerimeter - width + 1, width - 1);
        else if (lengthPerimeter < width + width + rows - 2)
            return Point.of(rows - 1, width + width + rows - 3 - lengthPerimeter);
        else
            return Point.of(width + width + rows + rows - 4 - lengthPerimeter, 0);
    }

    public ModelGameState step(ModelGameState gs, int index, Move move) {
        //the killed bots by bot
        val field = gs.getField();
        List<Player> players = gs.getPlayers();
        val me = players.get(index);
        val oldHead = me.head().orElseGet(() -> {
            log.warn("Broken invariant, me.head() is empty," +
                " gs=" + gs.toString() +
                " index=" + index +
                " move=" + move);
            return gs.getOrigins().get(index);
        });
        val newHead = calculateHead(field, oldHead, move);
        val cells = field.getCells();
        val stats = gs.getStats();

        if (oldHead.equals(newHead)){
            // the bot doesn't move
            return gs;
        }

        val oldCell = cells.get(oldHead);
        val newCell = cells.get(newHead);

        // collision or tail or head bot (other or itself), that is we have collision
        // in the cells tail is head
        val collisionOpt = IntStream.range(0, players.size())
            .filter(k -> players.get(k).contains(newHead))
            .mapToObj(k -> Pair.of(k, players.get(k)))
            .findFirst();
        if (newHead != oldHead && collisionOpt.isPresent()) {
            Pair<Integer, Player> collision = collisionOpt.get();
            Point collHead = collision.getValue().head().orElseGet(() -> {
                log.warn("Broken invariant, me is empty, gs = " + gs.toString());
                return gs.getOrigins().get(collision.getKey());
            });
            //1. the player bumps with the other player's head
            //2. the player eats itself
            //3. the player pid moves, and other player dies
            if (newHead.equals(collHead)) {
                // the player bumps with the other player's head
                stats.setHeadToHeadCount(stats.getHeadToHeadCount() + 1);
            } else if (collision.getKey() == index) {
                // the player eats itself
                respawnPlayer(gs, index);
                stats.setSelfBiteCount(stats.getSelfBiteCount() + 1);
            } else {
                // the player `index` moves, and other player `collision.key` dies,
                // if the current player was on the empty cell, its tail increases
                // otherwise it just moves to the next cell
                val deadIdx = collision.getKey();
                respawnPlayer(gs, deadIdx);
                stats.setBiteCount(stats.getBiteCount() + 1);
                if (oldCell.equals(Cell.empty())) {
                    me.addNewHead(newHead);
                } else {
                    me.setOnlyHead(newHead);
                }
            }
        } else if (!newHead.equals(oldHead) && !oldCell.isEmpty()) {
            // we stay on the nonempty cell
            // single head, don't make the tail, just setPoint the head
            // otherwise we should have made the contour from the previous step
            if (me.size() > 1) {
                // we detected violated invariant
                log.warn("Broken invariant, we stay on the nonempty cell and have the tail," +
                    " gs=" + gs.toString() +
                    " index=" + index +
                    " move=" + move);
            }
            me.setOnlyHead(newHead);
        } else if (!newHead.equals(oldHead) && !newCell.isEmpty()) {
            // we step from empty to nonempty, calculate the contours
            val flooded = calculateFloodedAreas(field, me.getBody());
            // flood area now becomes owned by the current player
            for (Point p : flooded) {
                cells.put(p, Cell.owned(index));
            }
            // flood removes tails if any
            for (int k = 0; k < players.size(); k++) {
                Player player = players.get(k);
                if (k == index) {
                    me.setOnlyHead(newHead);
                } else if (player.isEmpty()) {
                    log.warn("Broken invariant, found empty player," +
                        " gs=" + gs.toString() +
                        " index=" + index +
                        " move=" + move);
                } else {
                    val head = player.head().orElseGet(() -> {
                        log.warn("Broken invariant, found empty player," +
                            " gs=" + gs.toString() +
                            " index=" + index +
                            " move=" + move);
                        return gs.getOrigins().get(index);
                    });
                    val rest = player.getBody().stream()
                        .filter(p -> !cells.get(p).isOwned())
                        .collect(toList());
                    player.makeBody(head, rest);
                }
            }
            // finally update statistics
            stats.getScores().set(index, stats.getScores().get(index) + flooded.size());
            stats.setFilledCount(stats.getFilledCount() + flooded.size());
        } else if (!newHead.equals(oldHead)) {
            // oldCell == EMPTY && newCell == EMPTY (for sure)
            // we step into empty area, increase the tail
            // note: head is the last element
            me.addNewHead(newHead);
        }
        return gs;
    }

    public void respawnPlayer(ModelGameState gs, int deadIdx) {
        val deadPlayer = gs.getPlayers().get(deadIdx); // mutable ArrayList here
        val field = gs.getField();
        val cells = gs.getField().getCells();

        val others = new HashSet<Point>();
        for (int k = 0; k < gs.getPlayers().size(); k++) {
            if (k != deadIdx) {
                others.addAll(gs.getPlayers().get(k).getBody());
            }
        }

        Function<Point, Boolean> isAccessible = point ->
            hasInside(point, field)
                && cells.get(point).isBorder()
                && !others.contains(point);

        // find the closest to the origin nonempty cell
        val origin = gs.getOrigins().get(deadIdx);
        if (isAccessible.apply(origin)) {
            deadPlayer.setOnlyHead(origin);
            return ;
        }

        for (int r = 1; r < field.getHeight() + field.getWidth(); r++) {
            // diagonal points
            for (int k = 0; k < r; k++) {
                // vertexes in square distances from point
                val p1 = Point.of(origin.getRow() - k, origin.getCol() + r - k);
                val p2 = Point.of(origin.getRow() - r + k, origin.getCol() - k);
                val p3 = Point.of(origin.getRow() + k, origin.getCol() - r + k);
                val p4 = Point.of(origin.getRow() + r - k, origin.getCol() + k);
                if (isAccessible.apply(p1)) {
                    deadPlayer.setOnlyHead(p1);
                    return;
                }
                if (isAccessible.apply(p2)) {
                    deadPlayer.setOnlyHead(p2);
                    return;
                }
                if (isAccessible.apply(p3)) {
                    deadPlayer.setOnlyHead(p3);
                    return;
                }
                if (isAccessible.apply(p4)) {
                    deadPlayer.setOnlyHead(p4);
                    return;
                }
            }
        }
        log.warn("Broken game state," +
            " origins=" + gs.getOrigins() +
            " idx=" + deadIdx +
            " body=" + deadPlayer +
            " gameState=" + gs.toString());
    }

    public List<Point> calculateFloodedAreas(Field field, List<Point> body) {
        val neighbors = Arrays.asList(
            Point.of(0, -1),
            Point.of(-1, 0),
            Point.of(0, 1),
            Point.of(1, 0));
        val cells = field.getCells();
        List<Set<Point>> areas = new ArrayList<>();
        Set<Point> boundaryArea = new HashSet<>(body); // The same as body ?

        Function<Point, Boolean> area = p -> areas.stream().anyMatch(points -> points.contains(p));

        for (val b : body) {
            // search in the neighborhood of p empty areas
            // empty means not only empty surface but also free of players
            val starts = neighbors.stream()
                .map(n -> Point.of(b.getRow() + n.getRow(), b.getCol() + n.getCol()))
                .filter(it -> this.hasInside(it, field)
                    && cells.get(it).isEmpty()
                    && !boundaryArea.contains(it)
                    && !area.apply(it))
                .collect(Collectors.toSet());

            val newSets = starts.stream().map(it -> this.flood(field, boundaryArea, it)).distinct()
                .collect(toList());
            areas.addAll(newSets);
        }
        // hack to handle the case
        // * * * * * * * * B
        // *     a A       *
        // *     a a       *
        // *               *
        // D * * * * * * * C
        if (areas.size() <= 1) {
            areas.add(Collections.emptySet());
        }
        val flooded = new ArrayList<Point>(areas
            .stream()
            .min(Comparator.comparing(Set::size))
            .orElseGet(() -> {
                log.warn("Broken invariant, areas must be nonempty, " +
                    " field=" + field +
                    " body=" + body);
                return Sets.newHashSet();
            })
        );
        flooded.addAll(body);
        return flooded;
    }

    public Set<Point> flood(Field field, Set<Point> boundary, Point start) {
        val neighbors = Arrays.asList(Point.of(0, -1),
            Point.of(-1, 0),
            Point.of(0, 1),
            Point.of(1, 0));

        // result is the growing set of points describing the filled area
        Set<Point> result = new HashSet<>();
        Predicate<Point> area = p -> {
            Cell cell = field.getCells().get(p);
            return hasInside(p, field)
                && !result.contains(p)
                && !boundary.contains(p)
                && cell.isEmpty();
        };

        if (!area.test(start))
            return result;
        // now we know that start point is somewhere inside
        Deque<Point> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Point cur = queue.pollFirst();
            result.add(cur);
            List<Point> candidates = neighbors.stream()
                .map(pnt -> Point.of(cur.getRow() + pnt.getRow(), cur.getCol() + pnt.getCol()))
                .filter(pnt -> area.test(pnt) && !queue.contains(pnt))
                .collect(toList());
            queue.addAll(candidates);
        }
        return result;
    }

    /**
     * @param duration number of iterations, all bots get the same number of steps
     * @param percent should be the number of percent, i.e. 95.0, not 0.95
     *                for 95% coverage
     * @param randomSeedOpt if not set, initial origins and ordering will be default and
     *                      the seed for the match will not be set
     */
    public Match createMatch(
        int height, int width, Supplier<List<Bot>> botsFactory,
        long duration, double percent, Optional<Long> randomSeedOpt
    ) {
        val initializerRng = randomSeedOpt.map(Random::new);
        val bots = botsFactory.get();
        val field = this.createDefaultField(height, width);
        val perm0 = createDefaultPermutation(bots.size());
        // permute players if we have random
        val originPerm = initializerRng.map(r -> copyShuffledPermutation(perm0, r)).orElse(perm0);
        val origins = this.createOrigins(field.getHeight(), field.getWidth(), originPerm);
        // the list of players is immutable, however the players themselves are mutable
        ImmutableList.Builder<Player> pb = ImmutableList.builder();
        for (val o: origins) {
            pb.add(new Player(Lists.newArrayList(o)));
        }
        val players = pb.build();
        val filledCount = field.getCells().entrySet().stream()
            .map(e -> e.getValue().isEmpty()? 0 : 1)
            .reduce((a, b) -> a + b)
            .orElse(0);
        val scores = IntStream.generate(() -> 0)
            .limit(bots.size()).boxed()
            .collect(toCollection(ArrayList::new));
        val stats = new Stats(0, filledCount, 0, 0, 0, scores);
        val reordering = initializerRng.map(r -> copyShuffledPermutation(perm0, r)).orElse(perm0);
        val gameState = ModelGameState.of(field, origins, players, stats, reordering);
        val botsCopy = ImmutableList.copyOf(bots);
        return Match.builder()
            .gameState(gameState)
            .percent(percent)
            .duration(duration)
            .bots(botsCopy)
            .randomSeed(randomSeedOpt)
            .build();
    }

    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public Replay runMatch(Match match, Optional<Integer> delay, MatchLogger logger) {
        ModelGameState gs = match.getGameState();
        val bots = match.getBots();
        val nb = bots.size();
        // immutable list of bot names for the replay
        // mutable list for logger
        // don't use stream().map().collect() since we need mutable array list
        val botNames = new ArrayList<String>(nb);
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Bot bot : bots) {
            builder.add(bot.getName());
            botNames.add(bot.getName());
        }
        val replayBotNames = builder.build();
        val stats = gs.getStats();

        val allMoves = new ArrayList<List<Move>>((int) match.getDuration());
        for (int tick = 0; tick < match.getDuration(); tick++) {
            // if the cells has filled enough, do finish
            if (getFillPercentage(gs) >= match.getPercent()) {
                break;
            }
            // now do loop for bots
            // tick + 1 because we want last iteration == allMoves.size
            stats.setIteration(tick + 1);
            val moves = new ArrayList<Move>(nb);
            for (int k = 0; k < nb; k++) {
                moves.add(Move.STOP);
            }
            // enumerate all the bots, move them
            for (int k = 0; k < nb; k++) {
                int idx = gs.getReordering().get(k);
                GameState gsView = getClientGameState(gs, idx);
                val bot = bots.get(idx);
                val move = safeMove(gsView, bot);
                if (move.isPresent()) {
                    gs = step(gs, idx, move.get());
                    moves.set(idx, move.get());
                    botNames.set(idx, bot.getName());
                    logger.accept(gs, botNames);
                } else {
                    log.debug("Bot " + idx + " failed on step " + tick);
                    moves.add(Move.STOP);
                }
                delay.ifPresent(d -> {
                    try {
                        Thread.sleep(d);
                    } catch (InterruptedException e) {
                        log.debug("Interrupt: ", e);
                    }
                });
            }
            // enumerated all bots, gathered
            allMoves.add(moves);
        }
        val immStats = ImmutableStats.builder()
            .iteration(stats.iteration)
            .filledCount(stats.filledCount)
            .headToHeadCount(stats.headToHeadCount)
            .selfBiteCount(stats.selfBiteCount)
            .biteCount(stats.biteCount)
            .scores(ImmutableList.copyOf(stats.scores))
            .build();
        // return stub for now
        return new Replay(
            gs.getField().getHeight(),
            gs.getField().getWidth(),
            match.getPercent(),
            match.getDuration(),
            replayBotNames,
            allMoves,
            immStats,
            match.getRandomSeed()
        );
    }

    public ModelGameState runReplay(Replay replay, Optional<Long> delay, MatchLogger logger) {
        Supplier<List<Bot>> factory = () -> {
            List<String> names = replay.getBotNames();
            List<Bot> bots = new ArrayList<>(names.size());
            for (String name : names) {
                bots.add(new Bot() {
                    public String getName() { return name; }
                    public Move move(GameState gs) { return Move.STOP; }
                });
            }
            return bots;
        };
        ModelGameState mgs = createMatch(
            replay.getHeight(),
            replay.getWidth(),
            factory,
            replay.getDuration(),
            replay.getPercent(),
            replay.getRandomSeed()
        ).getGameState();
        val botNames = replay.getBotNames();
        for (int tick = 0; tick < replay.getMoves().size(); tick++) {
            mgs.getStats().setIteration(tick + 1);
            // enumerate all the bots, move them
            val nb = mgs.getPlayers().size();
            val reordering = mgs.getReordering();
            List<Move> tickMoves = replay.getMoves().get(tick);
            for (int k = 0; k < nb; k++) {
                int idx = reordering.get(k);
                Move move = tickMoves.get(idx);
                mgs = step(mgs, idx, move);
                logger.accept(mgs, botNames);
                delay.ifPresent(d -> {
                    try {
                        Thread.sleep(d);
                    } catch (InterruptedException e) {
                        log.debug("Interrupt: ", e);
                    }
                });
            }
        }
        return mgs;
    }

    public String describeGameState(ModelGameState gs, List<String> botNames,
        boolean rewindPosition, boolean useColors
    ) {

        val field = gs.getField();
        val players = gs.getPlayers();
        val layout0 = new String[field.getHeight()][field.getWidth()];

        val ASCII_LETTERS = "abcdefghijklmnopqrstuvwxyz";
        val marks = new ArrayList<Pair<String, String>>();
        for (int idx = 0; idx < players.size(); idx++) {
            String mark = String.valueOf(ASCII_LETTERS.charAt(idx));
            marks.add(Pair.of(mark, mark.toUpperCase()));
        }

        field.getCells().forEach((p, cell) -> {
            switch (cell.getCellType()) {
                case EMPTY:
                    layout0[p.getRow()][p.getCol()] = " ";
                    break;
                case OWNED:
                    if (useColors) {
                        layout0[p.getRow()][p.getCol()] = colors.get(cell.getBotId()) + cell.getBotId().toString() + "\u001B[97m";
                    } else {
                        layout0[p.getRow()][p.getCol()] = cell.getBotId().toString();
                    }
                    break;
                case BORDER:
                    layout0[p.getRow()][p.getCol()] = "*";
                    break;
            }
        });

        val ai = new AtomicInteger(0);
        for (Player player : players) {
            val body = player.getBody();
            val lastIdx = body.size() - 1;
            for (int k = 0; k < body.size(); k++) {
                Point p = body.get(k);
                if (k == lastIdx)
                    layout0[p.getRow()][p.getCol()] = marks.get(ai.get()).getRight();
                else
                    layout0[p.getRow()][p.getCol()] = marks.get(ai.get()).getLeft();
            }
            ai.incrementAndGet();
        }
        List<Integer> scores = calculateScores(gs);

        val sb = new StringBuilder();
        val width = layout0[0].length * 2 - 1;
        sb.append(StringUtils.repeat(' ', width)).append('\n');
        val count = players.size();
        for (int idx = 0; idx < count; idx++) {
            val letter = marks.get(idx).getRight();
            val score = scores.get(idx);
            String botName;
            if (idx < botNames.size())
                botName = botNames.get(idx);
            else
                botName = "bot_" + idx;
            val info = letter + ": \"" + botName + "\" - " + score;
            // extend the bot information to fit exactly needed width
            sb.append(info)
                .append(StringUtils.repeat(' ', width - info.length()))
                .append('\n');
        }
        for (String[] row : layout0) {
            sb.append(String.join(".", Arrays.asList(row))).append('\n');
        }
        if (rewindPosition) {
            // move the cursor "up" to write the cells again on the next iteration
            val upCount = layout0.length + scores.size() + 2;
            sb.append(StringUtils.repeat("\u001B[A", upCount));
        }
        return sb.toString();
    }

    private Point calculateHead(Field field, Point bot, Move move) {
        int row = bot.getRow();
        int col = bot.getCol();
        switch (move) {
            case UP:
                row -= (0 <= row - 1) ? 1 : 0;
                break;
            case DOWN:
                row += (row + 1 < field.getHeight()) ? 1 : 0;
                break;
            case LEFT:
                col -= (0 <= col - 1) ? 1 : 0;
                break;
            case RIGHT:
                col += (col + 1 < field.getWidth()) ? 1 : 0;
                break;
            case STOP: // stay at position
                break;
        }
        return Point.of(row, col);
    }

    private Optional<Move> safeMove(GameState gs, Bot bot) {
        try {
            return Optional.of(bot.move(gs));
        } catch (Exception e) {
            // we get here when bot throws an exception or thinks too long
            log.warn("Error occurred for bot {}: ", bot.getName());
        }
        // empty shows, that the bot does something bad
        return Optional.empty();
    }

    private boolean hasInside(Point p, Field field) {
        return (p.getCol() >= 0 && p.getCol() < field.getWidth())
            && (p.getRow() >= 0 && p.getRow() < field.getHeight());
    }

    public List<Integer> createDefaultPermutation(int len) {
        val builder = ImmutableList.<Integer>builder();
        for (int i = 0; i < len; i++) {
            builder.add(i);
        }
        return builder.build();
    }

    public List<Integer> copyShuffledPermutation(List<Integer> xs, Random random) {
        val tmp = new Integer[xs.size()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = xs.get(i);
        }
        for (int i = xs.size() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // swap
            int a = tmp[index];
            tmp[index] = tmp[i];
            tmp[i] = a;
        }
        return ImmutableList.copyOf(tmp);
    }
}
