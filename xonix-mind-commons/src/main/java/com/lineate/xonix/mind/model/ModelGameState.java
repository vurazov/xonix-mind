package com.lineate.xonix.mind.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The game state is mutable. To safely copy it, use deepCopy.
 */
@Value
@Wither
@AllArgsConstructor(staticName = "of")
public class ModelGameState {
    @NonNull
    Field field;

    @NonNull
    List<Point> origins;

    @NonNull
    List<Player> players;

    @NonNull
    Stats stats;

    @NonNull
    List<Integer> reordering;

    @Override
    public String toString() {
        return formatString(this);
    }

    private static final List<Point> neigh =
            ImmutableList.of(Point.of(0, -1), Point.of(-1, 0), Point.of(0, 1), Point.of(1, 0));

    public String formatString(ModelGameState gs) {
        val f = gs.getField();
        val m = f.getHeight();
        val n = f.getWidth();
        val layer0 = new char[m][n];
        val layer1 = new char[m][n];
        for (int i = 0; i < f.getHeight(); i++) {
            for (int j = 0; j < f.getWidth(); j++) {
                val cell = f.getCells().get(Point.of(i, j));
                layer0[i][j] = ' ';
                layer1[i][j] = '.';
                if (cell != null) {
                    if (cell.isEmpty()) {
                        layer0[i][j] = ' ';
                    } else if (cell.isBorder()) {
                        layer0[i][j] = '*';
                    } else if (cell.isOwned()) {
                        layer0[i][j] = (char) ('0' + cell.getBotId());
                    }
                }
            }
        }
        for (int k = 0; k < gs.getPlayers().size(); k++) {
            val player = gs.getPlayers().get(k);
            val ch = (char)('A' + k);
            for (int l = 0; l < player.getBody().size(); l++) {
                val p = player.getBody().get(l);
                // if it is the last element, i.e. bot's head
                if (l == player.getBody().size() - 1) {
                    layer1[p.getRow()][p.getCol()] = ch;
                } else {
                    layer1[p.getRow()][p.getCol()] = Character.toLowerCase(ch);
                }
            }
        }
        // merge layers
        val sb = new StringBuilder((m + 1) * n * 2);
        for (int i = 0; i < f.getHeight(); i++) {
            for (int j = 0; j < f.getWidth(); j++) {
                sb.append(layer0[i][j])
                    .append(layer1[i][j]);
            }
            sb.append('\n');
        }
        sb.append("reordering=")
            .append("[")
            .append(StringUtils.join(gs.getReordering(), ','))
            .append("]\n");
        val stats = gs.getStats();
        sb.append("stats=")
            .append(String.format("Stats(%s,%s,%s,%s,%s,[%s])",
                stats.iteration,
                stats.filledCount,
                stats.headToHeadCount,
                stats.selfBiteCount,
                stats.biteCount,
                StringUtils.join(stats.scores, ',')
            )).append('\n');
        sb.append("origins=")
            .append("[")
            .append(StringUtils.join(gs.getOrigins(), ','))
            .append("]\n");
        return sb.toString();
    }

    public static ModelGameState parseString(String str) {
        // detect sizes
        val rawLines = Arrays.stream(str.split("\n"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(toList());
        val lines = new ArrayList<String>(rawLines.size());
        val rest = new ArrayList<String>();
        for (String s : rawLines) {
            if (s.startsWith("*")) {
                lines.add(s);
            } else {
                rest.add(s);
            }
        }
        val m = lines.size();
        val n = lines.stream().map(it -> it.length() / 2)
            .max(Integer::compareTo).orElse(0);
        val layer0 = new char[m][n];
        val layer1 = new char[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n * 2; j++) {
                val c = lines.get(i).charAt(j);
                if (j % 2 == 0)
                    layer0[i][j / 2] = c;
                else
                    layer1[i][j / 2] = c;
            }
        }
        // playersMap contained by a playerList
        val playersMap = new HashMap<Integer, ArrayList<Point>>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                val c = layer1[i][j];
                if (Character.isLetter(c) && Character.isUpperCase(c))
                    playersMap.put(c - 'A', Lists.newArrayList(Point.of(i, j)));
            }
        }
        val maxId = playersMap.keySet().stream().max(Integer::compareTo).orElse(-1);
        for (int k = 0; k <= maxId; k++) {
            playersMap.computeIfAbsent(k, _k -> Lists.newArrayList());
        }
        // now playersMap is guaranteed to have size == number of players
        val cb = new ArrayList<Pair<Point, Cell>>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                val c = layer0[i][j];
                Cell cell;
                if (c == '*') {
                    cell = Cell.border();
                } else if (Character.isDigit(c)) {
                    cell = Cell.owned(c - '0');
                } else {
                    cell = Cell.empty(); // space
                }
                cb.add(Pair.of(Point.of(i, j), cell));
            }
        }
        // convert list of pairs to map
        val cells = cb.stream().collect(toMap(Pair::getLeft, Pair::getRight));
        // now build player bodies = tails + heads
        // head is the last element of the corresponding list
        for (val kv : playersMap.entrySet()) {
            val body = kv.getValue();
            if (!body.isEmpty()) {
                // current point, start with head
                val cp = new AtomicReference<Point>(body.get(0));
                val ct = 'a' + kv.getKey(); // the player's tail char
                while (cp.get() != null) {
                    // seek for lower letter around until not found
                    val t = cp.get();
                    Optional<Point> mbPoint = neigh.stream().map(nit -> {
                        val i = bound(t.getRow() + nit.getRow(), 0, m - 1);
                        val j = bound(t.getCol() + nit.getCol(), 0, n - 1);
                        return Point.of(i, j);
                    }).filter(p ->
                        !body.contains(p) && layer1[p.getRow()][p.getCol()] == ct
                    ).findFirst();
                    Point point = mbPoint.orElse(null);
                    if (point != null) {
                        body.add(0, point);
                    }
                    cp.set(point);
                }
            }
            //body is empty => do nothing, the player has been killed maybe
        }
        val np = playersMap.size();
        val filledCount = cells.entrySet().stream()
            .map(e -> e.getValue().isEmpty()? 0: 1)
            .reduce((a, b) -> a + b)
            .orElse(0);
        // calculate scores
        val scores = IntStream.generate(() -> 0)
            .limit(np).boxed()
            .collect(toCollection(ArrayList::new));
        for (Cell cell : cells.values()) {
            if (cell.isOwned()) {
                int old = scores.get(cell.getBotId());
                scores.set(cell.getBotId(), old + 1);
            }
        }
        // initialize origins by the number of players
        ImmutableList.Builder<Player> pb = ImmutableList.builder();
        for (int k = 0; k < np; k++) {
            pb.add(new Player(playersMap.get(k)));
        }
        val players = pb.build();
        val field = new Field(m, n, cells);
        // reordering and stats
        val triple = parseRest(np, rest);
        val reordering = triple.getReordering().orElseGet(() ->
            Gameplay.IT.createDefaultPermutation(np));
        val origins = triple.getOrigins().orElseGet(() ->
            ImmutableList.copyOf(Gameplay.IT.createOrigins(m, n, np)));
        val stats = triple.getStats().orElseGet(() ->
            new Stats(0, 0, 0, 0, 0, scores));
        stats.setFilledCount(filledCount);

        return ModelGameState.of(field, origins, players, stats, reordering);
    }

    private static ParseRestResult parseRest(int np, List<String> rest) {
        Optional<List<Integer>> reordering = Optional.empty();
        Optional<List<Point>> origins = Optional.empty();
        Optional<Stats> stats = Optional.empty();
        for (String str : rest) {
            String[] lr = str.split("=");
            String l = lr[0].trim();
            String r = lr[1].trim();
            switch (l) {
                case "reordering": {
                    Matcher m1 = Pattern.compile("\\[(.*?)]").matcher(r);
                    if (m1.matches()) {
                        List<Integer> list = Arrays.stream(m1.group(1).split(","))
                            .map(s -> Integer.parseInt(s.trim()))
                            .collect(toCollection(ArrayList::new));
                        // check
                        boolean allPresent = IntStream.range(0, np).allMatch(list::contains);
                        if (list.size() != np || !allPresent) {
                            throw new RuntimeException("Cannot parse, np=" + np + " rest=" + rest);
                        }
                        reordering = Optional.of(list);
                    }
                    break;
                }
                case "stats": {
                    Matcher m1 = Pattern.compile("Stats\\((.*?)\\)").matcher(r);
                    if (m1.matches()) {
                        Matcher m2 = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),\\[(.*?)]")
                            .matcher(m1.group(1));
                        if (m2.matches()) {
                            int a1 = Integer.parseInt(m2.group(1));
                            int a2 = Integer.parseInt(m2.group(2));
                            int a3 = Integer.parseInt(m2.group(3));
                            int a4 = Integer.parseInt(m2.group(4));
                            int a5 = Integer.parseInt(m2.group(5));
                            List<Integer> scores = Arrays.stream(m2.group(6).split(","))
                                .map(s -> Integer.parseInt(s.trim()))
                                .collect(toCollection(ArrayList::new));
                            stats = Optional.of(new Stats(a1, a2, a3, a4, a5, scores));
                        }
                    }
                    break;
                }
                case "origins": {
                    Matcher m1 = Pattern.compile("\\[(.*?)]").matcher(r);
                    if (m1.matches()) {
                        Matcher m2 = Pattern.compile("\\((\\d+),(\\d+)\\),?").matcher(m1.group(1));
                        List<Point> list = new ArrayList<>();
                        while (m2.find()) {
                            list.add(Point.of(
                                Integer.parseInt(m2.group(1)),
                                Integer.parseInt(m2.group(2))
                            ));
                        }
                        if (list.size() != np) {
                            throw new RuntimeException("Cannot parse, np=" + np + " rest=" + rest);
                        }
                        origins = Optional.of(list);
                    }
                    break;
                }
                default:
                    throw new RuntimeException("Unexpected key: " + l);
            }
        }
        return new ParseRestResult(reordering, origins, stats);
    }

    @SuppressWarnings("SameParameterValue")
    private static int bound(int x, int l, int r) {
        if (x < l) return l;
        else if (r < x) return r;
        else return x;
    }

    @Value
    private static class ParseRestResult {
        Optional<List<Integer>> reordering;
        Optional<List<Point>> origins;
        Optional<Stats> stats;
    }
}
